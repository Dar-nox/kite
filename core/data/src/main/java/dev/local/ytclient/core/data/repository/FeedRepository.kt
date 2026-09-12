package dev.local.ytclient.core.data.repository

import dev.local.ytclient.core.data.model.FeedChip
import dev.local.ytclient.core.data.model.FeedContent
import dev.local.ytclient.core.data.model.SyncResult
import kotlinx.coroutines.flow.Flow

/**
 * The home feed, built from subscribed channels' uploads.
 *
 * The algorithmic Home feed is not available through any API and is not attempted, per
 * `FEATURES.md`. Everything here is cached rows plus local rules, so the feed opens populated and
 * offline; a sync only ever writes more rows in behind it.
 */
interface FeedRepository {

    /**
     * The feed as a screen renders it.
     *
     * @param activeChips the user's filter chips; they persist across sessions via settings
     * @param revealHidden true after the user taps "Show" on the hidden-results notice — reveals
     *   filtered items inline for the session without changing any setting
     */
    fun observeFeed(
        activeChips: Set<FeedChip>,
        revealHidden: Boolean = false,
    ): Flow<FeedContent>

    /**
     * Pull-to-refresh. Cached rows are already on screen when this runs; whatever it fetches lands
     * in Room and reaches the UI through the same `Flow`, so there is no separate update path.
     */
    suspend fun refresh(): SyncResult

    /** Swipe left on a card. Local only and reversible from settings. */
    suspend fun hideVideo(videoId: String)

    suspend fun unhideVideo(videoId: String)
}
