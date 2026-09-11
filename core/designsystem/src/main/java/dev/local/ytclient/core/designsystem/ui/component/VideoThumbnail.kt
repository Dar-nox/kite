package dev.local.ytclient.core.designsystem.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import dev.local.ytclient.core.designsystem.ui.theme.AppTheme
import dev.local.ytclient.core.designsystem.ui.theme.KiteAspect
import dev.local.ytclient.core.designsystem.ui.theme.KiteRadius
import dev.local.ytclient.core.designsystem.ui.theme.KiteSize
import dev.local.ytclient.core.designsystem.ui.theme.KiteSpacing
import dev.local.ytclient.core.designsystem.util.KiteFormat

/**
 * The thumbnail every card variant shares: image, duration badge, and the partially-watched
 * progress bar.
 *
 * The bar sits along the bottom edge of the thumbnail rather than below it, is red on a
 * translucent track, and only appears for a partially watched item — an unstarted or finished video
 * gets no bar at all, which is what distinguishes "60% through" from "watched".
 *
 * @param width when null the thumbnail fills its parent's width and derives height from
 *   [aspectRatio]; compact rows pass an explicit size instead.
 */
@Composable
fun VideoThumbnail(
    thumbnailUrl: String?,
    durationSec: Int,
    modifier: Modifier = Modifier,
    showProgress: Boolean = false,
    progress: Float = 0f,
    radius: Dp = KiteRadius.thumbnailLarge,
    aspectRatio: Float = KiteAspect.Video,
    width: Dp? = null,
    height: Dp? = null,
    contentDescription: String? = null,
) {
    val colors = AppTheme.colors

    val sizeModifier = if (width != null && height != null) {
        Modifier.width(width).height(height)
    } else {
        Modifier.fillMaxWidth().aspectRatio(aspectRatio)
    }

    Box(
        modifier = modifier
            .then(sizeModifier)
            .clip(RoundedCornerShape(radius))
            .background(colors.surfaceControl),
    ) {
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = contentDescription,
            placeholder = ColorPainter(colors.surfaceControl),
            error = ColorPainter(colors.surfaceControl),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
        )

        DurationBadge(durationSec = durationSec)

        if (showProgress) {
            WatchedProgressBar(progress = progress)
        }
    }
}

/** Duration badge, bottom right: `rgba(0,0,0,0.8)` scrim at 6dp radius. */
@Composable
internal fun BoxScope.DurationBadge(durationSec: Int, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .align(Alignment.BottomEnd)
            .padding(KiteSpacing.space4 / 2)
            .background(colors.badgeScrim, RoundedCornerShape(KiteRadius.badge))
            .padding(horizontal = KiteSpacing.space4, vertical = KiteSpacing.space2),
    ) {
        Text(
            text = KiteFormat.duration(durationSec),
            style = AppTheme.type.micro,
            color = colors.textPrimary,
        )
    }
}

/**
 * 3dp red bar over a translucent track, flush with the bottom edge.
 *
 * A plain [Box] rather than `LinearProgressIndicator`: the Material indicator animates its own
 * progress value and enforces a minimum height, neither of which is wanted here.
 */
@Composable
internal fun BoxScope.WatchedProgressBar(progress: Float, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(KiteSize.progressTrack)
            .background(colors.progressTrack),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(colors.progressFill),
        )
    }
}
