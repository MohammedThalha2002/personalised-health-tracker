package com.example.personalisedtracker.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Fitness-focused Material 3 palette. Hand-tuned (not dynamic) so the brand
 * stays consistent across light/dark and devices without Monet support.
 * Greens evoke health/progress, indigo secondary reads well on charts,
 * amber tertiary highlights streaks and warnings.
 */

// --- Light ---
val md_light_primary = Color(0xFF1F6E3A)
val md_light_onPrimary = Color(0xFFFFFFFF)
val md_light_primaryContainer = Color(0xFFA7F3B6)
val md_light_onPrimaryContainer = Color(0xFF002110)

val md_light_secondary = Color(0xFF3D5A99)
val md_light_onSecondary = Color(0xFFFFFFFF)
val md_light_secondaryContainer = Color(0xFFDAE2FF)
val md_light_onSecondaryContainer = Color(0xFF001847)

val md_light_tertiary = Color(0xFF8B5000)
val md_light_onTertiary = Color(0xFFFFFFFF)
val md_light_tertiaryContainer = Color(0xFFFFDDB6)
val md_light_onTertiaryContainer = Color(0xFF2C1600)

val md_light_error = Color(0xFFBA1A1A)
val md_light_onError = Color(0xFFFFFFFF)
val md_light_errorContainer = Color(0xFFFFDAD6)
val md_light_onErrorContainer = Color(0xFF410002)

val md_light_background = Color(0xFFF8FBF5)
val md_light_onBackground = Color(0xFF191D17)
val md_light_surface = Color(0xFFF8FBF5)
val md_light_onSurface = Color(0xFF191D17)
val md_light_surfaceVariant = Color(0xFFDDE5DA)
val md_light_onSurfaceVariant = Color(0xFF424940)
val md_light_outline = Color(0xFF727970)
val md_light_outlineVariant = Color(0xFFC1C9BF)

// --- Dark ---
val md_dark_primary = Color(0xFF8CD69C)
val md_dark_onPrimary = Color(0xFF00391E)
val md_dark_primaryContainer = Color(0xFF00532D)
val md_dark_onPrimaryContainer = Color(0xFFA7F3B6)

val md_dark_secondary = Color(0xFFB3C5FF)
val md_dark_onSecondary = Color(0xFF062D72)
val md_dark_secondaryContainer = Color(0xFF234380)
val md_dark_onSecondaryContainer = Color(0xFFDAE2FF)

val md_dark_tertiary = Color(0xFFFFB870)
val md_dark_onTertiary = Color(0xFF4A2700)
val md_dark_tertiaryContainer = Color(0xFF6A3B00)
val md_dark_onTertiaryContainer = Color(0xFFFFDDB6)

val md_dark_error = Color(0xFFFFB4AB)
val md_dark_onError = Color(0xFF690005)
val md_dark_errorContainer = Color(0xFF93000A)
val md_dark_onErrorContainer = Color(0xFFFFDAD6)

val md_dark_background = Color(0xFF11140F)
val md_dark_onBackground = Color(0xFFE1E4DC)
val md_dark_surface = Color(0xFF11140F)
val md_dark_onSurface = Color(0xFFE1E4DC)
val md_dark_surfaceVariant = Color(0xFF424940)
val md_dark_onSurfaceVariant = Color(0xFFC1C9BF)
val md_dark_outline = Color(0xFF8B938A)
val md_dark_outlineVariant = Color(0xFF424940)

// Kept for backwards compat with any legacy references — not used by Theme.kt.
@Deprecated("Use MaterialTheme.colorScheme.primary") val Purple80 = md_dark_primary
@Deprecated("Use MaterialTheme.colorScheme.secondary") val PurpleGrey80 = md_dark_secondary
@Deprecated("Use MaterialTheme.colorScheme.tertiary") val Pink80 = md_dark_tertiary
@Deprecated("Use MaterialTheme.colorScheme.primary") val Purple40 = md_light_primary
@Deprecated("Use MaterialTheme.colorScheme.secondary") val PurpleGrey40 = md_light_secondary
@Deprecated("Use MaterialTheme.colorScheme.tertiary") val Pink40 = md_light_tertiary