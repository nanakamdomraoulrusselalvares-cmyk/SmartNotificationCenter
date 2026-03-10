package com.smartnotification.presentation.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Vibrant Deep Purple & Neon Blue Palette ──────────────────────────────────
// Light theme vibrant colors
val VibrantPrimary40 = Color(0xFF6200EE)
val VibrantOnPrimary40 = Color(0xFFFFFFFF)
val VibrantPrimaryContainer40 = Color(0xFFE8DDFF)
val VibrantOnPrimaryContainer40 = Color(0xFF1F005C)

val VibrantSecondary40 = Color(0xFF03DAC6)
val VibrantOnSecondary40 = Color(0xFF000000)
val VibrantSecondaryContainer40 = Color(0xFFBCFFF7)
val VibrantOnSecondaryContainer40 = Color(0xFF00201D)

val VibrantTertiary40 = Color(0xFFFF0266)
val VibrantOnTertiary40 = Color(0xFFFFFFFF)
val VibrantTertiaryContainer40 = Color(0xFFFFD9E2)
val VibrantOnTertiary40Container = Color(0xFF3E0013)

val VibrantError40 = Color(0xFFB00020)
val VibrantOnError40 = Color(0xFFFFFFFF)
val VibrantErrorContainer40 = Color(0xFFF9DEDC)
val VibrantOnErrorContainer40 = Color(0xFF410E0B)

// Dark theme vibrant colors
val VibrantPrimary80 = Color(0xFFBB86FC)
val VibrantOnPrimary80 = Color(0xFF23005C)
val VibrantPrimaryContainer80 = Color(0xFF4300B2)
val VibrantOnPrimaryContainer80 = Color(0xFFE8DDFF)

val VibrantSecondary80 = Color(0xFF03DAC6)
val VibrantOnSecondary80 = Color(0xFF003732)
val VibrantSecondaryContainer80 = Color(0xFF005049)
val VibrantOnSecondaryContainer80 = Color(0xFFBCFFF7)

val VibrantTertiary80 = Color(0xFFFF80AB)
val VibrantOnTertiary80 = Color(0xFF56001D)
val VibrantTertiaryContainer80 = Color(0xFF7D002E)
val VibrantOnTertiary80Container = Color(0xFFFFD9E2)

val VibrantError80 = Color(0xFFF2B8B5)
val VibrantOnError80 = Color(0xFF601410)
val VibrantErrorContainer80 = Color(0xFF8C1D18)
val VibrantOnErrorContainer80 = Color(0xFFF9DEDC)

// UI Status Colors (Harmonized with Vibrant Theme)
val PriorityHighColor = Color(0xFFFF0266) 
val PriorityMediumColor = Color(0xFF6200EE)
val PriorityLowColor = Color(0xFF03DAC6)
val StatusScheduledColor = Color(0xFF6200EE)
val StatusTriggeredColor = Color(0xFF03DAC6)
val StatusCancelledColor = Color(0xFF9E9E9E)

// ── Dark color scheme ─────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary = VibrantPrimary80,
    onPrimary = VibrantOnPrimary80,
    primaryContainer = VibrantPrimaryContainer80,
    onPrimaryContainer = VibrantOnPrimaryContainer80,
    secondary = VibrantSecondary80,
    onSecondary = VibrantOnSecondary80,
    secondaryContainer = VibrantSecondaryContainer80,
    onSecondaryContainer = VibrantOnSecondaryContainer80,
    tertiary = VibrantTertiary80,
    onTertiary = VibrantOnTertiary80,
    tertiaryContainer = VibrantTertiaryContainer80,
    onTertiaryContainer = VibrantOnTertiary80Container,
    error = VibrantError80,
    onError = VibrantOnError80,
    errorContainer = VibrantErrorContainer80,
    onErrorContainer = VibrantOnErrorContainer80,
    background = Color(0xFF121212), // Midnight Black
    surface = Color(0xFF121212),
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99)
)

// ── Light color scheme ────────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary = VibrantPrimary40,
    onPrimary = VibrantOnPrimary40,
    primaryContainer = VibrantPrimaryContainer40,
    onPrimaryContainer = VibrantOnPrimaryContainer40,
    secondary = VibrantSecondary40,
    onSecondary = VibrantOnSecondary40,
    secondaryContainer = VibrantSecondaryContainer40,
    onSecondaryContainer = VibrantOnSecondaryContainer40,
    tertiary = VibrantTertiary40,
    onTertiary = VibrantOnTertiary40,
    tertiaryContainer = VibrantTertiaryContainer40,
    onTertiaryContainer = VibrantOnTertiary40Container,
    error = VibrantError40,
    onError = VibrantOnError40,
    errorContainer = VibrantErrorContainer40,
    onErrorContainer = VibrantOnErrorContainer40,
    background = Color(0xFFFFFBFF), // Pure White
    surface = Color(0xFFFFFBFF),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EB),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E)
)

val AppTypography = Typography(
    displayLarge = TextStyle(fontSize = 57.sp, fontWeight = FontWeight.Bold),
    headlineLarge = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.SemiBold),
    headlineMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium)
)

@Composable
fun SmartNotificationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
