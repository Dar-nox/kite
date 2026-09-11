package dev.local.ytclient.core.designsystem.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import dev.local.ytclient.core.designsystem.ui.preview.KitePreview
import dev.local.ytclient.core.designsystem.ui.theme.AppTheme
import dev.local.ytclient.core.designsystem.ui.theme.KiteMotion
import dev.local.ytclient.core.designsystem.ui.theme.KiteRadius
import dev.local.ytclient.core.designsystem.ui.theme.KiteSize
import dev.local.ytclient.core.designsystem.ui.theme.KiteSpacing

/** Pill shape: fully rounded, as used by chips, pills, and segmented segments. */
internal val PillShape = RoundedCornerShape(percent = KiteRadius.pillPercent)

/**
 * Filter chip. Fully rounded, 12dp horizontal padding, 6dp vertical.
 *
 * Selection inverts rather than going red: `surfaceInverse` fill with `textInverse` text when
 * selected, `surfaceControl` with `#EEEEEE` text when not. Red is reserved for progress.
 *
 * The visual pill is shorter than 48dp, so the clickable area is padded out to the minimum touch
 * target — the background stays small, the hit area does not.
 */
@Composable
fun AppChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: Int? = null,
) {
    val colors = AppTheme.colors
    val targetBackground = if (selected) colors.surfaceInverse else colors.surfaceControl
    val targetText = if (selected) colors.textInverse else colors.chipUnselectedText

    // 150ms ease-out. Colour is the animated property here, which is already a cross-fade, so
    // reduced motion changes nothing about this component.
    val background by animateColorAsState(
        targetValue = targetBackground,
        animationSpec = KiteMotion.tweenOut(KiteMotion.ChipSelectionMs),
        label = "chipBackground",
    )
    val textColor by animateColorAsState(
        targetValue = targetText,
        animationSpec = KiteMotion.tweenOut(KiteMotion.ChipSelectionMs),
        label = "chipText",
    )

    Box(
        modifier = modifier
            .heightIn(min = KiteSize.touchTarget)
            .clip(PillShape)
            .clickable(
                onClick = onClick,
                role = Role.Checkbox,
                onClickLabel = if (selected) "Remove $label filter" else "Apply $label filter",
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .background(background, PillShape)
                .padding(
                    horizontal = KiteSpacing.chipHorizontal,
                    vertical = KiteSpacing.chipVertical,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(text = label, style = AppTheme.type.micro, color = textColor)
            if (count != null) {
                Spacer(Modifier.width(KiteSpacing.space4))
                Text(text = count.toString(), style = AppTheme.type.micro, color = textColor)
            }
        }
    }
}

@Preview
@Composable
private fun AppChipPreview() {
    KitePreview {
        Row(horizontalArrangement = Arrangement.spacedBy(KiteSpacing.space8)) {
            AppChip(label = "All", selected = true, onClick = {})
            AppChip(label = "Unwatched", selected = false, onClick = {}, count = 12)
            AppChip(label = "Under 10m", selected = false, onClick = {})
        }
    }
}
