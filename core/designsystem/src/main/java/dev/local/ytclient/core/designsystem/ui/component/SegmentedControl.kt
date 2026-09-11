package dev.local.ytclient.core.designsystem.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import dev.local.ytclient.core.designsystem.ui.preview.KitePreview
import dev.local.ytclient.core.designsystem.ui.theme.AppTheme
import dev.local.ytclient.core.designsystem.ui.theme.KiteMotion
import dev.local.ytclient.core.designsystem.ui.theme.KiteSize
import dev.local.ytclient.core.designsystem.ui.theme.KiteSpacing

/** One entry in a segmented control. */
@Immutable
data class SegmentOption<T>(
    val value: T,
    val label: String,
)

/**
 * Segmented control. Full width, `surfaceRaised` background, 3dp inner padding, fully rounded, with
 * segments split evenly.
 *
 * Used for the watch screen's two tabs, which stay pinned beneath the player — the point of the
 * control is that switching is always one tap no matter how deep the list below has scrolled.
 *
 * The selected segment inverts to `surfaceInverse` with `textInverse` text, matching the chip
 * treatment so selection reads the same everywhere in the app.
 */
@Composable
fun <T> SegmentedControl(
    options: List<SegmentOption<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surfaceRaised, PillShape)
            .padding(KiteSpacing.segmentInset),
        horizontalArrangement = Arrangement.spacedBy(KiteSpacing.segmentInset),
    ) {
        options.forEach { option ->
            val isSelected = option.value == selected
            val segmentBackground by animateColorAsState(
                targetValue = if (isSelected) colors.surfaceInverse else colors.surfaceRaised,
                animationSpec = KiteMotion.tweenOut(KiteMotion.ChipSelectionMs),
                label = "segmentBackground",
            )
            val segmentText by animateColorAsState(
                targetValue = if (isSelected) colors.textInverse else colors.textSecondary,
                animationSpec = KiteMotion.tweenOut(KiteMotion.ChipSelectionMs),
                label = "segmentText",
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(KiteSize.segmentHeight)
                    .clip(PillShape)
                    .background(segmentBackground, PillShape)
                    // selectable() carries the tab role and the selected state into semantics, so
                    // the segment is announced correctly without hand-writing either.
                    .selectable(
                        selected = isSelected,
                        role = Role.Tab,
                        onClick = { onSelect(option.value) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = option.label,
                    style = AppTheme.type.label,
                    color = segmentText,
                )
            }
        }
    }
}

@Preview
@Composable
private fun SegmentedControlPreview() {
    KitePreview {
        SegmentedControl(
            options = listOf(
                SegmentOption(value = "up_next", label = "Up next"),
                SegmentOption(value = "comments", label = "Comments"),
            ),
            selected = "up_next",
            onSelect = {},
        )
    }
}
