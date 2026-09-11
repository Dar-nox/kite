package dev.local.ytclient.core.designsystem.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import dev.local.ytclient.core.designsystem.ui.preview.KitePreview
import dev.local.ytclient.core.designsystem.ui.theme.AppTheme
import dev.local.ytclient.core.designsystem.ui.theme.KiteMotion
import dev.local.ytclient.core.designsystem.ui.theme.KiteSize

/** Alpha a disabled control renders at; it is dimmed, never hidden. */
internal const val DisabledAlpha = 0.5f

/**
 * Toggle. 34x19dp track, 15dp thumb, 2dp inset.
 *
 * On is `saveGreen` with the thumb right and white; off is `borderStrong` with the `#8A8A8E` thumb
 * left. Green is the "saved / on" family throughout the app, so a toggle never reaches for the
 * primary or accent color.
 *
 * The track is 19dp tall, so the toggleable area is padded out to the 48dp minimum target while the
 * drawn track keeps its specified size.
 *
 * Under reduced motion the thumb snaps instead of sliding while the track still changes color —
 * the state change stays visible, the translation is what gets dropped.
 *
 * The row hosting a toggle supplies the label, so there is no `description` parameter here: passing
 * one would make TalkBack read the setting twice.
 */
@Composable
fun AppToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = AppTheme.colors
    val reducedMotion = AppTheme.reducedMotion

    val trackColor by animateColorAsState(
        targetValue = if (checked) colors.saveGreen else colors.borderStrong,
        animationSpec = KiteMotion.tweenOut(KiteMotion.ChipSelectionMs),
        label = "toggleTrack",
    )
    val thumbColor by animateColorAsState(
        targetValue = if (checked) colors.textPrimary else colors.toggleThumbOff,
        animationSpec = KiteMotion.tweenOut(KiteMotion.ChipSelectionMs),
        label = "toggleThumb",
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) ThumbOnOffset else KiteSize.toggleInset,
        animationSpec = if (reducedMotion) snap() else KiteMotion.tweenOut(KiteMotion.ChipSelectionMs),
        label = "toggleThumbOffset",
    )

    Box(
        modifier = modifier
            .heightIn(min = KiteSize.touchTarget)
            .width(KiteSize.toggleTrackWidth)
            .alpha(if (enabled) 1f else DisabledAlpha)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                // Role.Switch plus the value is what makes TalkBack say "on"/"off"; there is no
                // valueDescription parameter to set, and no need for one.
                onValueChange = onCheckedChange,
            ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .width(KiteSize.toggleTrackWidth)
                .height(KiteSize.toggleTrackHeight)
                .clip(CircleShape)
                .background(trackColor),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(KiteSize.toggleThumb)
                    .clip(CircleShape)
                    .background(thumbColor),
            )
        }
    }
}

/** Thumb x-offset in the on position: track width - thumb - inset. */
private val ThumbOnOffset: Dp =
    KiteSize.toggleTrackWidth - KiteSize.toggleThumb - KiteSize.toggleInset

@Preview
@Composable
private fun AppTogglePreview() {
    KitePreview {
        AppToggle(checked = true, onCheckedChange = {})
    }
}
