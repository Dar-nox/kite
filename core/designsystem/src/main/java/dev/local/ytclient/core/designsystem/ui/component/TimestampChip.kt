package dev.local.ytclient.core.designsystem.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import dev.local.ytclient.core.designsystem.ui.preview.KitePreview
import dev.local.ytclient.core.designsystem.ui.theme.AppTheme
import dev.local.ytclient.core.designsystem.ui.theme.KiteRadius
import dev.local.ytclient.core.designsystem.ui.theme.KiteSpacing
import dev.local.ytclient.core.designsystem.util.KiteFormat

/**
 * Timestamp chip: `timestampSurface` background, `timestampText` text, `micro`, 6dp radius.
 *
 * Used for chapters, note timestamps, and timestamps lifted out of description body text.
 *
 * Deliberately not clickable on its own. In every place the spec uses it the chip sits inside a
 * larger tappable row (a chapter line, a note row), and giving the chip its own 48dp hit area
 * inside that row would fight the row's target. Wire the seek to the row and keep this as a label.
 */
@Composable
fun TimestampChip(
    seconds: Int,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val colors = AppTheme.colors
    Text(
        text = label ?: KiteFormat.duration(seconds),
        style = AppTheme.type.micro,
        color = colors.timestampText,
        modifier = modifier
            .clip(RoundedCornerShape(KiteRadius.badge))
            .background(colors.timestampSurface)
            .padding(horizontal = KiteSpacing.space8, vertical = KiteSpacing.space2),
    )
}

@Preview
@Composable
private fun TimestampChipPreview() {
    KitePreview {
        TimestampChip(seconds = 372)
    }
}
