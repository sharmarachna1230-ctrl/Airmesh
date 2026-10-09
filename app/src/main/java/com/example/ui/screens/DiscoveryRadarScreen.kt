package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PeerDevice
import com.example.ui.components.RadarAnimation
import com.example.ui.theme.WhatsAppDarkBackground
import com.example.ui.theme.WhatsAppDarkCard
import com.example.ui.theme.WhatsAppDarkSurface
import com.example.ui.theme.WhatsAppPrimary

@Composable
fun DiscoveryRadarScreen(
    peers: List<PeerDevice>,
    isScanning: Boolean,
    onToggleScan: () -> Unit,
    onConnectPeer: (String) -> Unit,
    onStartChat: (PeerDevice) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WhatsAppDarkBackground)
            .padding(16.dp)
    ) {
        // Radar Visualization Area
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Bluetooth Mesh Radar",
                            color = Color(0xFFE9EDEF),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isScanning) "Actively scanning 10-30m perimeter..." else "Scanning paused",
                            color = if (isScanning) Color(0xFF00E676) else Color(0xFF8696A0),
                            fontSize = 12.sp
                        )
                    }

                    IconButton(
                        onClick = onToggleScan,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isScanning) WhatsAppPrimary else Color(0xFF2A3942))
                            .testTag("radar_toggle_scan_button")
                    ) {
                        Icon(
                            imageVector = if (isScanning) Icons.Default.BluetoothSearching else Icons.Default.Bluetooth,
                            contentDescription = "Toggle Scan",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Animated Radar Sweep
                RadarAnimation(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    peers = peers,
                    isScanning = isScanning,
                    onPeerClick = onStartChat
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text("🟢 0–10m (BLE)", fontSize = 11.sp, color = Color(0xFF8696A0))
                    Text("🟡 10–20m (Classic)", fontSize = 11.sp, color = Color(0xFF8696A0))
                    Text("🔵 20–30m (Multi-Hop)", fontSize = 11.sp, color = Color(0xFF8696A0))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Discovered Devices Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "NEARBY DISCOVERED DEVICES (${peers.size})",
                fontSize = 12.sp,
                color = Color(0xFF8696A0),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Auto-Handshake ON",
                fontSize = 11.sp,
                color = Color(0xFF00E676),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // List of Discovered Peers
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(peers, key = { it.id }) { peer ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = WhatsAppDarkSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onStartChat(peer) }
                        .testTag("radar_peer_card_${peer.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar with online dot
                        Box {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(if (peer.isConnected) WhatsAppPrimary else Color(0xFF2A3942)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = peer.displayName.take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (peer.isConnected) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00E676))
                                        .align(Alignment.BottomEnd)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Device Details
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = peer.displayName,
                                    color = Color(0xFFE9EDEF),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (peer.matchedContactName != null) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "📖 Phonebook",
                                        fontSize = 10.sp,
                                        color = Color(0xFF00A884),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "RSSI: ${peer.rssi} dBm • Est. ${peer.distanceMeters}m away",
                                color = Color(0xFF8696A0),
                                fontSize = 12.sp
                            )

                            Text(
                                text = "Protocol: ${peer.linkType.name} • Battery: ${peer.batteryPercent}%",
                                color = Color(0xFF8696A0),
                                fontSize = 11.sp
                            )
                        }

                        // Connect / Chat Action Button
                        if (peer.isConnected) {
                            Button(
                                onClick = { onStartChat(peer) },
                                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("chat_button_${peer.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = "Chat",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Chat", fontSize = 12.sp, color = Color.White)
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onConnectPeer(peer.id) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("pair_button_${peer.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BluetoothConnected,
                                    contentDescription = "Pair",
                                    tint = WhatsAppPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pair", fontSize = 12.sp, color = WhatsAppPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}
