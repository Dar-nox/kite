package dev.local.ytclient.core.designsystem.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import dev.local.ytclient.core.designsystem.ui.model.TagChipData
import dev.local.ytclient.core.designsystem.ui.model.VideoCardData
import dev.local.ytclient.core.designsystem.ui.preview.KitePreview
import dev.local.ytclient.core.designsystem.ui.preview.sampleTags
import dev.local.ytclient.core.designsystem.ui.preview.sampleVideoCard
import dev.local.ytclient.core.designsystem.ui.theme.AppTheme
import dev.local.ytclient.core.designsystem.ui.theme.KiteRadius
import dev.local.ytclient.core.designsystem.ui.theme.KiteSize
import dev.local.ytclient.core.designsystem.ui.theme.KiteSpacing

/**
 * Video card — compact. 88x50dp thumbnail on the left, text on the right, about five per screen.
 *
 * No avatar at this size: `DESIGN.md` calls it noise. Tags render only when the row has any, which
 * is how the same component serves both the compact feed and library rows.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoCardCompact(
    data: VideoCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
) {
    val colors = AppTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (data.dimmed) DimmedCardAlpha else 1f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                onClickLabel = "Open ${data.title}",
            ),
        verticalAlignment = Alignment.Top,
    ) {
        VideoThumbnail(
            thumbnailUrl = data.thumbnailUrl,
            durationSec = data.durationSec,
            showProgress = data.showProgress,
            progress = data.progress,
            radius = KiteRadius.thumbnailGrid,
            width = KiteSize.compactThumbWidth,
            height = KiteSize.compactThumbHeight,
        )

        Spacer(Modifier.width(KiteSpacing.cardToText))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = data.title,
                style = AppTheme.type.cardTitle,
                color = colors.textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (data.metaLine != null) {
                Spacer(Modifier.height(KiteSpacing.space2))
                Text(
                    text = data.metaLine,
                    style = AppTheme.type.meta,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (data.tags.isNotEmpty()) {
                Spacer(Modifier.height(KiteSpacing.space4))
                TagRow(tags = data.tags)
            }
        }
    }
}

/** Tag chips in a row, wrapping to a second line only when they have to. */
@Composable
fun TagRow(tags: List<TagChipData>, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(KiteSpacing.space4),
    ) {
        tags.forEach { tag -> TagChip(tag = tag) }
    }
}

@Preview
@Composable
private fun VideoCardCompactPreview() {
    KitePreview {
        Column(verticalArrangement = Arrangement.spacedBy(KiteSpacing.feedItemGapCompact)) {
            VideoCardCompact(data = sampleVideoCard, onClick = {})
            VideoCardCompact(
                data = sampleVideoCard.copy(title = "Saved row", tags = sampleTags),
                onClick = {},
            )
        }
    }
}
