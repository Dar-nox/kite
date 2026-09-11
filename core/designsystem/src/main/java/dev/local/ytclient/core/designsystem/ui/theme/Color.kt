package dev.local.ytclient.core.designsystem.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Raw hex values from `DESIGN.md`.
 *
 * Nothing outside this file and [AppColors] should reference these constants, and feature code must
 * never reference them at all — it reads tokens through [LocalAppColors] instead. Adding a value
 * here without exposing it as a token in [AppColors] is the wrong direction; add the token too.
 */

// --- Surfaces ---
internal val Background = Color(0xFF0F0F0F)
internal val SurfaceRaised = Color(0xFF1C1C1F)
internal val SurfaceControl = Color(0xFF262626)
internal val SurfaceInverse = Color(0xFFFFFFFF)

// --- Borders ---
internal val BorderSubtle = Color(0xFF1F1F22)
internal val BorderDefault = Color(0xFF2A2A2E)
internal val BorderStrong = Color(0xFF3A3A3D)

// --- Text ---
internal val TextPrimary = Color(0xFFFFFFFF)
internal val TextBody = Color(0xFFDDDDDD)
internal val TextSecondary = Color(0xFF9A9A9A)
internal val TextTertiary = Color(0xFF7A7A7A)
internal val TextInverse = Color(0xFF0F0F0F)

// --- Accent and semantic ---
internal val AccentRed = Color(0xFFFF0033)
internal val SaveGreen = Color(0xFF1D9E75)
internal val SaveGreenText = Color(0xFF9FE1CB)
internal val SaveGreenSurface = Color(0xFF1F3A2E)
internal val InfoBlue = Color(0xFF378ADD)
internal val InfoBlueText = Color(0xFF78A9FF)
internal val TimestampSurface = Color(0xFF2E2438)
internal val TimestampText = Color(0xFFCECBF6)
internal val DangerSurface = Color(0xFF3A2424)
internal val DangerText = Color(0xFFF7C1C1)
internal val WarningAmber = Color(0xFFFAC775)
internal val HighlightSurface = Color(0xFF4A3F1A)
internal val HighlightText = Color(0xFFFAC775)

// --- Component-local values named by DESIGN.md but not part of the token tables ---

/** Text on an unselected chip ("Chip" component). */
internal val ChipUnselectedText = Color(0xFFEEEEEE)

/** Thumb of a toggle in the off state ("Toggle" component). */
internal val ToggleThumbOff = Color(0xFF8A8A8E)

/** Duration badge scrim: `rgba(0,0,0,0.8)`. */
internal val BadgeScrim = Color(0xCC000000)

/** Track underneath a partially-watched progress bar: `rgba(255,255,255,0.25)`. */
internal val ProgressTrack = Color(0x40FFFFFF)

/** Mini-player close button background: `rgba(0,0,0,0.65)`. */
internal val MiniPlayerCloseScrim = Color(0xA6000000)
