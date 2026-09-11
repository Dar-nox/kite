package dev.local.ytclient.core.data.model

import dev.local.ytclient.core.data.filter.FilterReason
import dev.local.ytclient.core.data.filter.Filterable

/**
 * One feed row, already joined with its channel and watch state.
 *
 * Implements [Filterable] so the pipeline can run over it without knowing it came from the feed.
 */
data class FeedItem(
    override val videoId: String,
    override val title: String,
    override val channelId: String,
    override val description: String? = null,
    override val isShort: Boolean = false,
    override val isVertical: Boolean = false,
    val channelName: String? = null,
    val channelAvatarUrl: String? = null,
    val thumbnailUrl: String? = null,
    val durationSec: Int = 0,
    val publishedAt: Long = 0L,
    val viewCount: Long? = null,
    val positionSec: Int = 0,
    val completed: Boolean = false,
) : Filterable {

    /** 0f unwatched, 1f finished. Only values strictly between draw a progress bar. */
    val progress: Float
        get() = if (durationSec <= 0) 0f else (positionSec.toFloat() / durationSec).coerceIn(0f, 1f)

    val showProgress: Boolean get() = progress > 0f && progress < 1f
}

/** The feed filter chips. They persist across sessions; the set lives in settings. */
enum class FeedChip(val label: String) {
    All("All"),
    Unwatched("Unwatched"),
    Under10m("Under 10m"),
    ;

    companion object {
        fun fromName(value: String?): FeedChip? =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}

/**
 * Feed state as the screen renders it.
 *
 * `hiddenCount` is carried alongside the items rather than computed in the ViewModel so the
 * "N results hidden" affordance can never fall out of sync with what was actually removed.
 */
data class FeedContent(
    val items: List<FeedItem> = emptyList(),
    val hiddenByReason: Map<FilterReason, Int> = emptyMap(),
    val activeChips: Set<FeedChip> = setOf(FeedChip.All),
    val isRefreshing: Boolean = false,
    val lastSyncedAt: Long? = null,
    /** Set when a refresh failed but cached rows are on screen. */
    val isStale: Boolean = false,
) {
    val hiddenCount: Int get() = hiddenByReason.values.sum()

    /** Nothing cached at all, as opposed to "cached but filtered away". */
    val isEmpty: Boolean get() = items.isEmpty() && hiddenCount == 0

    /** Cached content exists but every row was filtered out — the case that reads as a bug. */
    val emptiedByFilters: Boolean get() = items.isEmpty() && hiddenCount > 0
}
