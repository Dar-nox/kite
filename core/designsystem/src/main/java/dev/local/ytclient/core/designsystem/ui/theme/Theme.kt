package dev.local.ytclient.core.designsystem.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Material 3 color roles mapped onto the kite palette.
 *
 * The app's own components read [LocalAppColors] directly; this scheme exists so the Material
 * components we do use (sheets, ripples, scrollbars, text fields) inherit the same palette instead
 * of Material defaults. `surfaceTint` is transparent because elevation here is expressed through
 * the three explicit surface tiers, not through Material's tinting.
 */
private val KiteColorScheme = darkColorScheme(
    primary = SurfaceInverse,
    onPrimary = TextInverse,
    primaryContainer = SurfaceControl,
    onPrimaryContainer = TextPrimary,
    inversePrimary = Background,
    secondary = SurfaceControl,
    onSecondary = ChipUnselectedText,
    secondaryContainer = SurfaceRaised,
    onSecondaryContainer = TextBody,
    tertiary = SaveGreen,
    onTertiary = SaveGreenText,
    tertiaryContainer = SaveGreenSurface,
    onTertiaryContainer = SaveGreenText,
    background = Background,
    onBackground = TextPrimary,
    surface = SurfaceRaised,
    onSurface = TextBody,
    surfaceVariant = SurfaceControl,
    onSurfaceVariant = TextSecondary,
    surfaceTint = Color.Transparent,
    surfaceContainerLowest = Background,
    surfaceContainerLow = Background,
    surfaceContainer = SurfaceRaised,
    surfaceContainerHigh = SurfaceRaised,
    surfaceContainerHighest = SurfaceControl,
    surfaceDim = Background,
    surfaceBright = SurfaceControl,
    inverseSurface = SurfaceInverse,
    inverseOnSurface = TextInverse,
    error = DangerText,
    onError = DangerSurface,
    errorContainer = DangerSurface,
    onErrorContainer = DangerText,
    outline = BorderDefault,
    outlineVariant = BorderSubtle,
    scrim = Color.Black,
)

/** Material roles mapped onto the kite type scale, so defaults never leak in at 600/700 weight. */
private val KiteMaterialTypography = Typography(
    displayLarge = KiteTypography.screenTitle,
    displayMedium = KiteTypography.screenTitle,
    displaySmall = KiteTypography.screenTitle,
    headlineLarge = KiteTypography.screenTitle,
    headlineMedium = KiteTypography.screenTitle,
    headlineSmall = KiteTypography.sectionTitle,
    titleLarge = KiteTypography.sectionTitle,
    titleMedium = KiteTypography.cardTitleLarge,
    titleSmall = KiteTypography.cardTitle,
    bodyLarge = KiteTypography.body,
    bodyMedium = KiteTypography.label,
    bodySmall = KiteTypography.meta,
    labelLarge = KiteTypography.label,
    labelMedium = KiteTypography.meta,
    labelSmall = KiteTypography.micro,
)

/** Radius ladder matching `KiteRadius`. */
private val KiteShapes = Shapes(
    extraSmall = RoundedCornerShape(KiteRadius.badge),
    small = RoundedCornerShape(KiteRadius.thumbnailGrid),
    medium = RoundedCornerShape(KiteRadius.card),
    large = RoundedCornerShape(KiteRadius.thumbnailLarge),
    extraLarge = RoundedCornerShape(KiteRadius.sheetTop),
)

/**
 * The app theme. Dark only, by design — there is no light variant and no `darkTheme` parameter.
 *
 * Wrap the whole activity content in this. It publishes the kite tokens to the composition and
 * maps them onto Material so third-party Material components still look right.
 */
@Composable
fun KiteTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalAppColors provides KiteColors,
        LocalKiteType provides KiteTypography,
        LocalReducedMotion provides rememberReducedMotion(),
    ) {
        MaterialTheme(
            colorScheme = KiteColorScheme,
            typography = KiteMaterialTypography,
            shapes = KiteShapes,
            content = content,
        )
    }
}

/** Convenience for non-Compose contexts that need a raw dp value in the 4dp scale. */
fun kiteDp(steps: Int): androidx.compose.ui.unit.Dp = (steps * 4).dp
