package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.WhatsAppDarkBackground
import com.example.ui.theme.WhatsAppDarkCard
import com.example.ui.theme.WhatsAppDarkSurface
import com.example.ui.theme.WhatsAppPrimary

@Composable
fun SettingsScreen(
    onRefreshContacts: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WhatsAppDarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Highlighted Banner: Download on Android Phone
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF00382B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(WhatsAppPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Android,
                            contentDescription = "Android APK Download",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Download APK to Android Phone",
                            color = Color(0xFFE9EDEF),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Install & run completely without internet",
                            color = Color(0xFF00E676),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Follow these simple steps to install AirMesh on your physical Android smartphone:",
                    fontSize = 13.sp,
                    color = Color(0xFFE9EDEF),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                InstallationStepItem(
                    step = "1",
                    title = "Export / Download APK",
                    desc = "In AI Studio top-right menu (⚙️ or ⋮), tap 'Download APK' or 'Export Project' to save the APK file."
                )

                InstallationStepItem(
                    step = "2",
                    title = "Send to Phone",
                    desc = "Transfer the generated APK file to your phone using USB, Bluetooth, or local file manager."
                )

                InstallationStepItem(
                    step = "3",
                    title = "Enable Unknown Sources",
                    desc = "On your Android phone, go to Settings ➔ Security ➔ Allow Install from Unknown Sources."
                )

                InstallationStepItem(
                    step = "4",
                    title = "Install & Enjoy",
                    desc = "Tap the APK to install. Turn on Bluetooth and talk to nearby friends without any SIM, Wi-Fi router, or mobile data!"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Install AirMesh: 100% Offline P2P WhatsApp via Bluetooth & Wi-Fi Direct. No Internet required! Build and export your APK from AI Studio."
                            )
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share AirMesh App"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("share_app_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Share AirMesh APK Link with Friends",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // P2P Protocol Settings
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "P2P PROTOCOL & MESH ENGINE",
                    fontSize = 12.sp,
                    color = Color(0xFF8696A0),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                ProtocolSpecRow(
                    title = "Encryption Standard",
                    value = "AES-256-GCM + ECDH",
                    icon = Icons.Default.Lock
                )

                ProtocolSpecRow(
                    title = "Discovery Mechanism",
                    value = "Continuous BLE Beacons + RFCOMM",
                    icon = Icons.Default.Bluetooth
                )

                ProtocolSpecRow(
                    title = "Routing Model",
                    value = "Multi-Hop Store & Forward (5 TTL)",
                    icon = Icons.Default.PhoneAndroid
                )

                ProtocolSpecRow(
                    title = "Local Storage",
                    value = "SQLite / Room (Zero Cloud)",
                    icon = Icons.Default.Info
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Phonebook Contacts Refresh
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "PHONEBOOK & CONTACTS",
                    fontSize = 12.sp,
                    color = Color(0xFF8696A0),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "AirMesh matches nearby Bluetooth MAC addresses with names stored in your Android phonebook.",
                    fontSize = 13.sp,
                    color = Color(0xFFE9EDEF)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        onRefreshContacts()
                        Toast.makeText(context, "Phonebook contacts synchronized!", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("refresh_contacts_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync",
                        tint = WhatsAppPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Re-sync Phone Contacts", color = WhatsAppPrimary)
                }
            }
        }
    }
}

@Composable
fun InstallationStepItem(step: String, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(WhatsAppPrimary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE9EDEF)
            )
            Text(
                text = desc,
                fontSize = 12.sp,
                color = Color(0xFF8696A0),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun ProtocolSpecRow(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = WhatsAppPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.sp, color = Color(0xFFE9EDEF), fontWeight = FontWeight.Medium)
            Text(text = value, fontSize = 11.sp, color = Color(0xFF8696A0))
        }
    }
}
