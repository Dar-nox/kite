package dev.local.ytclient.core.designsystem.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import dev.local.ytclient.core.designsystem.ui.preview.KitePreview
import dev.local.ytclient.core.designsystem.ui.theme.AppTheme
import dev.local.ytclient.core.designsystem.ui.theme.KiteSize
import dev.local.ytclient.core.designsystem.ui.theme.KiteSpacing

/**
 * Stepper: a pill on `background` that sits inside a raised settings row. Minus, value, plus.
 *
 * A bound that is reached renders at `textTertiary` rather than disappearing — the control stays
 * put, so the row does not reflow when you hit the limit.
 */
@Composable
fun AppStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    suffix: String? = null,
) {
    val colors = AppTheme.colors
    val canDecrement = enabled && value > range.first
    val canIncrement = enabled && value < range.last

    Row(
        modifier = modifier
            .clip(PillShape)
            .background(colors.background)
            .padding(horizontal = KiteSpacing.space4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperButton(
            onClick = { onValueChange(value - 1) },
            enabled = canDecrement,
            description = "Decrease",
            icon = Icons.Rounded.Remove,
        )

        Text(
            text = if (suffix != null) "$value $suffix" else value.toString(),
            style = AppTheme.type.label,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(min = KiteSize.actionIcon)
                .padding(horizontal = KiteSpacing.space4),
        )

        StepperButton(
            onClick = { onValueChange(value + 1) },
            enabled = canIncrement,
            description = "Increase",
            icon = Icons.Rounded.Add,
        )
    }
}

@Composable
private fun StepperButton(
    onClick: () -> Unit,
    enabled: Boolean,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .size(KiteSize.touchTarget)
            .clip(CircleShape)
            .alpha(if (enabled) 1f else DisabledAlpha)
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) colors.textBody else colors.textTertiary,
            modifier = Modifier.size(KiteSize.actionIcon * 0.6f),
        )
    }
}

@Preview
@Composable
private fun AppStepperPreview() {
    KitePreview {
        AppStepper(value = 3, onValueChange = {}, range = 2..5)
    }
}
