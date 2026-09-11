package dev.local.ytclient.core.designsystem.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import dev.local.ytclient.core.designsystem.ui.preview.KitePreview
import dev.local.ytclient.core.designsystem.ui.theme.AppTheme
import dev.local.ytclient.core.designsystem.ui.theme.KiteSize

/**
 * Radio. 17dp. Selected is `infoBlue` fill with a white check; unselected is a 1.5dp `borderStrong`
 * outline on transparent.
 *
 * The blue fill is the only place the info family is used as a solid, and it is paired with a check
 * glyph so state never rests on color alone.
 */
@Composable
fun AppRadio(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = AppTheme.colors

    Box(
        modifier = modifier
            .size(KiteSize.touchTarget)
            .alpha(if (enabled) 1f else DisabledAlpha)
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(KiteSize.radio)
                    .clip(CircleShape)
                    .background(colors.infoBlue),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = colors.textPrimary,
                    modifier = Modifier.size(KiteSize.radio * 0.7f),
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(KiteSize.radio)
                    .clip(CircleShape)
                    .border(KiteSize.radioOutline, colors.borderStrong, CircleShape),
            )
        }
    }
}

@Preview
@Composable
private fun AppRadioPreview() {
    KitePreview {
        AppRadio(selected = true, onClick = {})
    }
}
