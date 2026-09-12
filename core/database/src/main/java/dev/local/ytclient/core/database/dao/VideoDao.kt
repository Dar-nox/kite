package dev.local.ytclient.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import dev.local.ytclient.core.database.model.VideoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Cached video metadata.
 *
 * Every read here is a [Flow] so a background sync writing to the table updates the UI with no
 * manual refresh path — the data-flow contract in `ARCHITECTURE.md`.
 */
@Dao
interface VideoDao {

    @Upsert
    suspend fun upsert(video: VideoEntity)

    @Upsert
    suspend fun upsertAll(videos: List<VideoEntity>)

    /**
     * Writes only the columns a sync owns, on conflict.
     *
     * `isShort` is absent from the update list on purpose: the Data API cannot detect Shorts, the
     * stream resolver sets it from the watch page, and a nightly refresh that reset it to false
     * would quietly leak Shorts back into the feed. `description` uses COALESCE so a lazily fetched
     * description survives a metadata refresh that did not ask for one.
     */
    @Query(
        """
        INSERT INTO videos (
            videoId, title, channelId, durationSec, publishedAt, thumbnailUrl,
            viewCount, isVertical, description, categoryId, cachedAt
        )
        VALUES (
            :videoId, :title, :channelId, :durationSec, :publishedAt, :thumbnailUrl,
            :viewCount, :isVertical, :description, :categoryId, :cachedAt
        )
        ON CONFLICT(videoId) DO UPDATE SET
            title = excluded.title,
            channelId = excluded.channelId,
            durationSec = excluded.durationSec,
            publishedAt = excluded.publishedAt,
            thumbnailUrl = excluded.thumbnailUrl,
            viewCount = excluded.viewCount,
            description = COALESCE(excluded.description, videos.description),
            categoryId = excluded.categoryId,
            cachedAt = excluded.cachedAt
        """
    )
    suspend fun syncMetadata(video: VideoEntity)

    @Query("SELECT * FROM videos WHERE videoId = :videoId")
    fun observe(videoId: String): Flow<VideoEntity?>

    @Query("SELECT * FROM videos WHERE videoId = :videoId")
    suspend fun find(videoId: String): VideoEntity?

    @Query("SELECT * FROM videos WHERE videoId IN (:videoIds)")
    fun observeByIds(videoIds: List<String>): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE channelId = :channelId ORDER BY publishedAt DESC")
    fun observeByChannel(channelId: String): Flow<List<VideoEntity>>

    /**
     * The feed's raw input: every cached upload from a subscribed channel, newest first.
     *
     * One join rather than a query per channel, so the feed does not scale its database work with
     * the size of the subscription list.
     */
    @Query(
        """
        SELECT v.* FROM videos v
        JOIN channels c ON c.channelId = v.channelId
        WHERE c.isSubscribed = 1
        ORDER BY v.publishedAt DESC
        """
    )
    fun observeSubscribedUploads(): Flow<List<VideoEntity>>

    /** Descriptions are fetched lazily on watch, so they are written separately. */
    @Query("UPDATE videos SET description = :description WHERE videoId = :videoId")
    suspend fun setDescription(videoId: String, description: String?)

    @Query("SELECT COUNT(*) FROM videos")
    suspend fun count(): Int

    /** Which of [videoIds] are already cached, so a sync can report how many rows were new. */
    @Query("SELECT videoId FROM videos WHERE videoId IN (:videoIds)")
    suspend fun existingIds(videoIds: List<String>): List<String>

    /**
     * Wipes cached metadata while keeping every row the user's data still points at.
     *
     * A blanket `DELETE FROM videos` would strand saved items, notes, watch state, and playlist
     * rows — exactly what `DATA_MODEL.md` says a cache clear must not do.
     */
    @Query(
        """
        DELETE FROM videos WHERE videoId NOT IN (
            SELECT videoId FROM saved_items
            UNION SELECT videoId FROM notes
            UNION SELECT videoId FROM watch_state
            UNION SELECT videoId FROM playlist_items
        )
        """,
    )
    suspend fun clearCache()
}
