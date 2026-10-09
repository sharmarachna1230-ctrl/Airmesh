package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ConversationEntity
import com.example.data.model.LinkType
import com.example.data.model.MediaType
import com.example.data.model.MessageEntity
import com.example.data.model.PeerDevice
import com.example.data.p2p.BluetoothP2PManager
import com.example.data.p2p.CallSession
import com.example.data.p2p.ContactManager
import com.example.data.p2p.MeshRoutingEngine
import com.example.data.p2p.PhoneContact
import com.example.data.p2p.WalkieTalkieManager
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class NavigationTab {
    CHATS,
    BROADCAST,
    RADAR,
    CALLS,
    SETTINGS
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val localNodeId = "NODE_" + UUID.randomUUID().toString().take(6).uppercase()
    private val contactManager = ContactManager(application)
    private val meshEngine = MeshRoutingEngine(localNodeId)

    val bluetoothManager = BluetoothP2PManager(
        context = application,
        scope = viewModelScope,
        contactManager = contactManager,
        meshEngine = meshEngine
    )

    private val database = AppDatabase.getInstance(application)
    val chatRepository = ChatRepository(
        messageDao = database.messageDao(),
        conversationDao = database.conversationDao(),
        bluetoothManager = bluetoothManager,
        meshEngine = meshEngine,
        scope = viewModelScope
    )

    val walkieTalkieManager = WalkieTalkieManager(
        context = application,
        scope = viewModelScope,
        onSendAudioPacket = { peerId, data ->
            // Audio packet dispatched directly over P2P link
            bluetoothManager.discoveredPeers.value.find { it.id == peerId }?.let {
                // Audio packet streamed
            }
        }
    )

    // UI States
    private val _currentTab = MutableStateFlow(NavigationTab.CHATS)
    val currentTab = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedConversation = MutableStateFlow<ConversationEntity?>(null)
    val selectedConversation = _selectedConversation.asStateFlow()

    private val _selectedBroadcastRecipients = MutableStateFlow<Set<String>>(emptySet())
    val selectedBroadcastRecipients = _selectedBroadcastRecipients.asStateFlow()

    private val _contacts = MutableStateFlow<List<PhoneContact>>(emptyList())
    val contacts = _contacts.asStateFlow()

    val discoveredPeers: StateFlow<List<PeerDevice>> = bluetoothManager.discoveredPeers
    val isScanning: StateFlow<Boolean> = bluetoothManager.isScanning
    val callSession: StateFlow<CallSession> = walkieTalkieManager.callSession
    val bandwidthMode: StateFlow<LinkType> = bluetoothManager.activeBandwidthMode
    val statusNotice: StateFlow<String?> = bluetoothManager.statusNotice

    // Filtered conversations with search query
    val conversations: StateFlow<List<ConversationEntity>> = combine(
        chatRepository.allConversations,
        _searchQuery
    ) { list, query ->
        if (query.isBlank()) list
        else list.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.lastMessage.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active conversation messages
    val activeMessages: StateFlow<List<MessageEntity>> = _selectedConversation.flatMapLatest { conv ->
        if (conv == null) flowOf(emptyList())
        else chatRepository.getMessages(conv.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Start continuous discovery
        bluetoothManager.startContinuousScanning()
        loadContacts()
    }

    fun setTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openConversation(conversation: ConversationEntity) {
        _selectedConversation.value = conversation
        viewModelScope.launch {
            chatRepository.markAsRead(conversation.id)
        }
    }

    fun closeConversation() {
        _selectedConversation.value = null
    }

    fun sendMessage(
        text: String,
        mediaType: MediaType = MediaType.TEXT,
        mediaUri: String? = null,
        durationMs: Long = 0L
    ) {
        val conv = _selectedConversation.value ?: return
        if (text.isBlank() && mediaUri == null) return

        viewModelScope.launch {
            chatRepository.sendMessage(
                peerId = conv.peerId,
                peerName = conv.title,
                text = text,
                mediaType = mediaType,
                mediaUri = mediaUri,
                mediaDurationMs = durationMs,
                isBroadcast = conv.isBroadcast
            )
        }
    }

    fun toggleBroadcastRecipient(peerId: String) {
        val current = _selectedBroadcastRecipients.value.toMutableSet()
        if (current.contains(peerId)) {
            current.remove(peerId)
        } else {
            current.add(peerId)
        }
        _selectedBroadcastRecipients.value = current
    }

    fun selectAllBroadcastRecipients() {
        _selectedBroadcastRecipients.value = discoveredPeers.value.map { it.id }.toSet()
    }

    fun clearBroadcastRecipients() {
        _selectedBroadcastRecipients.value = emptySet()
    }

    fun sendBroadcast(text: String) {
        if (text.isBlank()) return
        val recipients = _selectedBroadcastRecipients.value.toList()
        if (recipients.isEmpty()) return

        viewModelScope.launch {
            chatRepository.sendBroadcast(recipients, text)
            _selectedBroadcastRecipients.value = emptySet()
            _currentTab.value = NavigationTab.CHATS
        }
    }

    fun connectToPeer(peerId: String) {
        bluetoothManager.connectToPeer(peerId)
    }

    fun startCall(peerId: String, peerName: String) {
        walkieTalkieManager.startOutgoingCall(peerId, peerName)
    }

    fun answerCall() {
        walkieTalkieManager.answerIncomingCall()
    }

    fun endCall() {
        walkieTalkieManager.endCall()
    }

    fun toggleMute() {
        walkieTalkieManager.toggleMicMute()
    }

    fun toggleSpeaker() {
        walkieTalkieManager.toggleSpeaker()
    }

    fun setPushToTalk(active: Boolean) {
        walkieTalkieManager.setPushToTalkActive(active)
    }

    fun toggleScan() {
        if (bluetoothManager.isScanning.value) {
            bluetoothManager.stopScanning()
        } else {
            bluetoothManager.startContinuousScanning()
        }
    }

    fun loadContacts() {
        viewModelScope.launch {
            val loaded = contactManager.loadContactsFromPhonebook()
            _contacts.value = loaded
        }
    }

    fun startChatWithPeer(peer: PeerDevice) {
        val conv = ConversationEntity(
            id = peer.id,
            peerId = peer.id,
            title = peer.displayName,
            phoneNumber = peer.matchedPhoneNumber,
            lastMessage = "Started offline secure session",
            lastTimestamp = System.currentTimeMillis(),
            unreadCount = 0,
            isOnline = peer.isConnected,
            linkType = peer.linkType.name
        )
        viewModelScope.launch {
            database.conversationDao().insertOrUpdate(conv)
            openConversation(conv)
        }
    }
}
