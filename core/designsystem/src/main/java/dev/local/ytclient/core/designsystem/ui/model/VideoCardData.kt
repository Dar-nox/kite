package dev.local.ytclient.core.designsystem.ui.model

import androidx.compose.runtime.Immutable
import dev.local.ytclient.core.designsystem.ui.theme.TagColorKey

/**
 * Presentation model for the three video card variants.
 *
 * Features map their own state onto this and pass it down, which keeps the cards stateless: no
 * ViewModel, no database types, and nothing here knows whether a row came from the feed, a
 * playlist, or the library. Text that varies by surface (the metadata line) is formatted by the
 * caller with [dev.local.ytclient.core.designsystem.util.KiteFormat].
 */
@Immutable
data class VideoCardData(
    val videoId: String,
    val title: String,
    val thumbnailUrl: String?,
    val durationSec: Int,
    /** Pre-formatted "Channel · 12K views · 2 days ago". Null hides the line entirely. */
    val metaLine: String? = null,
    val channelName: String? = null,
    /** Large card only — compact rows and grid tiles deliberately show no avatar. */
    val channelAvatarUrl: String? = null,
    /** 0f unwatched, 1f fully watched. Only values strictly between the two draw a bar. */
    val progress: Float = 0f,
    /** Fully watched: the card renders at 60% opacity unless the Unwatched filter excluded it. */
    val dimmed: Boolean = false,
    val isShort: Boolean = false,
    /** Library and playlist rows only. */
    val tags: List<TagChipData> = emptyList(),
) {
    /** True when a progress bar should be drawn: started but not finished. */
    val showProgress: Boolean get() = progress > 0f && progress < 1f
}

/** A tag as drawn: its name plus the palette entry its stored `colorKey` resolves to. */
@Immutable
data class TagChipData(
    val id: Long,
    val name: String,
    val colorKey: TagColorKey = TagColorKey.Default,
)
