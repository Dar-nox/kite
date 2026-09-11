package dev.local.ytclient.core.designsystem.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import dev.local.ytclient.core.designsystem.ui.model.VideoCardData
import dev.local.ytclient.core.designsystem.ui.preview.KitePreview
import dev.local.ytclient.core.designsystem.ui.preview.sampleVideoCard
import dev.local.ytclient.core.designsystem.ui.theme.AppTheme
import dev.local.ytclient.core.designsystem.ui.theme.KiteRadius
import dev.local.ytclient.core.designsystem.ui.theme.KiteSize
import dev.local.ytclient.core.designsystem.ui.theme.KiteSpacing

/** Opacity a fully watched card renders at, per `FEATURES.md`. */
internal const val DimmedCardAlpha = 0.6f

/**
 * Video card — large. One per row, the biggest thumbnails, roughly two per screen.
 *
 * Structure per `DESIGN.md`: 16:9 thumbnail at 16dp radius, then 10dp of air, then a 28dp channel
 * avatar beside a two-line title and one metadata line. Fully watched cards drop to 60% opacity;
 * the Unwatched filter removes them upstream instead, so both paths can't fight over the same row.
 *
 * @param onLongClick opens the context sheet. It is optional only so previews stay simple — every
 *   real call site passes one, since long press is the non-gesture equivalent of the swipe actions.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoCardLarge(
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
            radius = KiteRadius.thumbnailLarge,
            contentDescription = null,
        )

        Spacer(Modifier.height(KiteSpacing.cardToText))

        Row(verticalAlignment = Alignment.Top) {
            ChannelAvatar(url = data.channelAvatarUrl, name = data.channelName)

            Spacer(Modifier.width(KiteSpacing.cardToText))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = data.title,
                    style = AppTheme.type.cardTitleLarge,
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
    }
}

/** 28dp circular channel avatar. Only the large card shows one; smaller variants treat it as noise. */
@Composable
internal fun ChannelAvatar(url: String?, name: String?, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .size(KiteSize.avatarLargeCard)
            .clip(CircleShape)
            .background(colors.surfaceControl),
        contentAlignment = Alignment.Center,
    ) {
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = name?.let { "$it avatar" },
                placeholder = ColorPainter(colors.surfaceControl),
                error = ColorPainter(colors.surfaceControl),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                text = name?.take(1)?.uppercase().orEmpty(),
                style = AppTheme.type.meta,
                color = colors.textSecondary,
            )
        }
    }
}

@Preview
@Composable
private fun VideoCardLargePreview() {
    KitePreview {
        Column(verticalArrangement = Arrangement.spacedBy(KiteSpacing.feedItemGapLarge)) {
            VideoCardLarge(data = sampleVideoCard, onClick = {})
            VideoCardLarge(data = sampleVideoCard.copy(progress = 1f, dimmed = true), onClick = {})
        }
    }
}
