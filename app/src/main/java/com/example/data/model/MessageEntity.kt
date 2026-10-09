package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val recipientId: String,
    val senderName: String,
    val content: String,
    val encryptedPayload: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENT,
    val mediaType: MediaType = MediaType.TEXT,
    val mediaUri: String? = null,
    val mediaDurationMs: Long = 0L,
    val isIncoming: Boolean = false,
    val isEncrypted: Boolean = true,
    val meshHopCount: Int = 0,
    val meshRoute: String = "Direct Link (0 Hops)",
    val isBroadcast: Boolean = false
)

enum class MessageStatus {
    PENDING_STORE_FORWARD, // Clock icon - stored offline waiting for peer/mesh router
    SENT,                  // Single tick
    DELIVERED,             // Double tick (gray)
    READ                   // Double tick (blue)
}

enum class MediaType {
    TEXT,
    VOICE_NOTE,
    IMAGE,
    DOCUMENT
}

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val peerId: String,
    val title: String,
    val phoneNumber: String? = null,
    val lastMessage: String = "",
    val lastTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isBroadcast: Boolean = false,
    val isOnline: Boolean = false,
    val linkType: String = "BLE"
)
