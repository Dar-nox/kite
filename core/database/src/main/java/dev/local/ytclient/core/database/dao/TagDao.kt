package dev.local.ytclient.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dev.local.ytclient.core.database.model.TagEntity
import kotlinx.coroutines.flow.Flow

/** User tags. Names are unique; `colorKey` names a palette entry rather than a color. */
@Dao
interface TagDao {

    /**
     * Returns the id of `name`, creating it if needed.
     *
     * `INSERT OR IGNORE` plus a read is used rather than `@Upsert` because upserting on a unique
     * name would allocate a fresh id and orphan the `saved_item_tags` rows pointing at the old one.
     * The re-read after an ignored insert covers two callers racing on the same new tag.
     */
    @Transaction
    suspend fun insertOrGet(name: String, colorKey: String): Long? {
        idForName(name)?.let { return it }
        val id = insert(TagEntity(name = name, colorKey = colorKey))
        return if (id == -1L) idForName(name) else id
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(tag: TagEntity): Long

    @Query("SELECT id FROM tags WHERE name = :name LIMIT 1")
    suspend fun idForName(name: String): Long?

    @Query("SELECT * FROM tags ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE id IN (:ids)")
    suspend fun findByIds(ids: List<Long>): List<TagEntity>

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("INSERT OR IGNORE INTO saved_item_tags(savedItemId, tagId) VALUES (:savedItemId, :tagId)")
    suspend fun addToItem(savedItemId: Long, tagId: Long)

    @Query("DELETE FROM saved_item_tags WHERE savedItemId = :savedItemId AND tagId = :tagId")
    suspend fun removeFromItem(savedItemId: Long, tagId: Long)

    @Query("SELECT tagId FROM saved_item_tags WHERE savedItemId = :savedItemId")
    fun observeTagIdsForItem(savedItemId: Long): Flow<List<Long>>

    /** Tag usage counts, so the settings screen can show which tags are actually in use. */
    @Query(
        """
        SELECT t.* FROM tags t
        LEFT JOIN saved_item_tags sit ON sit.tagId = t.id
        GROUP BY t.id ORDER BY COUNT(sit.tagId) DESC, t.name COLLATE NOCASE
        """,
    )
    fun observeByUsage(): Flow<List<TagEntity>>
}
