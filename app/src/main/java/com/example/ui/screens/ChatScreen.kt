package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.crypto.E2EEncryptionManager
import com.example.data.model.ConversationEntity
import com.example.data.model.MediaType
import com.example.data.model.MessageEntity
import com.example.ui.components.ChatBubble
import com.example.ui.theme.WhatsAppDarkBackground
import com.example.ui.theme.WhatsAppDarkCard
import com.example.ui.theme.WhatsAppDarkSurface
import com.example.ui.theme.WhatsAppPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversation: ConversationEntity,
    messages: List<MessageEntity>,
    onBack: () -> Unit,
    onSendMessage: (text: String, mediaType: MediaType, durationMs: Long) -> Unit,
    onStartCall: (peerId: String, peerName: String) -> Unit
) {
    BackHandler { onBack() }

    var inputText by remember { mutableStateOf("") }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showAttachmentMenu by remember { mutableStateOf(false) }
    var showHopInfoDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Auto scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WhatsAppDarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // WhatsApp Top App Bar
        Surface(
            color = WhatsAppDarkSurface,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("chat_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFE9EDEF)
                    )
                }

                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(WhatsAppPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = conversation.title.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Title & Subtitle (Connection / Hop status)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showSecurityDialog = true }
                ) {
                    Text(
                        text = conversation.title,
                        color = Color(0xFFE9EDEF),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = if (conversation.isBroadcast) "1-to-Many Multi-Person Broadcast"
                        else conversation.linkType,
                        color = Color(0xFF00E676),
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }

                // Voice Call (Walkie-Talkie) Button
                if (!conversation.isBroadcast) {
                    IconButton(
                        onClick = { onStartCall(conversation.peerId, conversation.title) },
                        modifier = Modifier.testTag("chat_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "P2P Walkie-Talkie Voice Call",
                            tint = WhatsAppPrimary
                        )
                    }
                }

                IconButton(
                    onClick = { showSecurityDialog = true },
                    modifier = Modifier.testTag("chat_info_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "E2EE Security Info",
                        tint = Color(0xFFFFD54F)
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = Color(0xFF8696A0)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Verify Security Code") },
                            onClick = {
                                showMenu = false
                                showSecurityDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mesh Hop Path") },
                            onClick = {
                                showMenu = false
                                showHopInfoDialog = true
                            }
                        )
                    }
                }
            }
        }

        // WhatsApp Chat Messages Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp)
            ) {
                // E2EE WhatsApp Gold Banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF182229),
                            tonalElevation = 1.dp,
                            modifier = Modifier.clickable { showSecurityDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Encrypted",
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Messages are end-to-end encrypted over Bluetooth Mesh. Tap to verify.",
                                    color = Color(0xFFFFD54F),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                items(messages, key = { it.id }) { message ->
                    ChatBubble(
                        message = message,
                        onRouteClick = { showHopInfoDialog = true }
                    )
                }
            }
        }

        // Attachment selection popup
        if (showAttachmentMenu) {
            Surface(
                color = WhatsAppDarkCard,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                showAttachmentMenu = false
                                onSendMessage("📷 Photo shared via Wi-Fi Direct (48 Mbps)", MediaType.IMAGE, 0L)
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE91E63)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📷", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Gallery Photo", fontSize = 12.sp, color = Color(0xFFE9EDEF))
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                showAttachmentMenu = false
                                onSendMessage("📄 Project_Report.pdf", MediaType.DOCUMENT, 0L)
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF5E35B1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📄", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Document", fontSize = 12.sp, color = Color(0xFFE9EDEF))
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                showAttachmentMenu = false
                                onSendMessage("🎤 Voice Note (12s)", MediaType.VOICE_NOTE, 12000L)
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF9800)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎤", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Voice Note", fontSize = 12.sp, color = Color(0xFFE9EDEF))
                    }
                }
            }
        }

        // WhatsApp Bottom Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = WhatsAppDarkCard,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    IconButton(onClick = { /* Emoji picker */ }) {
                        Icon(
                            imageVector = Icons.Default.SentimentSatisfied,
                            contentDescription = "Emoji",
                            tint = Color(0xFF8696A0)
                        )
                    }

                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                "Message via Bluetooth...",
                                color = Color(0xFF8696A0),
                                fontSize = 15.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color(0xFFE9EDEF),
                            unfocusedTextColor = Color(0xFFE9EDEF)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field")
                    )

                    IconButton(
                        onClick = { showAttachmentMenu = !showAttachmentMenu },
                        modifier = Modifier.testTag("chat_attach_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attach media",
                            tint = Color(0xFF8696A0)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Green Action Button (Send or Mic)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(WhatsAppPrimary)
                    .clickable {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText.trim(), MediaType.TEXT, 0L)
                            inputText = ""
                        } else {
                            // Quick voice note send
                            onSendMessage("Voice message", MediaType.VOICE_NOTE, 8000L)
                        }
                    }
                    .testTag("chat_send_button"),
                contentAlignment = Alignment.Center
            ) {
                if (inputText.isNotBlank()) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Record Voice Note",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }

    // Security Code Dialog (60-digit WhatsApp safety code)
    if (showSecurityDialog) {
        val safetyCode = remember(conversation.peerId) {
            E2EEncryptionManager.generateSafetyFingerprint(conversation.peerId)
        }
        AlertDialog(
            onDismissRequest = { showSecurityDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFFFFD54F),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Verify Security Code",
                    color = Color(0xFFE9EDEF),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "To verify that your offline Bluetooth communication with ${conversation.title} is end-to-end encrypted with AES-256-GCM, compare this 60-digit number with their device:",
                        fontSize = 13.sp,
                        color = Color(0xFF8696A0)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = Color(0xFF182229),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = safetyCode,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E676),
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "✓ Keys exchanged locally via ECDH without internet.",
                        fontSize = 12.sp,
                        color = Color(0xFF00A884)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showSecurityDialog = false }) {
                    Text("Done", color = WhatsAppPrimary)
                }
            }
        )
    }

    // Mesh Hop Route Dialog
    if (showHopInfoDialog) {
        AlertDialog(
            onDismissRequest = { showHopInfoDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Mesh Route Topology",
                    color = Color(0xFFE9EDEF),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "AirMesh Packet Routing Protocol:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE9EDEF)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Link: ${conversation.linkType}\n• Protocol: Bluetooth BLE + Wi-Fi Direct\n• Encryption: AES-256-GCM Zero-Knowledge\n• Intermediate nodes cannot decrypt or inspect payload.",
                        fontSize = 12.sp,
                        color = Color(0xFF8696A0),
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showHopInfoDialog = false }) {
                    Text("OK", color = WhatsAppPrimary)
                }
            }
        )
    }
}
