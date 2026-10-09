package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MediaType
import com.example.data.model.MessageEntity
import com.example.data.model.MessageStatus
import com.example.ui.theme.WhatsAppBlueTicks
import com.example.ui.theme.WhatsAppDarkReceivedBubble
import com.example.ui.theme.WhatsAppDarkSentBubble
import com.example.ui.theme.WhatsAppGrayTicks
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatBubble(
    message: MessageEntity,
    onRouteClick: () -> Unit = {}
) {
    val isOut = !message.isIncoming
    val alignment = if (isOut) Alignment.End else Alignment.Start

    val bubbleColor = if (isOut) WhatsAppDarkSentBubble else WhatsAppDarkReceivedBubble
    val bubbleShape = if (isOut) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        horizontalAlignment = alignment
    ) {
        Surface(
            shape = bubbleShape,
            color = bubbleColor,
            tonalElevation = 2.dp,
            modifier = Modifier
                .widthIn(min = 80.dp, max = 310.dp)
                .testTag("chat_bubble_${message.id}")
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // If message arrived via multi-hop mesh, show discreet route tag
                if (message.meshHopCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.25f))
                            .clickable { onRouteClick() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Route,
                            contentDescription = "Mesh Route",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${message.meshHopCount} Hops Mesh",
                            fontSize = 10.sp,
                            color = Color(0xFFFFB300),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                when (message.mediaType) {
                    MediaType.TEXT -> {
                        Text(
                            text = message.content,
                            color = Color(0xFFE9EDEF),
                            fontSize = 15.sp,
                            lineHeight = 20.sp
                        )
                    }
                    MediaType.VOICE_NOTE -> {
                        VoiceNoteBubbleContent(message)
                    }
                    MediaType.IMAGE -> {
                        ImageBubbleContent(message)
                    }
                    MediaType.DOCUMENT -> {
                        DocumentBubbleContent(message)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom row: encryption lock, timestamp & status ticks
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    if (message.isEncrypted) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Encrypted",
                            tint = Color(0xFFFFD54F).copy(alpha = 0.7f),
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }

                    val timeStr = remember(message.timestamp) {
                        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
                    }
                    Text(
                        text = timeStr,
                        fontSize = 11.sp,
                        color = Color(0xFF8696A0)
                    )

                    if (isOut) {
                        Spacer(modifier = Modifier.width(4.dp))
                        StatusTickIcon(status = message.status)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusTickIcon(status: MessageStatus) {
    when (status) {
        MessageStatus.PENDING_STORE_FORWARD -> {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = "Offline Pending (Store & Forward)",
                tint = WhatsAppGrayTicks,
                modifier = Modifier.size(14.dp)
            )
        }
        MessageStatus.SENT -> {
            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = "Sent",
                tint = WhatsAppGrayTicks,
                modifier = Modifier.size(15.dp)
            )
        }
        MessageStatus.DELIVERED -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Delivered",
                tint = WhatsAppGrayTicks,
                modifier = Modifier.size(16.dp)
            )
        }
        MessageStatus.READ -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Read",
                tint = WhatsAppBlueTicks,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun VoiceNoteBubbleContent(message: MessageEntity) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF00A884)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play voice note",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            // Simulated waveform equalizer bars
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)) {
                val barWidth = 3.dp.toPx()
                val gap = 2.dp.toPx()
                val totalBars = (size.width / (barWidth + gap)).toInt().coerceIn(15, 35)
                for (i in 0 until totalBars) {
                    val h = ((i * 7) % 18 + 4).dp.toPx()
                    val x = i * (barWidth + gap)
                    drawLine(
                        color = Color(0xFF00A884),
                        start = Offset(x, size.height / 2 - h / 2),
                        end = Offset(x, size.height / 2 + h / 2),
                        strokeWidth = barWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
            Text(
                text = "0:${String.format(Locale.US, "%02d", (message.mediaDurationMs / 1000).coerceAtLeast(3))}",
                fontSize = 11.sp,
                color = Color(0xFF8696A0)
            )
        }
    }
}

@Composable
fun ImageBubbleContent(message: MessageEntity) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1B2730)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "📷", fontSize = 38.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Bluetooth High-Speed Media",
                fontSize = 13.sp,
                color = Color(0xFFE9EDEF),
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Wi-Fi Direct P2P Chunk (1.2 MB)",
                fontSize = 11.sp,
                color = Color(0xFF8696A0)
            )
        }
    }
}

@Composable
fun DocumentBubbleContent(message: MessageEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "📄", fontSize = 28.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = message.content.ifBlank { "Offline_Document.pdf" },
                fontSize = 14.sp,
                color = Color(0xFFE9EDEF),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "P2P Mesh Transfer • 420 KB",
                fontSize = 11.sp,
                color = Color(0xFF8696A0)
            )
        }
    }
}
