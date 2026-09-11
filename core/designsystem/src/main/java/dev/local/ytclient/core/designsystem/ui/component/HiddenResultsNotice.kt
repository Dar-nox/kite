package dev.local.ytclient.core.designsystem.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import dev.local.ytclient.core.designsystem.ui.preview.KitePreview
import dev.local.ytclient.core.designsystem.ui.theme.AppTheme
import dev.local.ytclient.core.designsystem.ui.theme.KiteRadius
import dev.local.ytclient.core.designsystem.ui.theme.KiteSize
import dev.local.ytclient.core.designsystem.ui.theme.KiteSpacing

/**
 * "N results hidden — Show".
 *
 * `CLAUDE.md` makes this mandatory rather than decorative: when blocked channels, Shorts
 * containment, or keyword filters remove items, the list has to say so. A list that looks empty for
 * an unexplained reason reads as a bug.
 *
 * Tapping [onShow] reveals the hidden items inline for the session only — it does not change any
 * setting, so the next visit is filtered again.
 */
@Composable
fun HiddenResultsNotice(
    hiddenCount: Int,
    onShow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(KiteRadius.banner))
            .background(colors.surfaceRaised)
            .padding(KiteSpacing.insideSurface),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        Icon(
            imageVector = Icons.Rounded.VisibilityOff,
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.size(KiteSize.leadingIcon),
        )
        Spacer(Modifier.width(KiteSpacing.space12))
        Text(
            text = if (hiddenCount == 1) "1 result hidden" else "$hiddenCount results hidden",
            style = AppTheme.type.label,
            color = colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "Show",
            style = AppTheme.type.label,
            color = colors.infoBlueText,
            modifier = Modifier
                .clickable(onClickLabel = "Show $hiddenCount hidden results", onClick = onShow)
                .padding(horizontal = KiteSpacing.space8, vertical = KiteSpacing.space4),
        )
    }
}

@Preview
@Composable
private fun HiddenResultsNoticePreview() {
    KitePreview {
        HiddenResultsNotice(hiddenCount = 12, onShow = {})
    }
}
