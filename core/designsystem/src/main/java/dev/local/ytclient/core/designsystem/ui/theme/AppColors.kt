package dev.local.ytclient.core.designsystem.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Every color the app may draw, as named in the color tables of `DESIGN.md`.
 *
 * Feature code reads these through [AppTheme.colors] and never references a [Color] literal. If a
 * value you need is missing here, add it as a token — do not inline it at the call site.
 *
 * The app is dark-only: there is deliberately no second instance of this class, and no code path
 * that picks between two.
 */
@Immutable
data class AppColors(
    // --- Surfaces ---
    /** App background. */
    val background: Color,
    /** Cards, sheets, settings rows, grouped content. */
    val surfaceRaised: Color,
    /** Unselected chips, secondary buttons. */
    val surfaceControl: Color,
    /** Selected chips, primary buttons. */
    val surfaceInverse: Color,

    // --- Borders ---
    /** Section dividers, tab bar top edge. */
    val borderSubtle: Color,
    /** Inside raised surfaces. */
    val borderDefault: Color,
    /** Unselected radio outlines, toggle tracks. */
    val borderStrong: Color,

    // --- Text ---
    /** Titles, primary values. */
    val textPrimary: Color,
    /** Body copy on raised surfaces. */
    val textBody: Color,
    /** Metadata — channel, views, dates. */
    val textSecondary: Color,
    /** Section labels, captions, helper text. This on [background] is the contrast floor. */
    val textTertiary: Color,
    /** Text drawn on [surfaceInverse]. */
    val textInverse: Color,

    // --- Accent and semantic ---
    /** Progress bars and the app mark. Nothing else. */
    val accentRed: Color,
    /** Toggle-on fill, swipe-save panel. */
    val saveGreen: Color,
    /** Text/icon on green surfaces. */
    val saveGreenText: Color,
    /** Green tag backgrounds. */
    val saveGreenSurface: Color,
    /** Selected radio fill, progress banner. */
    val infoBlue: Color,
    /** Inline actions — Undo, Show, links. */
    val infoBlueText: Color,
    /** Timestamp and chapter chips. */
    val timestampSurface: Color,
    /** Text on timestamp chips. */
    val timestampText: Color,
    /** Keyword filter chips. */
    val dangerSurface: Color,
    /** Text on danger surfaces. */
    val dangerText: Color,
    /** Folder icons, pending-action banners. */
    val warningAmber: Color,
    /** Search match highlight background. */
    val highlightSurface: Color,
    /** Search match highlight text. */
    val highlightText: Color,

    // --- Values named per component in DESIGN.md ---
    /** Text on an unselected chip. */
    val chipUnselectedText: Color,
    /** Thumb of a toggle in the off state. */
    val toggleThumbOff: Color,
    /** Duration badge scrim, `rgba(0,0,0,0.8)`. */
    val badgeScrim: Color,
    /** Track underneath a partially-watched progress bar, `rgba(255,255,255,0.25)`. */
    val progressTrack: Color,
    /** Mini-player close button background, `rgba(0,0,0,0.65)`. */
    val miniPlayerCloseScrim: Color,
) {
    /** Surface/text pair for a tag's stored `colorKey`. */
    fun tagColors(key: TagColorKey): TagColors =
        TagPalette[key] ?: TagPalette.getValue(TagColorKey.Default)

    /**
     * Progress bar color for a partially-watched item: red over a translucent track. Kept here so
     * the "red is for progress only" rule has one owner.
     */
    val progressFill: Color get() = accentRed
}

/** The single dark palette. */
val KiteColors: AppColors = AppColors(
    background = Background,
    surfaceRaised = SurfaceRaised,
    surfaceControl = SurfaceControl,
    surfaceInverse = SurfaceInverse,
    borderSubtle = BorderSubtle,
    borderDefault = BorderDefault,
    borderStrong = BorderStrong,
    textPrimary = TextPrimary,
    textBody = TextBody,
    textSecondary = TextSecondary,
    textTertiary = TextTertiary,
    textInverse = TextInverse,
    accentRed = AccentRed,
    saveGreen = SaveGreen,
    saveGreenText = SaveGreenText,
    saveGreenSurface = SaveGreenSurface,
    infoBlue = InfoBlue,
    infoBlueText = InfoBlueText,
    timestampSurface = TimestampSurface,
    timestampText = TimestampText,
    dangerSurface = DangerSurface,
    dangerText = DangerText,
    warningAmber = WarningAmber,
    highlightSurface = HighlightSurface,
    highlightText = HighlightText,
    chipUnselectedText = ChipUnselectedText,
    toggleThumbOff = ToggleThumbOff,
    badgeScrim = BadgeScrim,
    progressTrack = ProgressTrack,
    miniPlayerCloseScrim = MiniPlayerCloseScrim,
)

val LocalAppColors = staticCompositionLocalOf { KiteColors }

/** Single entry point for tokens. `AppTheme.colors.textSecondary`, `AppTheme.spacing.screenMargin`. */
object AppTheme {
    val colors: AppColors
        @Composable @ReadOnlyComposable get() = LocalAppColors.current

    val type: KiteType
        @Composable @ReadOnlyComposable get() = LocalKiteType.current

    /** True when the system has animations disabled; components cross-fade instead of translating. */
    val reducedMotion: Boolean
        @Composable @ReadOnlyComposable get() = LocalReducedMotion.current
}
