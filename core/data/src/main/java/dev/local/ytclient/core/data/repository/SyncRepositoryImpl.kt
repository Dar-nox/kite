package dev.local.ytclient.core.data.repository

import dev.local.ytclient.core.data.mapper.toEntity
import dev.local.ytclient.core.data.model.SyncResult
import dev.local.ytclient.core.database.dao.ChannelDao
import dev.local.ytclient.core.database.dao.VideoDao
import dev.local.ytclient.core.database.model.ChannelEntity
import dev.local.ytclient.core.network.api.ApiFailure
import dev.local.ytclient.core.network.api.YouTubeApi
import dev.local.ytclient.core.network.api.toApiFailure
import dev.local.ytclient.core.network.dto.PlaylistItemDto
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Uploads-playlist sync.
 *
 * Two calls per page per channel: `playlistItems` for the page, then one batched `videos` call for
 * the metadata a playlist item does not carry — duration, view count, category. Batching 50 ids into
 * a single call is the difference between a refresh that fits the daily quota and one that does not.
 *
 * Pages are fetched in bounded parallel; writes happen one channel at a time. Ordering the writes
 * keeps the new-item count honest and makes a partial failure easy to reason about: whatever
 * completed stays completed, and the next run picks up where it stopped.
 */
@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val api: YouTubeApi,
    private val channelDao: ChannelDao,
    private val videoDao: VideoDao,
) : SyncRepository {

    override suspend fun refreshFeed(maxPagesPerChannel: Int): SyncResult = try {
        val channels = channelDao.findSubscribed()
            .filter { !it.uploadsPlaylistId.isNullOrBlank() }
        if (channels.isEmpty()) return SyncResult.Empty

        var newItems = 0
        coroutineScope {
            channels.chunked(CHANNEL_CONCURRENCY).forEach { batch ->
                val fetched: List<Pair<ChannelEntity, List<PlaylistItemDto>>> = batch
                    .map { channel ->
                        async {
                            val playlistId = requireNotNull(channel.uploadsPlaylistId)
                            channel to fetchUploads(playlistId, maxPagesPerChannel)
                        }
                    }
                    .awaitAll()

                for ((channel, items) in fetched) {
                    newItems += writeVideos(items, channel.channelId)
                }
            }
        }
        SyncResult.Success(newItems = newItems, channelsSynced = channels.size)
    } catch (t: Throwable) {
        SyncResult.Failure(t.toApiFailure())
    }

    override suspend fun syncSubscriptions(): SyncResult = try {
        val response = api.mySubscriptions(maxResults = PAGE_SIZE)
        val ids = response.items.mapNotNull { it.id }.distinct()
        if (ids.isEmpty()) return SyncResult.Empty

        var synced = 0
        ids.chunked(IDS_PER_CALL).forEach { chunk ->
            val fetched = api.channels(id = chunk.joinToString(","))
            fetched.items.forEach { dto ->
                val channel = dto.toEntity()
                channelDao.syncMetadata(
                    channelId = channel.channelId,
                    title = channel.title,
                    avatarUrl = channel.avatarUrl,
                    uploadsPlaylistId = channel.uploadsPlaylistId,
                    subscriberCount = channel.subscriberCount,
                )
                channelDao.setSubscribed(channel.channelId, true)
                synced++
            }
        }
        SyncResult.Success(newItems = 0, channelsSynced = synced)
    } catch (t: Throwable) {
        when (t.toApiFailure()) {
            // mine=true without a token comes back 403. Say what is actually wrong, and point at the
            // path that does work in a key-only build.
            is ApiFailure.Forbidden -> SyncResult.Failure(
                ApiFailure.Unauthenticated(
                    "Reading your Google subscriptions needs sign-in. Add channels by URL instead.",
                ),
            )
            else -> SyncResult.Failure(t.toApiFailure())
        }
    }

    override suspend fun backfillChannel(channelId: String, maxPages: Int): SyncResult = try {
        val playlistId = channelDao.find(channelId)?.uploadsPlaylistId
        if (playlistId.isNullOrBlank()) return SyncResult.Empty

        SyncResult.Success(
            newItems = writeVideos(fetchUploads(playlistId, maxPages), channelId),
            channelsSynced = 1,
        )
    } catch (t: Throwable) {
        SyncResult.Failure(t.toApiFailure())
    }

    /** Pages through an uploads playlist until the pages run out or [maxPages] is reached. */
    private suspend fun fetchUploads(playlistId: String, maxPages: Int): List<PlaylistItemDto> {
        val collected = ArrayList<PlaylistItemDto>(maxPages * PAGE_SIZE)
        var token: String? = null
        repeat(maxPages) {
            val page = api.playlistItems(
                playlistId = playlistId,
                maxResults = PAGE_SIZE,
                pageToken = token,
            )
            collected += page.items
            token = page.nextPageToken
            if (page.items.isEmpty() || token.isNullOrBlank()) return collected
        }
        return collected
    }

    /**
     * Batch-fetches metadata and writes the rows, returning how many were not already cached.
     *
     * Playlist items carry a title and a thumbnail but no duration, view count, or category, which
     * is why the second call is not optional — and no `isShort`, which is why this never writes that
     * column.
     */
    private suspend fun writeVideos(items: List<PlaylistItemDto>, channelId: String): Int {
        val ids = items
            .mapNotNull { it.contentDetails?.videoId ?: it.snippet?.resourceId?.videoId }
            .distinct()
        if (ids.isEmpty()) return 0

        val alreadyCached = videoDao.existingIds(ids).toSet()
        val now = System.currentTimeMillis()
        var newCount = 0

        ids.chunked(IDS_PER_CALL).forEach { chunk ->
            api.videos(ids = chunk.joinToString(",")).items.forEach { dto ->
                val entity = dto.toEntity(channelIdFallback = channelId, cachedAt = now)
                videoDao.syncMetadata(entity)
                if (entity.videoId !in alreadyCached) newCount++
            }
        }
        return newCount
    }

    private companion object {
        const val PAGE_SIZE = 50
        const val IDS_PER_CALL = 50

        /**
         * Bounded concurrency. Higher is faster, but a burst of parallel calls is also how a client
         * trips the API's rate limits — a bad trade for a few hundred milliseconds on a personal
         * app.
         */
        const val CHANNEL_CONCURRENCY = 4
    }
}
