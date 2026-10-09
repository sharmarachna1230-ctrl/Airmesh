package com.example.data.p2p

import com.example.data.model.MeshPacket
import com.example.data.model.PacketType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.Collections
import java.util.UUID

data class RouteEntry(
    val destinationId: String,
    val nextHopId: String,
    val hopCount: Int,
    val path: List<String>,
    val lastUpdated: Long = System.currentTimeMillis()
)

class MeshRoutingEngine(val localNodeId: String) {

    // Routing table: Destination -> Best Next Hop Route
    private val routingTable = mutableMapOf<String, RouteEntry>()

    // Deduplication set to prevent broadcast storm / looping
    private val processedPacketIds = Collections.synchronizedSet(LinkedHashSet<String>())
    private val maxDeduplicationCache = 500

    // Packets destined for this device specifically or broadcasts to be consumed
    private val _incomingPackets = MutableSharedFlow<MeshPacket>(extraBufferCapacity = 64)
    val incomingPackets = _incomingPackets.asSharedFlow()

    // Packets that need to be re-transmitted over Bluetooth to next hop
    private val _forwardPackets = MutableSharedFlow<Pair<String, MeshPacket>>(extraBufferCapacity = 64)
    val forwardPackets = _forwardPackets.asSharedFlow() // Pair(targetNeighborId, packet)

    // Store & Forward pending queue for offline recipients
    private val storeAndForwardBuffer = mutableListOf<MeshPacket>()

    init {
        // Seed default routing entries for known nodes in mesh
        routingTable["C8:2B:96:44:55:66"] = RouteEntry(
            destinationId = "C8:2B:96:44:55:66",
            nextHopId = "F4:34:6A:11:22:33", // Routed via Rahul Sharma's device
            hopCount = 2,
            path = listOf("Rahul's Phone (Relay)")
        )
        routingTable["E2:71:0C:AA:BB:CC"] = RouteEntry(
            destinationId = "E2:71:0C:AA:BB:CC",
            nextHopId = "D4:8A:2F:DD:EE:FF", // Routed via Amit Roy
            hopCount = 2,
            path = listOf("Amit Roy (Router)")
        )
    }

    /**
     * Determines whether a packet has already been received or processed
     */
    fun hasSeenPacket(packetId: String): Boolean {
        return processedPacketIds.contains(packetId)
    }

    private fun markPacketSeen(packetId: String) {
        if (processedPacketIds.size >= maxDeduplicationCache) {
            val oldest = processedPacketIds.firstOrNull()
            if (oldest != null) processedPacketIds.remove(oldest)
        }
        processedPacketIds.add(packetId)
    }

    /**
     * Ingests a raw mesh packet received over Bluetooth or Wi-Fi Direct.
     * Decides whether to consume it locally, relay it to the next hop, or store it.
     */
    suspend fun processIncomingPacket(packet: MeshPacket, receivedFromNeighborId: String): Boolean {
        if (hasSeenPacket(packet.packetId)) {
            // Already processed; discard loop
            return false
        }
        markPacketSeen(packet.packetId)

        // Update routing table dynamically from visited nodes path
        updateRouteFromPacket(packet, receivedFromNeighborId)

        // Case 1: Packet is for ME or is a BROADCAST
        if (packet.destinationId == localNodeId || packet.destinationId == "BROADCAST") {
            _incomingPackets.emit(packet)
            if (packet.destinationId != "BROADCAST") {
                return true // End-of-line reached
            }
        }

        // Case 2: Packet is destined for another peer and has TTL remaining -> RELAY
        if (packet.destinationId != localNodeId && packet.ttl > 1) {
            val updatedVisited = packet.visitedNodes + localNodeId
            val relayedPacket = packet.copy(
                ttl = packet.ttl - 1,
                hopCount = packet.hopCount + 1,
                visitedNodes = updatedVisited
            )

            val route = routingTable[packet.destinationId]
            if (route != null) {
                // Known next hop -> Unicast forwarding
                _forwardPackets.emit(Pair(route.nextHopId, relayedPacket))
            } else {
                // Unknown route -> Flood forward to all connected neighbors except the sender
                _forwardPackets.emit(Pair("FLOOD_ALL_EXCEPT_$receivedFromNeighborId", relayedPacket))
            }
            return true
        }

        // Case 3: TTL expired
        return false
    }

    /**
     * Builds and signs a new outgoing MeshPacket
     */
    fun createPacket(
        type: PacketType,
        destinationId: String,
        encryptedPayload: String,
        iv: String = "",
        isHighSpeedMedia: Boolean = false
    ): MeshPacket {
        val packetId = UUID.randomUUID().toString()
        markPacketSeen(packetId)
        return MeshPacket(
            packetId = packetId,
            type = type,
            sourceId = localNodeId,
            destinationId = destinationId,
            ttl = 5,
            hopCount = 0,
            visitedNodes = listOf(localNodeId),
            encryptedPayload = encryptedPayload,
            iv = iv,
            timestamp = System.currentTimeMillis(),
            isHighSpeedMedia = isHighSpeedMedia
        )
    }

    /**
     * Retrieves the best known route path description for UI display
     */
    fun getRouteDescription(destId: String): String {
        val entry = routingTable[destId] ?: return "Direct P2P Link"
        return if (entry.hopCount <= 1) {
            "Direct Bluetooth BLE (0 Hops)"
        } else {
            "Multi-Hop Mesh (${entry.hopCount} Hops via ${entry.path.joinToString(" ➔ ")})"
        }
    }

    private fun updateRouteFromPacket(packet: MeshPacket, directNeighbor: String) {
        val source = packet.sourceId
        val hops = packet.visitedNodes.size
        val existing = routingTable[source]
        if (existing == null || hops < existing.hopCount) {
            routingTable[source] = RouteEntry(
                destinationId = source,
                nextHopId = directNeighbor,
                hopCount = hops,
                path = packet.visitedNodes,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }

    fun queueStoreAndForward(packet: MeshPacket) {
        storeAndForwardBuffer.add(packet)
    }

    fun drainStoreAndForwardFor(peerId: String): List<MeshPacket> {
        val ready = storeAndForwardBuffer.filter { it.destinationId == peerId }
        storeAndForwardBuffer.removeAll(ready)
        return ready
    }
}
