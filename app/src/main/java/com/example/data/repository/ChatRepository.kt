package com.example.data.repository

import com.example.data.crypto.E2EEncryptionManager
import com.example.data.local.ConversationDao
import com.example.data.local.MessageDao
import com.example.data.model.ConversationEntity
import com.example.data.model.MediaType
import com.example.data.model.MessageEntity
import com.example.data.model.MessageStatus
import com.example.data.model.PacketType
import com.example.data.p2p.BluetoothP2PManager
import com.example.data.p2p.MeshRoutingEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class ChatRepository(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val bluetoothManager: BluetoothP2PManager,
    private val meshEngine: MeshRoutingEngine,
    private val scope: CoroutineScope
) {

    val allConversations: Flow<List<ConversationEntity>> = conversationDao.getAllConversations()

    init {
        // Seed default WhatsApp chats if database empty
        scope.launch(Dispatchers.IO) {
            seedInitialConversationsIfEmpty()
            listenForIncomingMeshPackets()
        }
    }

    fun getMessages(conversationId: String): Flow<List<MessageEntity>> {
        return messageDao.getMessagesForConversation(conversationId)
    }

    suspend fun markAsRead(conversationId: String) {
        messageDao.markConversationAsRead(conversationId)
        conversationDao.resetUnreadCount(conversationId)
    }

    suspend fun sendMessage(
        peerId: String,
        peerName: String,
        text: String,
        mediaType: MediaType = MediaType.TEXT,
        mediaUri: String? = null,
        mediaDurationMs: Long = 0L,
        isBroadcast: Boolean = false
    ) {
        val messageId = UUID.randomUUID().toString()
        val conversationId = if (isBroadcast) "BROADCAST_ALL" else peerId

        // 1. End-to-End Encrypt payload
        val (cipherText, iv) = E2EEncryptionManager.encrypt(text, peerId)

        // 2. Check route path and connection status
        val routeDesc = meshEngine.getRouteDescription(peerId)
        val isPeerDirectlyConnected = bluetoothManager.discoveredPeers.value.find { it.id == peerId }?.isConnected ?: false

        // Determine if target is reachable directly or via multi-hop mesh
        val isReachable = isPeerDirectlyConnected || routeDesc.contains("Multi-Hop")

        val initialStatus = if (isReachable) MessageStatus.SENT else MessageStatus.PENDING_STORE_FORWARD

        // 3. Save to local Room Database
        val message = MessageEntity(
            id = messageId,
            conversationId = conversationId,
            senderId = "ME",
            recipientId = peerId,
            senderName = "You",
            content = text,
            encryptedPayload = cipherText,
            timestamp = System.currentTimeMillis(),
            status = initialStatus,
            mediaType = mediaType,
            mediaUri = mediaUri,
            mediaDurationMs = mediaDurationMs,
            isIncoming = false,
            isEncrypted = true,
            meshHopCount = if (routeDesc.contains("Multi-Hop")) 2 else 0,
            meshRoute = routeDesc,
            isBroadcast = isBroadcast
        )
        messageDao.insertMessage(message)

        // Update Conversation header
        val displaySnippet = when (mediaType) {
            MediaType.TEXT -> text
            MediaType.VOICE_NOTE -> "🎤 Voice message (${mediaDurationMs / 1000}s)"
            MediaType.IMAGE -> "📷 Photo"
            MediaType.DOCUMENT -> "📄 Document"
        }
        conversationDao.updateLastMessage(conversationId, displaySnippet, System.currentTimeMillis())

        if (isReachable) {
            // Dispatch over Bluetooth Mesh
            val isHighSpeed = mediaType != MediaType.TEXT
            if (isHighSpeed) {
                bluetoothManager.upgradeToHighBandwidth(mediaType.name)
            }

            val packet = meshEngine.createPacket(
                type = PacketType.CHAT_MESSAGE,
                destinationId = peerId,
                encryptedPayload = cipherText,
                iv = iv,
                isHighSpeedMedia = isHighSpeed
            )

            meshEngine.processIncomingPacket(packet, "LOCAL_NODE")

            if (isHighSpeed) {
                scope.launch {
                    delay(3000)
                    bluetoothManager.downgradeToLowPower()
                }
            }

            // Simulate realistic P2P peer acknowledgement
            scope.launch {
                delay(600)
                messageDao.updateMessageStatus(messageId, MessageStatus.DELIVERED)
                delay(1200)
                messageDao.updateMessageStatus(messageId, MessageStatus.READ)
            }
        } else {
            // Queue in store and forward buffer
            val packet = meshEngine.createPacket(
                type = PacketType.CHAT_MESSAGE,
                destinationId = peerId,
                encryptedPayload = cipherText,
                iv = iv
            )
            meshEngine.queueStoreAndForward(packet)
        }
    }

    /**
     * Broadcast to multiple contacts at once
     */
    suspend fun sendBroadcast(recipientIds: List<String>, text: String) {
        val (cipherText, iv) = E2EEncryptionManager.encrypt(text, "BROADCAST")
        bluetoothManager.broadcastToMultiplePeers(recipientIds, cipherText, iv)

        val broadcastMsgId = UUID.randomUUID().toString()
        val broadcastMessage = MessageEntity(
            id = broadcastMsgId,
            conversationId = "BROADCAST_ALL",
            senderId = "ME",
            recipientId = "ALL (${recipientIds.size} recipients)",
            senderName = "You",
            content = text,
            encryptedPayload = cipherText,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.DELIVERED,
            mediaType = MediaType.TEXT,
            isIncoming = false,
            isEncrypted = true,
            meshHopCount = 1,
            meshRoute = "1-to-Many BLE Mesh Broadcast",
            isBroadcast = true
        )
        messageDao.insertMessage(broadcastMessage)
        conversationDao.updateLastMessage("BROADCAST_ALL", "📢 $text", System.currentTimeMillis())
    }

    private suspend fun listenForIncomingMeshPackets() {
        meshEngine.incomingPackets.collect { packet ->
            if (packet.sourceId != meshEngine.localNodeId && packet.type == PacketType.CHAT_MESSAGE) {
                // Decrypt message
                val plainText = E2EEncryptionManager.decrypt(packet.encryptedPayload, packet.iv, packet.sourceId)
                val senderName = bluetoothManager.discoveredPeers.value.find { it.id == packet.sourceId }?.displayName
                    ?: "Peer ${packet.sourceId.takeLast(4)}"

                val incomingMsg = MessageEntity(
                    id = packet.packetId,
                    conversationId = packet.sourceId,
                    senderId = packet.sourceId,
                    recipientId = "ME",
                    senderName = senderName,
                    content = plainText,
                    encryptedPayload = packet.encryptedPayload,
                    timestamp = packet.timestamp,
                    status = MessageStatus.DELIVERED,
                    mediaType = MediaType.TEXT,
                    isIncoming = true,
                    isEncrypted = true,
                    meshHopCount = packet.hopCount,
                    meshRoute = if (packet.hopCount > 0) "Mesh Routed (${packet.hopCount} hops)" else "Direct BLE Link",
                    isBroadcast = (packet.destinationId == "BROADCAST")
                )
                messageDao.insertMessage(incomingMsg)
                conversationDao.updateLastMessage(packet.sourceId, plainText, packet.timestamp)
            }
        }
    }

    private suspend fun seedInitialConversationsIfEmpty() {
        val currentList = conversationDao.getAllConversations().first()
        if (currentList.isNotEmpty()) return

        val initialConvs = listOf(
            ConversationEntity(
                id = "F4:34:6A:11:22:33",
                peerId = "F4:34:6A:11:22:33",
                title = "Rahul Sharma",
                phoneNumber = "+91 98765 43210",
                lastMessage = "Hey! Offline Bluetooth mesh works seamlessly 🚀",
                lastTimestamp = System.currentTimeMillis() - 1000 * 60 * 5,
                unreadCount = 1,
                isOnline = true,
                linkType = "Direct BLE (3.2m)"
            ),
            ConversationEntity(
                id = "C8:2B:96:44:55:66",
                peerId = "C8:2B:96:44:55:66",
                title = "Priya Patel",
                phoneNumber = "+91 98234 56789",
                lastMessage = "Got your audio call packet over Wi-Fi Direct!",
                lastTimestamp = System.currentTimeMillis() - 1000 * 60 * 30,
                unreadCount = 0,
                isOnline = true,
                linkType = "Mesh Routed via Rahul"
            ),
            ConversationEntity(
                id = "A0:18:7D:77:88:99",
                peerId = "A0:18:7D:77:88:99",
                title = "Vikram Singh",
                phoneNumber = "+91 97123 45678",
                lastMessage = "Let's test the walkie-talkie mode now.",
                lastTimestamp = System.currentTimeMillis() - 1000 * 60 * 120,
                unreadCount = 0,
                isOnline = true,
                linkType = "Direct BLE (4.8m)"
            ),
            ConversationEntity(
                id = "BROADCAST_ALL",
                peerId = "BROADCAST_ALL",
                title = "Nearby Broadcast (5 Peers)",
                phoneNumber = null,
                lastMessage = "📢 Broadcast channel ready for multi-person delivery",
                lastTimestamp = System.currentTimeMillis() - 1000 * 60 * 360,
                unreadCount = 0,
                isBroadcast = true,
                isOnline = true,
                linkType = "1-to-Many Multi-Hop"
            )
        )

        for (c in initialConvs) {
            conversationDao.insertOrUpdate(c)
        }

        // Seed some initial messages
        val seedMessages = listOf(
            MessageEntity(
                id = "m1",
                conversationId = "F4:34:6A:11:22:33",
                senderId = "F4:34:6A:11:22:33",
                recipientId = "ME",
                senderName = "Rahul Sharma",
                content = "Hey! Tested AirMesh in an area without cell service or Wi-Fi.",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 10,
                status = MessageStatus.READ,
                isIncoming = true,
                isEncrypted = true,
                meshRoute = "Direct BLE Link (3.2m)"
            ),
            MessageEntity(
                id = "m2",
                conversationId = "F4:34:6A:11:22:33",
                senderId = "ME",
                recipientId = "F4:34:6A:11:22:33",
                senderName = "You",
                content = "It automatically encrypted with AES-256-GCM. No servers needed!",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 8,
                status = MessageStatus.READ,
                isIncoming = false,
                isEncrypted = true,
                meshRoute = "Direct BLE Link (3.2m)"
            ),
            MessageEntity(
                id = "m3",
                conversationId = "F4:34:6A:11:22:33",
                senderId = "F4:34:6A:11:22:33",
                recipientId = "ME",
                senderName = "Rahul Sharma",
                content = "Hey! Offline Bluetooth mesh works seamlessly 🚀",
                timestamp = System.currentTimeMillis() - 1000 * 60 * 5,
                status = MessageStatus.DELIVERED,
                isIncoming = true,
                isEncrypted = true,
                meshRoute = "Direct BLE Link (3.2m)"
            )
        )
        messageDao.insertMessages(seedMessages)
    }
}
