package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConversationEntity
import com.example.data.model.LinkType
import com.example.data.model.PeerDevice
import com.example.ui.NavigationTab
import com.example.ui.theme.WhatsAppDarkBackground
import com.example.ui.theme.WhatsAppDarkCard
import com.example.ui.theme.WhatsAppDarkSurface
import com.example.ui.theme.WhatsAppGreen
import com.example.ui.theme.WhatsAppPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    conversations: List<ConversationEntity>,
    peers: List<PeerDevice>,
    currentTab: NavigationTab,
    searchQuery: String,
    isScanning: Boolean,
    bandwidthMode: LinkType,
    statusNotice: String?,
    selectedBroadcastRecipients: Set<String>,
    onSelectTab: (NavigationTab) -> Unit,
    onSearchChange: (String) -> Unit,
    onOpenConversation: (ConversationEntity) -> Unit,
    onStartChatWithPeer: (PeerDevice) -> Unit,
    onConnectPeer: (String) -> Unit,
    onToggleScan: () -> Unit,
    onToggleBroadcastSelect: (String) -> Unit,
    onSelectAllBroadcast: () -> Unit,
    onClearAllBroadcast: () -> Unit,
    onSendBroadcast: (String) -> Unit,
    onStartCall: (peerId: String, peerName: String) -> Unit,
    onRefreshContacts: () -> Unit
) {
    var isSearchActive by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WhatsAppDarkBackground)
            .statusBarsPadding()
    ) {
        // WhatsApp Top App Bar
        Surface(
            color = WhatsAppDarkSurface,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchChange,
                            placeholder = { Text("Search chats...", color = Color(0xFF8696A0), fontSize = 14.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("search_text_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = WhatsAppPrimary,
                                unfocusedBorderColor = Color(0xFF2A3942),
                                focusedTextColor = Color(0xFFE9EDEF),
                                unfocusedTextColor = Color(0xFFE9EDEF)
                            ),
                            trailingIcon = {
                                IconButton(onClick = {
                                    onSearchChange("")
                                    isSearchActive = false
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close search",
                                        tint = Color(0xFF8696A0)
                                    )
                                }
                            },
                            singleLine = true
                        )
                    } else {
                        // Title & Offline Status Indicator
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "AirMesh",
                                    color = Color(0xFFE9EDEF),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF005C4B))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "OFFLINE P2P",
                                        fontSize = 9.sp,
                                        color = Color(0xFF00E676),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(if (isScanning) Color(0xFF00E676) else Color(0xFFFFB300))
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isScanning) "Mesh Active • ${bandwidthMode.name}" else "Standby",
                                    fontSize = 11.sp,
                                    color = Color(0xFF8696A0)
                                )
                            }
                        }

                        IconButton(
                            onClick = { isSearchActive = true },
                            modifier = Modifier.testTag("home_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF8696A0)
                            )
                        }

                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.testTag("home_menu_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Menu",
                                    tint = Color(0xFF8696A0)
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Download APK to Android") },
                                    leadingIcon = { Icon(Icons.Default.Android, contentDescription = null, tint = WhatsAppPrimary) },
                                    onClick = {
                                        showMenu = false
                                        onSelectTab(NavigationTab.SETTINGS)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Scan Nearby Peers") },
                                    leadingIcon = { Icon(Icons.Default.Radar, contentDescription = null, tint = WhatsAppPrimary) },
                                    onClick = {
                                        showMenu = false
                                        onSelectTab(NavigationTab.RADAR)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("New Broadcast") },
                                    leadingIcon = { Icon(Icons.Default.CellTower, contentDescription = null, tint = WhatsAppPrimary) },
                                    onClick = {
                                        showMenu = false
                                        onSelectTab(NavigationTab.BROADCAST)
                                    }
                                )
                            }
                        }
                    }
                }

                // Temporary notice banner
                AnimatedVisibility(
                    visible = statusNotice != null,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    if (statusNotice != null) {
                        Surface(
                            color = Color(0xFF00382B),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚡ $statusNotice",
                                fontSize = 11.sp,
                                color = Color(0xFF00E676),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // WhatsApp Tab Bar
                ScrollableTabRow(
                    selectedTabIndex = currentTab.ordinal,
                    containerColor = WhatsAppDarkSurface,
                    contentColor = WhatsAppPrimary,
                    edgePadding = 8.dp,
                    divider = {}
                ) {
                    Tab(
                        selected = currentTab == NavigationTab.CHATS,
                        onClick = { onSelectTab(NavigationTab.CHATS) },
                        text = {
                            Text(
                                "CHATS",
                                fontWeight = FontWeight.Bold,
                                color = if (currentTab == NavigationTab.CHATS) WhatsAppPrimary else Color(0xFF8696A0)
                            )
                        },
                        modifier = Modifier.testTag("tab_chats")
                    )
                    Tab(
                        selected = currentTab == NavigationTab.BROADCAST,
                        onClick = { onSelectTab(NavigationTab.BROADCAST) },
                        text = {
                            Text(
                                "BROADCAST",
                                fontWeight = FontWeight.Bold,
                                color = if (currentTab == NavigationTab.BROADCAST) WhatsAppPrimary else Color(0xFF8696A0)
                            )
                        },
                        modifier = Modifier.testTag("tab_broadcast")
                    )
                    Tab(
                        selected = currentTab == NavigationTab.RADAR,
                        onClick = { onSelectTab(NavigationTab.RADAR) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "RADAR",
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentTab == NavigationTab.RADAR) WhatsAppPrimary else Color(0xFF8696A0)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF00E676))
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_radar")
                    )
                    Tab(
                        selected = currentTab == NavigationTab.CALLS,
                        onClick = { onSelectTab(NavigationTab.CALLS) },
                        text = {
                            Text(
                                "CALLS",
                                fontWeight = FontWeight.Bold,
                                color = if (currentTab == NavigationTab.CALLS) WhatsAppPrimary else Color(0xFF8696A0)
                            )
                        },
                        modifier = Modifier.testTag("tab_calls")
                    )
                    Tab(
                        selected = currentTab == NavigationTab.SETTINGS,
                        onClick = { onSelectTab(NavigationTab.SETTINGS) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "DOWNLOAD APK",
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentTab == NavigationTab.SETTINGS) WhatsAppGreen else Color(0xFF8696A0)
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_apk")
                    )
                }
            }
        }

        // Tab Content Container
        Box(modifier = Modifier.weight(1f)) {
            when (currentTab) {
                NavigationTab.CHATS -> {
                    ChatsListTab(
                        conversations = conversations,
                        onOpenConversation = onOpenConversation
                    )
                }
                NavigationTab.BROADCAST -> {
                    BroadcastScreen(
                        peers = peers,
                        selectedIds = selectedBroadcastRecipients,
                        onToggleSelect = onToggleBroadcastSelect,
                        onSelectAll = onSelectAllBroadcast,
                        onClearAll = onClearAllBroadcast,
                        onSendBroadcast = onSendBroadcast
                    )
                }
                NavigationTab.RADAR -> {
                    DiscoveryRadarScreen(
                        peers = peers,
                        isScanning = isScanning,
                        onToggleScan = onToggleScan,
                        onConnectPeer = onConnectPeer,
                        onStartChat = onStartChatWithPeer
                    )
                }
                NavigationTab.CALLS -> {
                    CallsTab(
                        peers = peers,
                        onStartCall = onStartCall
                    )
                }
                NavigationTab.SETTINGS -> {
                    SettingsScreen(
                        onRefreshContacts = onRefreshContacts
                    )
                }
            }

            // Floating Action Button
            if (currentTab == NavigationTab.CHATS) {
                FloatingActionButton(
                    onClick = { onSelectTab(NavigationTab.RADAR) },
                    containerColor = WhatsAppPrimary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .testTag("fab_new_chat")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "New Chat via Radar"
                    )
                }
            }
        }
    }
}

@Composable
fun ChatsListTab(
    conversations: List<ConversationEntity>,
    onOpenConversation: (ConversationEntity) -> Unit
) {
    if (conversations.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📡", fontSize = 42.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "No offline conversations yet",
                    color = Color(0xFFE9EDEF),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Switch to RADAR tab to discover nearby Bluetooth peers",
                    color = Color(0xFF8696A0),
                    fontSize = 13.sp
                )
            }
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(conversations, key = { it.id }) { conv ->
                ConversationItemRow(
                    conversation = conv,
                    onClick = { onOpenConversation(conv) }
                )
            }
        }
    }
}

@Composable
fun ConversationItemRow(
    conversation: ConversationEntity,
    onClick: () -> Unit
) {
    Surface(
        color = WhatsAppDarkBackground,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("conversation_row_${conversation.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(if (conversation.isBroadcast) Color(0xFF128C7E) else WhatsAppPrimary),
                contentAlignment = Alignment.Center
            ) {
                if (conversation.isBroadcast) {
                    Icon(
                        imageVector = Icons.Default.CellTower,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                } else {
                    Text(
                        text = conversation.title.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Contact Name & Last message
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.title,
                        color = Color(0xFFE9EDEF),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    val timeStr = remember(conversation.lastTimestamp) {
                        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(conversation.lastTimestamp))
                    }
                    Text(
                        text = timeStr,
                        color = if (conversation.unreadCount > 0) WhatsAppGreen else Color(0xFF8696A0),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.lastMessage,
                        color = Color(0xFF8696A0),
                        fontSize = 13.sp,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    if (conversation.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(WhatsAppGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${conversation.unreadCount}",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CallsTab(
    peers: List<PeerDevice>,
    onStartCall: (peerId: String, peerName: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = WhatsAppDarkSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(WhatsAppPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Walkie Talkie",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Local P2P Audio Streaming",
                        color = Color(0xFFE9EDEF),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ultra low-latency audio packet delivery without cellular or internet",
                        color = Color(0xFF8696A0),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "AVAILABLE PEERS FOR WALKIE-TALKIE:",
            fontSize = 12.sp,
            color = Color(0xFF8696A0),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(peers, key = { it.id }) { peer ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = WhatsAppDarkSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onStartCall(peer.id, peer.displayName) }
                        .testTag("call_peer_${peer.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(WhatsAppPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = peer.displayName.take(1).uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = peer.displayName,
                                color = Color(0xFFE9EDEF),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Est. distance: ${peer.distanceMeters}m • Direct Wi-Fi/BLE",
                                color = Color(0xFF8696A0),
                                fontSize = 12.sp
                            )
                        }

                        IconButton(
                            onClick = { onStartCall(peer.id, peer.displayName) },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(WhatsAppPrimary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
