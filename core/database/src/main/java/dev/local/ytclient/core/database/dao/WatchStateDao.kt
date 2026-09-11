package dev.local.ytclient.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import dev.local.ytclient.core.database.model.WatchStateEntity
import kotlinx.coroutines.flow.Flow

/**
 * This app's own watch history, deliberately separate from the Google account's.
 *
 * `completed` is set at ≥90% watched and stamps `completedAt`, which starts the auto-remove grace
 * window. The threshold and the window both live in the repository, not here: the table records
 * facts, the rules decide what to do with them.
 */
@Dao
interface WatchStateDao {

    @Upsert
    suspend fun upsert(state: WatchStateEntity)

    @Query("SELECT * FROM watch_state WHERE videoId = :videoId")
    fun observe(videoId: String): Flow<WatchStateEntity?>

    @Query("SELECT * FROM watch_state WHERE videoId = :videoId")
    suspend fun find(videoId: String): WatchStateEntity?

    @Query("SELECT * FROM watch_state")
    suspend fun findAll(): List<WatchStateEntity>

    @Query("SELECT * FROM watch_state")
    fun observeAll(): Flow<List<WatchStateEntity>>

    @Query("SELECT videoId FROM watch_state WHERE completed = 1")
    fun observeCompletedIds(): Flow<List<String>>

    /**
     * Items whose grace window has expired: completed, and completed long enough ago to act on.
     * Used by `AutoArchiveWorker` for auto-remove-watched.
     */
    @Query(
        """
        SELECT * FROM watch_state
        WHERE completed = 1 AND completedAt IS NOT NULL AND completedAt <= :completedBefore
        """,
    )
    suspend fun findCompletedBefore(completedBefore: Long): List<WatchStateEntity>

    /**
     * Records progress, flipping `completed` once the 90% threshold is crossed.
     *
     * A position that moves backwards (a rewatch) clears completion so the item can come back;
     * `completedAt` is only ever stamped on the transition, so the grace window measures from the
     * first time the video was finished rather than from the latest heartbeat.
     */
    @Transaction
    suspend fun recordProgress(videoId: String, positionSec: Int, durationSec: Int, now: Long) {
        val fraction = if (durationSec <= 0) 0f else positionSec.toFloat() / durationSec
        val existing = find(videoId)
        val isComplete = fraction >= COMPLETION_THRESHOLD
        upsert(
            WatchStateEntity(
                videoId = videoId,
                positionSec = positionSec,
                durationSec = durationSec,
                completed = isComplete,
                completedAt = when {
                    isComplete && existing?.completedAt == null -> now
                    isComplete -> existing?.completedAt
                    else -> null
                },
                lastWatchedAt = now,
            ),
        )
    }

    @Query("DELETE FROM watch_state WHERE videoId = :videoId")
    suspend fun clear(videoId: String)

    companion object {
        /** ≥90% watched counts as complete, per `FEATURES.md`. */
        const val COMPLETION_THRESHOLD = 0.9f
    }
}
