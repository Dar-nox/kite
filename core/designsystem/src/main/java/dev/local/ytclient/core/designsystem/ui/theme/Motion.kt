package dev.local.ytclient.core.designsystem.ui.theme

import android.provider.Settings
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/** Durations and curves from the motion table in `DESIGN.md`. */
object KiteMotion {
    const val ChipSelectionMs = 150
    const val SheetMs = 250
    const val DescriptionExpandMs = 200
    const val SwipeCommitMs = 200
    const val SwipeSpringBackMs = 150
    const val MiniPlayerSnapMs = 250
    const val LayoutChangeMs = 300

    /** Ease-out is the default curve for every discrete selection or reveal. */
    val EaseOutSpec = EaseOut

    /** Layout changes are the one ease-in-out case. */
    val EaseInOutSpec = EaseInOut

    val Linear = LinearEasing

    fun <T> tweenOut(durationMs: Int, delayMs: Int = 0): TweenSpec<T> =
        tween(durationMillis = durationMs, delayMillis = delayMs, easing = EaseOut)

    fun <T> tweenInOut(durationMs: Int, delayMs: Int = 0): TweenSpec<T> =
        tween(durationMillis = durationMs, delayMillis = delayMs, easing = EaseInOut)
}

/**
 * Whether the user has turned system animations off.
 *
 * `DESIGN.md` requires reduced motion to cross-fade instead of translating, keep the same
 * durations, and never remove feedback entirely — so components read this flag and swap the
 * animated *property*, they do not zero the duration.
 */
val LocalReducedMotion = staticCompositionLocalOf { false }

/**
 * Reads the system animator scale. 0 means animations are off.
 *
 * Read once per theme entry rather than observed: this changes only through system settings, which
 * recreate activities anyway.
 */
@Composable
fun rememberReducedMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    val scale = Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    return scale == 0f
}
