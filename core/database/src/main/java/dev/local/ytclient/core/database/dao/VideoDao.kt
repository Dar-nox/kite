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

    @Query("SELECT * FROM videos WHERE videoId = :videoId")
    fun observe(videoId: String): Flow<VideoEntity?>

    @Query("SELECT * FROM videos WHERE videoId = :videoId")
    suspend fun find(videoId: String): VideoEntity?

    @Query("SELECT * FROM videos WHERE videoId IN (:videoIds)")
    fun observeByIds(videoIds: List<String>): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE channelId = :channelId ORDER BY publishedAt DESC")
    fun observeByChannel(channelId: String): Flow<List<VideoEntity>>

    /** Descriptions are fetched lazily on watch, so they are written separately. */
    @Query("UPDATE videos SET description = :description WHERE videoId = :videoId")
    suspend fun setDescription(videoId: String, description: String?)

    @Query("SELECT COUNT(*) FROM videos")
    suspend fun count(): Int

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
