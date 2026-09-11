package dev.local.ytclient.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import dev.local.ytclient.core.database.model.FolderEntity
import dev.local.ytclient.core.database.model.FolderWithPlaylists
import dev.local.ytclient.core.database.model.PlaylistEntity
import dev.local.ytclient.core.database.model.PlaylistItemEntity
import dev.local.ytclient.core.database.model.PlaylistItemFull
import kotlinx.coroutines.flow.Flow

/**
 * Playlists, their items, and the local folders they are filed into.
 *
 * Everything inside a playlist — search, sort, filter — is a local query against `playlist_items`.
 * None of it is an API call, which is what lets a 2,000-item playlist be searchable while the rest
 * of it is still paging in.
 */
@Dao
interface PlaylistDao {

    @Upsert
    suspend fun upsert(playlist: PlaylistEntity)

    @Upsert
    suspend fun upsertAll(playlists: List<PlaylistEntity>)

    @Query("SELECT * FROM playlists WHERE playlistId = :playlistId")
    fun observe(playlistId: String): Flow<PlaylistEntity?>

    @Query("SELECT * FROM playlists WHERE playlistId = :playlistId")
    suspend fun find(playlistId: String): PlaylistEntity?

    /** Filed playlists grouped under their folder; unfiled playlists are handled separately. */
    @Transaction
    @Query("SELECT * FROM folders ORDER BY sortOrder ASC, name COLLATE NOCASE")
    fun observeFoldersWithPlaylists(): Flow<List<FolderWithPlaylists>>

    /** Unfiled section, which `DESIGN.md` puts last. */
    @Query("SELECT * FROM playlists WHERE folderId IS NULL ORDER BY sortOrder ASC, title COLLATE NOCASE")
    fun observeUnfiled(): Flow<List<PlaylistEntity>>

    @Query("UPDATE playlists SET folderId = :folderId WHERE playlistId = :playlistId")
    suspend fun setFolder(playlistId: String, folderId: Long?)

    @Query("UPDATE playlists SET lastSyncedAt = :syncedAt WHERE playlistId = :playlistId")
    suspend fun setLastSynced(playlistId: String, syncedAt: Long)

    @Query("UPDATE playlists SET sortOrder = :sortOrder WHERE playlistId = :playlistId")
    suspend fun setSortOrder(playlistId: String, sortOrder: Int)

    // --- Items ---

    @Transaction
    @Query("SELECT * FROM playlist_items WHERE playlistId = :playlistId ORDER BY position ASC")
    fun observeItems(playlistId: String): Flow<List<PlaylistItemFull>>

    @Upsert
    suspend fun upsertItems(items: List<PlaylistItemEntity>)

    @Query("DELETE FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun clearItems(playlistId: String)

    @Query("SELECT COUNT(*) FROM playlist_items WHERE playlistId = :playlistId")
    suspend fun countCachedItems(playlistId: String): Int

    @Query("UPDATE playlists SET cachedItemCount = :count WHERE playlistId = :playlistId")
    suspend fun updateCachedItemCount(playlistId: String, count: Int)

    /**
     * Re-derives `cachedItemCount` from the rows actually present.
     *
     * The banner compares this against the API-reported `itemCount`, so letting the two drift would
     * leave a "Loading N of M" bar on a playlist that is finished.
     */
    @Transaction
    suspend fun refreshCachedItemCount(playlistId: String) {
        updateCachedItemCount(playlistId, countCachedItems(playlistId))
    }

    /** Total duration of what is cached, for the "N videos · N hours" header. */
    @Query(
        """
        SELECT COALESCE(SUM(v.durationSec), 0) FROM playlist_items pi
        JOIN videos v ON v.videoId = pi.videoId
        WHERE pi.playlistId = :playlistId
        """
    )
    suspend fun totalDurationSec(playlistId: String): Long

    // --- Local queries inside a playlist: never API calls ---

    @Transaction
    @Query(
        """
        SELECT pi.* FROM playlist_items pi
        JOIN videos v ON v.videoId = pi.videoId
        WHERE pi.playlistId = :playlistId AND v.title LIKE '%' || :query || '%'
        ORDER BY pi.position ASC
        """
    )
    fun searchItems(playlistId: String, query: String): Flow<List<PlaylistItemFull>>

    @Transaction
    @Query(
        """
        SELECT pi.* FROM playlist_items pi
        JOIN videos v ON v.videoId = pi.videoId
        LEFT JOIN watch_state ws ON ws.videoId = pi.videoId
        WHERE pi.playlistId = :playlistId AND (ws.completed IS NULL OR ws.completed = 0)
        ORDER BY pi.position ASC
        """
    )
    fun observeUnwatchedItems(playlistId: String): Flow<List<PlaylistItemFull>>

    @Transaction
    @Query(
        """
        SELECT pi.* FROM playlist_items pi
        JOIN videos v ON v.videoId = pi.videoId
        WHERE pi.playlistId = :playlistId AND v.durationSec BETWEEN :minSec AND :maxSec
        ORDER BY pi.position ASC
        """
    )
    fun observeItemsByDuration(
        playlistId: String,
        minSec: Int,
        maxSec: Int,
    ): Flow<List<PlaylistItemFull>>

    @Transaction
    @Query(
        """
        SELECT pi.* FROM playlist_items pi
        JOIN videos v ON v.videoId = pi.videoId
        WHERE pi.playlistId = :playlistId AND v.channelId = :channelId
        ORDER BY pi.position ASC
        """
    )
    fun observeItemsByChannel(playlistId: String, channelId: String): Flow<List<PlaylistItemFull>>

    @Transaction
    suspend fun deletePlaylist(playlistId: String) {
        clearItems(playlistId)
        deletePlaylistRow(playlistId)
    }

    @Query("DELETE FROM playlists WHERE playlistId = :playlistId")
    suspend fun deletePlaylistRow(playlistId: String)
}

/** Local-only playlist folders. YouTube has no equivalent, so nothing here syncs. */
@Dao
interface FolderDao {

    @Upsert
    suspend fun upsert(folder: FolderEntity): Long

    @Query("SELECT * FROM folders ORDER BY sortOrder ASC, name COLLATE NOCASE")
    fun observeAll(): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun find(id: Long): FolderEntity?

    @Query("SELECT COALESCE(MAX(sortOrder), 0) FROM folders")
    suspend fun maxSortOrder(): Int

    /** Unfiles the folder's playlists first so nothing is stranded pointing at a dead folder id. */
    @Transaction
    suspend fun deleteFolder(id: Long) {
        unfileAll(id)
        deleteById(id)
    }

    @Query("UPDATE playlists SET folderId = NULL WHERE folderId = :folderId")
    suspend fun unfileAll(folderId: Long)

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteById(id: Long)
}
