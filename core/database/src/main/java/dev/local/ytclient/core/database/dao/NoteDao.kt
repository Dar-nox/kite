package dev.local.ytclient.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import dev.local.ytclient.core.database.model.NoteEntity
import dev.local.ytclient.core.database.model.NoteWithVideo
import kotlinx.coroutines.flow.Flow

/**
 * Timestamped bookmarks.
 *
 * Search runs against the `notes_fts` FTS4 index rather than `LIKE '%…%'`, so library search stays
 * fast as notes accumulate. Room keeps the index in sync with the content table via triggers, so
 * there is no manual reindex path to forget.
 *
 * `MATCH` takes FTS query syntax, not raw text — the repository is responsible for quoting user
 * input (see `NoteSearchQuery`), otherwise a term like `c++` or a bare quote throws.
 */
@Dao
interface NoteDao {

    @Insert
    suspend fun insert(note: NoteEntity): Long

    @Upsert
    suspend fun upsert(note: NoteEntity)

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun find(id: Long): NoteEntity?

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("UPDATE notes SET timestampSec = :timestampSec, body = :body WHERE id = :id")
    suspend fun update(id: Long, timestampSec: Int?, body: String)

    /** Notes for one video, timestamped ones first in playback order, undated ones after. */
    @Query(
        """
        SELECT * FROM notes WHERE videoId = :videoId
        ORDER BY timestampSec IS NULL, timestampSec ASC, createdAt DESC
        """,
    )
    fun observeForVideo(videoId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    /** Full-text search, joined to the video so the library can render a mixed-type result list. */
    @Transaction
    @Query(
        """
        SELECT notes.* FROM notes
        JOIN notes_fts ON notes.rowid = notes_fts.rowid
        WHERE notes_fts MATCH :query
        ORDER BY notes.createdAt DESC
        """
    )
    fun search(query: String): Flow<List<NoteWithVideo>>

    /** Notes belonging to a set of videos — used to filter search results to a collection. */
    @Transaction
    @Query(
        """
        SELECT notes.* FROM notes
        JOIN notes_fts ON notes.rowid = notes_fts.rowid
        WHERE notes_fts MATCH :query AND notes.videoId IN (:videoIds)
        ORDER BY notes.createdAt DESC
        """
    )
    fun searchWithin(query: String, videoIds: List<String>): Flow<List<NoteWithVideo>>

    @Query("SELECT COUNT(*) FROM notes")
    suspend fun count(): Int

    @Query("SELECT * FROM notes")
    suspend fun findAll(): List<NoteEntity>
}

/**
 * Escapes a user's search term for FTS4 `MATCH`.
 *
 * FTS treats punctuation as operators, so unquoted input such as `c++`, `"half`, or `don't` is
 * either a syntax error or a silently different query. Wrapping in double quotes and doubling any
 * embedded quote turns any input into a literal phrase search.
 */
fun ftsMatchQuery(term: String): String {
    val trimmed = term.trim()
    if (trimmed.isEmpty()) return "\"\""
    return "\"" + trimmed.replace("\"", "\"\"") + "\""
}
