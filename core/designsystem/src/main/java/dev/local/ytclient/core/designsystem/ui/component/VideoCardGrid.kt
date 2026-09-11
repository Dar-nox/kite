package dev.local.ytclient.core.designsystem.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import dev.local.ytclient.core.designsystem.ui.model.VideoCardData
import dev.local.ytclient.core.designsystem.ui.preview.KitePreview
import dev.local.ytclient.core.designsystem.ui.preview.sampleVideoCard
import dev.local.ytclient.core.designsystem.ui.theme.AppTheme
import dev.local.ytclient.core.designsystem.ui.theme.KiteRadius
import dev.local.ytclient.core.designsystem.ui.theme.KiteSpacing

/**
 * Video card — grid tile. Thumbnail fills the column width, 12dp radius, title clamped at two
 * lines, channel and view count as text only.
 *
 * Two lines is a hard requirement: one line truncates most real titles into uselessness. The tile
 * shows no avatar, matching the compact row's reasoning.
 *
 * The tile fills its parent's width, so the grid arranges tiles and passes the column count down
 * from [dev.local.ytclient.core.designsystem.util.GridColumns] rather than the tile computing it.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoCardGrid(
    data: VideoCardData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
) {
    val colors = AppTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (data.dimmed) DimmedCardAlpha else 1f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                onClickLabel = "Open ${data.title}",
            ),
    ) {
        VideoThumbnail(
            thumbnailUrl = data.thumbnailUrl,
            durationSec = data.durationSec,
            showProgress = data.showProgress,
            progress = data.progress,
            radius = KiteRadius.thumbnailGrid,
        )

        Spacer(Modifier.height(KiteSpacing.cardToText))

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
    }
}

@Preview
@Composable
private fun VideoCardGridPreview() {
    KitePreview {
        Column {
            VideoCardGrid(data = sampleVideoCard, onClick = {})
            Spacer(Modifier.height(KiteSpacing.feedItemGapGridRow))
            VideoCardGrid(data = sampleVideoCard.copy(title = "A short title"), onClick = {})
        }
    }
}
