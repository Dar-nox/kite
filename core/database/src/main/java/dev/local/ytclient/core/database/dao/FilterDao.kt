package dev.local.ytclient.core.database.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import dev.local.ytclient.core.database.model.HiddenVideoEntity
import dev.local.ytclient.core.database.model.KeywordFilterEntity
import dev.local.ytclient.core.database.model.VideoEntity
import kotlinx.coroutines.flow.Flow

/** A hidden feed item joined to its video, for the settings screen that lists them. */
data class HiddenVideoFull(
    @Embedded val hidden: HiddenVideoEntity,
    @Relation(parentColumn = "videoId", entityColumn = "videoId")
    val video: VideoEntity?,
)

/**
 * Keyword filters.
 *
 * Matching is applied by the filter pipeline at read time, never at insert time, so editing or
 * deleting a term takes effect on the next emission with no re-fetch.
 */
@Dao
interface KeywordFilterDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(filter: KeywordFilterEntity): Long

    @Query("SELECT * FROM keyword_filters ORDER BY term COLLATE NOCASE")
    fun observeAll(): Flow<List<KeywordFilterEntity>>

    @Query("SELECT * FROM keyword_filters WHERE id = :id")
    suspend fun find(id: Long): KeywordFilterEntity?

    @Query("SELECT term FROM keyword_filters")
    suspend fun allTerms(): List<String>

    @Query("DELETE FROM keyword_filters WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM keyword_filters WHERE term = :term")
    suspend fun deleteByTerm(term: String)

    @Query("DELETE FROM keyword_filters")
    suspend fun clear()
}

/**
 * Videos swiped away from the feed.
 *
 * Local only and reversible from settings, per `FEATURES.md`. Separate from channel blocking: this
 * hides one video, blocking removes a whole channel everywhere.
 */
@Dao
interface HiddenVideoDao {

    @Query("INSERT OR IGNORE INTO hidden_videos(videoId, hiddenAt) VALUES (:videoId, :hiddenAt)")
    suspend fun hide(videoId: String, hiddenAt: Long)

    @Query("SELECT videoId FROM hidden_videos")
    fun observeIds(): Flow<List<String>>

    @Transaction
    @Query("SELECT * FROM hidden_videos ORDER BY hiddenAt DESC")
    fun observeAll(): Flow<List<HiddenVideoFull>>

    @Query("SELECT COUNT(*) FROM hidden_videos")
    fun observeCount(): Flow<Int>

    @Query("DELETE FROM hidden_videos WHERE videoId = :videoId")
    suspend fun unhide(videoId: String)

    @Query("DELETE FROM hidden_videos")
    suspend fun clear()
}
