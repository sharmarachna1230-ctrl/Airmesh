package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.p2p.CallStatus
import com.example.ui.MainViewModel
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.WalkieTalkieCallScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.WhatsAppDarkBackground

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = WhatsAppDarkBackground
                ) {
                    AirMeshApp()
                }
            }
        }
    }
}

@Composable
fun AirMeshApp(viewModel: MainViewModel = viewModel()) {
    // Collect ViewModel states
    val currentTab by viewModel.currentTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val selectedConversation by viewModel.selectedConversation.collectAsState()
    val activeMessages by viewModel.activeMessages.collectAsState()
    val peers by viewModel.discoveredPeers.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val callSession by viewModel.callSession.collectAsState()
    val bandwidthMode by viewModel.bandwidthMode.collectAsState()
    val statusNotice by viewModel.statusNotice.collectAsState()
    val selectedBroadcastRecipients by viewModel.selectedBroadcastRecipients.collectAsState()

    // Permissions launcher
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Reload contacts if permission was granted
        if (permissions[Manifest.permission.READ_CONTACTS] == true) {
            viewModel.loadContacts()
        }
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
                add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.READ_CONTACTS)
            add(Manifest.permission.RECORD_AUDIO)
        }
        permissionsLauncher.launch(permissionsToRequest.toTypedArray())
    }

    // Call Screen overlay takes precedence when call active
    if (callSession.status != CallStatus.IDLE) {
        WalkieTalkieCallScreen(
            session = callSession,
            onEndCall = { viewModel.endCall() },
            onToggleMute = { viewModel.toggleMute() },
            onToggleSpeaker = { viewModel.toggleSpeaker() },
            onPushToTalk = { active -> viewModel.setPushToTalk(active) }
        )
    } else if (selectedConversation != null) {
        // Individual Chat Screen
        ChatScreen(
            conversation = selectedConversation!!,
            messages = activeMessages,
            onBack = { viewModel.closeConversation() },
            onSendMessage = { text, mediaType, durationMs ->
                viewModel.sendMessage(text, mediaType, null, durationMs)
            },
            onStartCall = { peerId, peerName ->
                viewModel.startCall(peerId, peerName)
            }
        )
    } else {
        // Main Home Screen with Tabs
        HomeScreen(
            conversations = conversations,
            peers = peers,
            currentTab = currentTab,
            searchQuery = searchQuery,
            isScanning = isScanning,
            bandwidthMode = bandwidthMode,
            statusNotice = statusNotice,
            selectedBroadcastRecipients = selectedBroadcastRecipients,
            onSelectTab = { tab -> viewModel.setTab(tab) },
            onSearchChange = { query -> viewModel.setSearchQuery(query) },
            onOpenConversation = { conv -> viewModel.openConversation(conv) },
            onStartChatWithPeer = { peer -> viewModel.startChatWithPeer(peer) },
            onConnectPeer = { peerId -> viewModel.connectToPeer(peerId) },
            onToggleScan = { viewModel.toggleScan() },
            onToggleBroadcastSelect = { peerId -> viewModel.toggleBroadcastRecipient(peerId) },
            onSelectAllBroadcast = { viewModel.selectAllBroadcastRecipients() },
            onClearAllBroadcast = { viewModel.clearBroadcastRecipients() },
            onSendBroadcast = { text -> viewModel.sendBroadcast(text) },
            onStartCall = { peerId, peerName -> viewModel.startCall(peerId, peerName) },
            onRefreshContacts = { viewModel.loadContacts() }
        )
    }
}
