package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.p2p.CallSession
import com.example.data.p2p.CallStatus
import com.example.ui.components.AudioWaveform
import com.example.ui.theme.WhatsAppDarkBackground
import com.example.ui.theme.WhatsAppDarkCard
import com.example.ui.theme.WhatsAppPrimary
import java.util.Locale

@Composable
fun WalkieTalkieCallScreen(
    session: CallSession,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onPushToTalk: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WhatsAppDarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF182229))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Radio,
                    contentDescription = null,
                    tint = Color(0xFF00E676),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AirMesh Offline P2P Walkie-Talkie",
                    fontSize = 12.sp,
                    color = Color(0xFF00E676),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(WhatsAppPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = session.peerName.take(1).uppercase().ifBlank { "P" },
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = session.peerName.ifBlank { "Nearby Peer" },
                color = Color(0xFFE9EDEF),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            val statusText = when (session.status) {
                CallStatus.DIALING -> "Calling via Bluetooth/Wi-Fi Direct..."
                CallStatus.INCOMING -> "Incoming P2P audio call..."
                CallStatus.CONNECTED -> "Encrypted Voice Stream • ${String.format(Locale.US, "%02d:%02d", session.durationSeconds / 60, session.durationSeconds % 60)}"
                CallStatus.ENDED -> "Call Ended"
                CallStatus.IDLE -> ""
            }

            Text(
                text = statusText,
                color = if (session.status == CallStatus.CONNECTED) Color(0xFF00E676) else Color(0xFF8696A0),
                fontSize = 14.sp
            )

            if (session.status == CallStatus.CONNECTED) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Latency: ${session.latencyMs} ms • 16 kHz Full-Duplex PCM",
                    color = Color(0xFF8696A0),
                    fontSize = 11.sp
                )
            }
        }

        // Middle: Live Audio Waveform & Walkie-Talkie Card
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WhatsAppDarkCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "REAL-TIME AUDIO SPECTRUM",
                        fontSize = 11.sp,
                        color = Color(0xFF8696A0),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AudioWaveform(
                        amplitude = session.audioLevel,
                        isActive = (session.status == CallStatus.CONNECTED && !session.isMicMuted)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Push To Talk Big Button
            Surface(
                shape = CircleShape,
                color = if (session.isPushToTalkActive) Color(0xFF00E676) else WhatsAppPrimary,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .size(110.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                onPushToTalk(true)
                                tryAwaitRelease()
                                onPushToTalk(false)
                            }
                        )
                    }
                    .testTag("push_to_talk_button")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Hold to talk",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (session.isPushToTalkActive) "TRANSMITTING" else "HOLD TO TALK",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Bottom Controls: Mute, End Call, Speaker
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute Button
            IconButton(
                onClick = onToggleMute,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (session.isMicMuted) Color(0xFFFF5252) else Color(0xFF2A3942))
                    .testTag("call_mute_button")
            ) {
                Icon(
                    imageVector = if (session.isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mute",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // End Call Button
            IconButton(
                onClick = onEndCall,
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935))
                    .testTag("call_end_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Speaker Button
            IconButton(
                onClick = onToggleSpeaker,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (session.isSpeakerOn) WhatsAppPrimary else Color(0xFF2A3942))
                    .testTag("call_speaker_button")
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Speaker",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
