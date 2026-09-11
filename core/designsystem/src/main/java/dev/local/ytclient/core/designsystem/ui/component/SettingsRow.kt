package dev.local.ytclient.core.designsystem.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import dev.local.ytclient.core.designsystem.ui.preview.KitePreview
import dev.local.ytclient.core.designsystem.ui.theme.AppTheme
import dev.local.ytclient.core.designsystem.ui.theme.KiteRadius
import dev.local.ytclient.core.designsystem.ui.theme.KiteSize
import dev.local.ytclient.core.designsystem.ui.theme.KiteSpacing

/**
 * `textTertiary` section label that heads a group of settings rows. Sentence case, with the 10dp gap
 * to the content it labels.
 */
@Composable
fun SettingsSectionLabel(text: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = text,
            style = AppTheme.type.micro,
            color = AppTheme.colors.textTertiary,
        )
        Spacer(Modifier.height(KiteSpacing.sectionLabelToContent))
    }
}

/**
 * The raised surface a run of related settings rows shares.
 *
 * Consecutive rows inside one group share this surface and are separated by dividers; unrelated
 * rows go in separate groups with [KiteSpacing.surfaceGap] between them. Rows after the first pass
 * `showDivider = true` — kept explicit rather than inferred from position so a group can be
 * reordered without dividers ending up in the wrong place.
 */
@Composable
fun SettingsGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(KiteRadius.card))
            .background(AppTheme.colors.surfaceRaised),
        content = content,
    )
}

/**
 * Settings row: `surfaceRaised`, 14dp radius, 14dp padding, 16dp leading icon in `textBody`, label
 * in the `label` token, and a trailing control — toggle, value, chevron, or stepper.
 *
 * [helper] renders beneath the label at `micro` in `textTertiary`.
 */
@Composable
fun SettingsRow(
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    helper: String? = null,
    showDivider: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = AppTheme.colors

    Column(modifier = modifier.fillMaxWidth()) {
        if (showDivider) {
            HorizontalDivider(thickness = KiteSize.hairline, color = colors.borderSubtle)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) {
                        Modifier.clickable(enabled = enabled, onClick = onClick)
                    } else {
                        Modifier
                    },
                )
                .padding(KiteSpacing.insideSurface),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.textBody,
                    modifier = Modifier.size(KiteSize.leadingIcon),
                )
                Spacer(Modifier.width(KiteSpacing.space12))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(KiteSpacing.space2),
            ) {
                Text(
                    text = label,
                    style = AppTheme.type.label,
                    color = colors.textBody,
                )
                if (helper != null) {
                    Text(
                        text = helper,
                        style = AppTheme.type.micro,
                        color = colors.textTertiary,
                    )
                }
            }

            if (trailing != null) {
                Spacer(Modifier.width(KiteSpacing.space12))
                Box(contentAlignment = Alignment.CenterEnd) { trailing() }
            }
        }
    }
}

/** Row with a toggle on the right. */
@Composable
fun SettingsToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    helper: String? = null,
    showDivider: Boolean = false,
    enabled: Boolean = true,
) {
    SettingsRow(
        label = label,
        modifier = modifier,
        icon = icon,
        helper = helper,
        showDivider = showDivider,
        enabled = enabled,
        // Tapping anywhere on the row flips the setting; the toggle is a visual, not the only target.
        onClick = if (enabled) ({ onCheckedChange(!checked) }) else null,
        trailing = {
            AppToggle(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
            )
        },
    )
}

/** Row that shows a current value and opens a picker. */
@Composable
fun SettingsValueRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    helper: String? = null,
    showDivider: Boolean = false,
) {
    SettingsRow(
        label = label,
        modifier = modifier,
        icon = icon,
        helper = helper,
        showDivider = showDivider,
        onClick = onClick,
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = value,
                    style = AppTheme.type.label,
                    color = AppTheme.colors.textSecondary,
                )
                Spacer(Modifier.width(KiteSpacing.space4))
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = AppTheme.colors.textTertiary,
                    modifier = Modifier.size(KiteSize.leadingIcon),
                )
            }
        },
    )
}

/** Row that navigates to another screen. */
@Composable
fun SettingsNavigationRow(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    helper: String? = null,
    showDivider: Boolean = false,
) {
    SettingsRow(
        label = label,
        modifier = modifier,
        icon = icon,
        helper = helper,
        showDivider = showDivider,
        onClick = onClick,
        trailing = {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Open $label",
                tint = AppTheme.colors.textTertiary,
                modifier = Modifier.size(KiteSize.leadingIcon),
            )
        },
    )
}

/** Row with an inline stepper, used for grid columns and grace windows. */
@Composable
fun SettingsStepperRow(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    helper: String? = null,
    showDivider: Boolean = false,
    enabled: Boolean = true,
    suffix: String? = null,
) {
    SettingsRow(
        label = label,
        modifier = modifier,
        icon = icon,
        helper = helper,
        showDivider = showDivider,
        enabled = enabled,
        trailing = {
            AppStepper(
                value = value,
                onValueChange = onValueChange,
                range = range,
                enabled = enabled,
                suffix = suffix,
            )
        },
    )
}

@Preview
@Composable
private fun SettingsRowPreview() {
    KitePreview {
        Column {
            SettingsSectionLabel(text = "Playback")
            SettingsGroup {
                SettingsToggleRow(
                    label = "Background playback",
                    checked = true,
                    onCheckedChange = {},
                )
                SettingsValueRow(
                    label = "Quality on wi-fi",
                    value = "1080p",
                    onClick = {},
                    showDivider = true,
                )
                SettingsStepperRow(
                    label = "Auto-remove grace window",
                    value = 24,
                    onValueChange = {},
                    range = 1..72,
                    showDivider = true,
                    suffix = "h",
                )
            }
        }
    }
}
