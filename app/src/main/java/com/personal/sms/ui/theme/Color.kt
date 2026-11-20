package com.personal.sms.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ---- Spec palette tokens (verbatim from the design) ----
private val Indigo = Color(0xFF5A4BE0)       // light primary / sent bubble
private val IndigoDark = Color(0xFF6457E6)   // dark primary
private val Accent = Color(0xFFB4A8FF)       // dark accent

private val LightBackground = Color(0xFFFAF8FE)
private val LightReceivedBubble = Color(0xFFEDEBF4)
private val LightSurface = Color(0xFFFFFFFF)
private val LightOnBackground = Color(0xFF1B1A24)
private val LightOnSurfaceVariant = Color(0xFF615F6E)
private val LightOutline = Color(0xFFC9C6D6)

private val DarkBackground = Color(0xFF121018)
private val DarkSurface = Color(0xFF1C1A24)
private val DarkReceivedBubble = Color(0xFF2A2735)
private val DarkOnBackground = Color(0xFFECEAF4)
private val DarkOnSurfaceVariant = Color(0xFFB6B3C4)
private val DarkOutline = Color(0xFF3A3747)

/** Fallback light scheme used below API 31 and whenever dynamic color is off. */
val LightColors = lightColorScheme(
    primary = Indigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5E1FA),
    onPrimaryContainer = Color(0xFF1A1140),
    secondary = Color(0xFF5F5B71),
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnBackground,
    surfaceVariant = LightReceivedBubble,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = Color(0xFFBA1A1A),
    onError = Color.White,
)

/** Fallback dark scheme used below API 31 and whenever dynamic color is off. */
val DarkColors = darkColorScheme(
    primary = IndigoDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2E2550),
    onPrimaryContainer = Accent,
    secondary = Color(0xFFC6C2DA),
    onSecondary = Color(0xFF2E2A3C),
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnBackground,
    surfaceVariant = DarkReceivedBubble,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

/**
 * App-specific semantic colors that don't map cleanly onto Material roles:
 * the OTP "Copy" chip, section headers, the scheduled marker, and the
 * deterministic avatar palette. Kept in a CompositionLocal so screens read
 * them the same way whether or not dynamic color is active.
 */
data class AppExtraColors(
    val otpChipContainer: Color,
    val otpChipContent: Color,
    val sectionHeader: Color,
    val scheduled: Color,
    val unreadBadge: Color,
    val onUnreadBadge: Color,
    val avatarPalette: List<Color>,
)

/** Avatar circle colors, matching the mockups (indigo, orange, green, rose, blue, grey). */
val AvatarPalette = listOf(
    Color(0xFF5A4BE0), // indigo
    Color(0xFFE08A3C), // orange
    Color(0xFF22A06B), // green
    Color(0xFFD14D6E), // rose
    Color(0xFF4C6EF0), // blue
    Color(0xFF8A8795), // grey
)

val LightExtraColors = AppExtraColors(
    otpChipContainer = Color(0xFFD6F0DD),
    otpChipContent = Color(0xFF1E7B45),
    sectionHeader = Color(0xFF6E5FE0),
    scheduled = Indigo,
    unreadBadge = Indigo,
    onUnreadBadge = Color.White,
    avatarPalette = AvatarPalette,
)

val DarkExtraColors = AppExtraColors(
    otpChipContainer = Color(0xFF1E3A2A),
    otpChipContent = Color(0xFF7FE0A0),
    sectionHeader = Accent,
    scheduled = Accent,
    unreadBadge = IndigoDark,
    onUnreadBadge = Color.White,
    avatarPalette = AvatarPalette,
)

val LocalAppExtraColors = staticCompositionLocalOf { LightExtraColors }
