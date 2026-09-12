package dev.local.ytclient.core.data.repository

import dev.local.ytclient.core.data.model.SyncResult

/**
 * Everything that talks to the network on the user's behalf.
 *
 * Workers call these; the UI calls [refreshFeed] through `FeedRepository` for pull-to-refresh. All
 * of them return a [SyncResult] rather than throwing, and none of them clear anything on failure —
 * a failed refresh leaves the cached feed exactly as it was.
 */
interface SyncRepository {

    /**
     * Pulls recent uploads for every subscribed channel.
     *
     * @param maxPagesPerChannel 50 items per page. One page is 2 units per channel (one
     *   `playlistItems` call plus one batched `videos` call), so a 50-channel refresh costs ~100 of
     *   the 10,000 daily units. The periodic worker uses one page; a pull-to-refresh uses one page;
     *   only a fresh subscription backfills deeper.
     */
    suspend fun refreshFeed(maxPagesPerChannel: Int = 1): SyncResult

    /**
     * Re-reads the subscription list. Requires OAuth, so with the key-only account binding this
     * reports [dev.local.ytclient.core.network.api.ApiFailure.Unauthenticated] and the manual
     * subscription list is left untouched.
     */
    suspend fun syncSubscriptions(): SyncResult

    /**
     * Backfills a channel's uploads deeper than the refresh window. Run once when a channel is
     * first subscribed, so its back catalogue is there instead of only the latest fifty.
     */
    suspend fun backfillChannel(channelId: String, maxPages: Int = 5): SyncResult
}
