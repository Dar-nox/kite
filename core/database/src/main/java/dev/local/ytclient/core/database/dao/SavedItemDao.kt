package dev.local.ytclient.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import dev.local.ytclient.core.database.model.Collection
import dev.local.ytclient.core.database.model.CollectionCount
import dev.local.ytclient.core.database.model.SavedItemEntity
import dev.local.ytclient.core.database.model.SavedItemFull
import kotlinx.coroutines.flow.Flow

/** How a collection is ordered. `FEATURES.md` offers these four and nothing else. */
enum class LibrarySort(val displayName: String) {
    DateAdded("Date added"),
    DatePublished("Date published"),
    Duration("Duration"),
    Manual("Manual order"),
}

/**
 * The custom Queue / Favorites / Archive.
 *
 * A video lives in exactly one collection at a time, so moving is a delete-plus-insert inside a
 * transaction rather than an update — the unique index on (videoId, collection) is a safety net, not
 * the mechanism that enforces it.
 */
@Dao
interface SavedItemDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: SavedItemEntity): Long

    @Query("DELETE FROM saved_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM saved_items WHERE videoId = :videoId")
    suspend fun deleteByVideoId(videoId: String)

    @Query("SELECT * FROM saved_items WHERE videoId = :videoId AND collection = :collection LIMIT 1")
    suspend fun find(videoId: String, collection: Collection): SavedItemEntity?

    @Query("SELECT * FROM saved_items WHERE videoId = :videoId LIMIT 1")
    suspend fun findAny(videoId: String): SavedItemEntity?

    @Query("SELECT videoId FROM saved_items")
    fun observeSavedVideoIds(): Flow<List<String>>

    /** Which collections a video is currently in — drives the filled state of the save button. */
    @Query("SELECT collection FROM saved_items WHERE videoId = :videoId")
    fun observeCollectionsFor(videoId: String): Flow<List<Collection>>

    @Query("SELECT collection AS collection, COUNT(*) AS count FROM saved_items GROUP BY collection")
    fun observeCounts(): Flow<List<CollectionCount>>

    @Query("SELECT COALESCE(MAX(sortOrder), 0) FROM saved_items WHERE collection = :collection")
    suspend fun maxSortOrder(collection: Collection): Int

    // --- Sort variants. ORDER BY cannot be a bind parameter, so each sort is its own query. ---

    @Transaction
    @Query("SELECT * FROM saved_items WHERE collection = :collection ORDER BY addedAt DESC, id DESC")
    fun observeByDateAdded(collection: Collection): Flow<List<SavedItemFull>>

    @Transaction
    @Query(
        """
        SELECT si.* FROM saved_items si
        LEFT JOIN videos v ON v.videoId = si.videoId
        WHERE si.collection = :collection
        ORDER BY v.publishedAt DESC
        """,
    )
    fun observeByDatePublished(collection: Collection): Flow<List<SavedItemFull>>

    @Transaction
    @Query(
        """
        SELECT si.* FROM saved_items si
        LEFT JOIN videos v ON v.videoId = si.videoId
        WHERE si.collection = :collection
        ORDER BY v.durationSec DESC
        """,
    )
    fun observeByDuration(collection: Collection): Flow<List<SavedItemFull>>

    @Transaction
    @Query("SELECT * FROM saved_items WHERE collection = :collection ORDER BY sortOrder ASC, addedAt ASC")
    fun observeManual(collection: Collection): Flow<List<SavedItemFull>>

    /** Dispatches to the right sort query. */
    fun observe(collection: Collection, sort: LibrarySort): Flow<List<SavedItemFull>> = when (sort) {
        LibrarySort.DateAdded -> observeByDateAdded(collection)
        LibrarySort.DatePublished -> observeByDatePublished(collection)
        LibrarySort.Duration -> observeByDuration(collection)
        LibrarySort.Manual -> observeManual(collection)
    }

    /**
     * Moves an item between collections, returning the id of the new row.
     *
     * `addedAt` is preserved so an undo restores the original position in a date-sorted list, and
     * the new sort order is appended at the end of the destination collection.
     */
    @Transaction
    suspend fun moveTo(videoId: String, from: Collection, to: Collection): Long? {
        val existing = find(videoId, from) ?: return null
        deleteById(existing.id)
        return insert(
            existing.copy(
                id = 0,
                collection = to,
                sortOrder = maxSortOrder(to) + 1,
            ),
        )
    }

    /** Applies a manual drag reorder: the list arrives in the new order. */
    @Transaction
    suspend fun applyOrder(orderedIds: List<Long>) {
        orderedIds.forEachIndexed { index, id -> updateSortOrder(id, index) }
    }

    @Query("UPDATE saved_items SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int)
}
