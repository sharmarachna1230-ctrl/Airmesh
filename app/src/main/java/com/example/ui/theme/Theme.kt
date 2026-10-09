package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = WhatsAppPrimary,
    onPrimary = Color.Black,
    primaryContainer = WhatsAppDarkSentBubble,
    onPrimaryContainer = WhatsAppDarkTextPrimary,
    secondary = WhatsAppGreen,
    onSecondary = Color.Black,
    secondaryContainer = WhatsAppDarkCard,
    onSecondaryContainer = WhatsAppDarkTextPrimary,
    tertiary = WhatsAppBlueTicks,
    background = WhatsAppDarkBackground,
    onBackground = WhatsAppDarkTextPrimary,
    surface = WhatsAppDarkSurface,
    onSurface = WhatsAppDarkTextPrimary,
    surfaceVariant = WhatsAppDarkCard,
    onSurfaceVariant = WhatsAppDarkTextSecondary,
    outline = WhatsAppDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = WhatsAppPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = WhatsAppLightSentBubble,
    onPrimaryContainer = WhatsAppLightTextPrimary,
    secondary = WhatsAppTeal,
    onSecondary = Color.White,
    secondaryContainer = WhatsAppLightSurface,
    onSecondaryContainer = WhatsAppLightTextPrimary,
    tertiary = WhatsAppBlueTicks,
    background = WhatsAppLightBackground,
    onBackground = WhatsAppLightTextPrimary,
    surface = WhatsAppLightSurface,
    onSurface = WhatsAppLightTextPrimary,
    surfaceVariant = Color(0xFFF0F2F5),
    onSurfaceVariant = WhatsAppLightTextSecondary,
    outline = WhatsAppLightBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek WhatsApp Dark mode for modern mesh look
    dynamicColor: Boolean = false, // Keep WhatsApp signature branding
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
