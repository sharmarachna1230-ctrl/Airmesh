package com.example.data.model

data class PeerDevice(
    val id: String,                         // Bluetooth MAC or unique Hash ID
    val deviceName: String,                 // Raw Bluetooth device name
    val matchedContactName: String? = null, // Matched from Phone Contacts!
    val matchedPhoneNumber: String? = null, // Matched phone number
    val rssi: Int = -65,                    // Received Signal Strength Indicator (dBm)
    val distanceMeters: Double = 3.5,       // Computed from RSSI
    val isConnected: Boolean = false,       // Physical connection status
    val isMeshNeighbor: Boolean = true,     // Accessible within mesh
    val canActAsRelay: Boolean = true,      // Willing to relay packets
    val batteryPercent: Int = 85,           // Battery status
    val linkType: LinkType = LinkType.BLE_LOW_POWER,
    val publicKey: String = "",             // RSA/Diffie-Hellman public key string
    val safetyFingerprint: String = "4810-9283-7182-9901-4192", // 60-digit E2EE safety code
    val lastSeen: Long = System.currentTimeMillis()
) {
    val displayName: String
        get() = matchedContactName ?: deviceName.ifBlank { "Nearby Device (${id.takeLast(4)})" }
}

enum class LinkType {
    BLE_LOW_POWER,        // Bluetooth Low Energy (Text / Heartbeats)
    BLUETOOTH_CLASSIC,    // Bluetooth Classic RFCOMM (Audio / Small files)
    WIFI_DIRECT           // High-speed Wi-Fi Direct P2P (Photos / Video / Walkie-Talkie)
}

data class MeshPacket(
    val packetId: String,
    val type: PacketType,
    val sourceId: String,
    val destinationId: String,       // Target peer, or "BROADCAST"
    val ttl: Int = 4,                // Time to live (max hops)
    val hopCount: Int = 0,
    val visitedNodes: List<String> = emptyList(),
    val encryptedPayload: String,    // AES-256-GCM ciphertext
    val iv: String = "",             // Initialization vector (Base64)
    val signature: String = "",      // HMAC or digital signature
    val timestamp: Long = System.currentTimeMillis(),
    val isHighSpeedMedia: Boolean = false
)

enum class PacketType {
    HANDSHAKE,
    KEY_EXCHANGE,
    CHAT_MESSAGE,
    ACK_DELIVERED,
    ACK_READ,
    BROADCAST_MESSAGE,
    WALKIE_TALKIE_AUDIO,
    MEDIA_CHUNK,
    ROUTING_HEARTBEAT
}
