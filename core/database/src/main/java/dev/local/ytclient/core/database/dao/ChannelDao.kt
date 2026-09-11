package dev.local.ytclient.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import dev.local.ytclient.core.database.model.ChannelEntity
import kotlinx.coroutines.flow.Flow

/** Channels, subscriptions, blocks, and the per-channel rules that live on the channel. */
@Dao
interface ChannelDao {

    @Upsert
    suspend fun upsert(channel: ChannelEntity)

    @Upsert
    suspend fun upsertAll(channels: List<ChannelEntity>)

    @Query("SELECT * FROM channels WHERE channelId = :channelId")
    fun observe(channelId: String): Flow<ChannelEntity?>

    @Query("SELECT * FROM channels WHERE channelId = :channelId")
    suspend fun find(channelId: String): ChannelEntity?

    @Query("SELECT * FROM channels WHERE isSubscribed = 1 ORDER BY title COLLATE NOCASE")
    fun observeSubscribed(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE isBlocked = 1 ORDER BY title COLLATE NOCASE")
    fun observeBlocked(): Flow<List<ChannelEntity>>

    /**
     * The block list as a plain id list, which is what the filter pipeline consumes. Kept as a
     * `Flow` so blocking a channel takes effect on screen immediately.
     */
    @Query("SELECT channelId FROM channels WHERE isBlocked = 1")
    fun observeBlockedIds(): Flow<List<String>>

    /** Uploads playlists of every subscribed channel: the feed's source of truth. */
    @Query("SELECT uploadsPlaylistId FROM channels WHERE isSubscribed = 1 AND uploadsPlaylistId IS NOT NULL")
    suspend fun subscribedUploadsPlaylistIds(): List<String>

    @Query("UPDATE channels SET isBlocked = :blocked WHERE channelId = :channelId")
    suspend fun setBlocked(channelId: String, blocked: Boolean)

    @Query("UPDATE channels SET isSubscribed = :subscribed WHERE channelId = :channelId")
    suspend fun setSubscribed(channelId: String, subscribed: Boolean)

    /** Per-channel speed memory. Null clears it, which falls back to the global default. */
    @Query("UPDATE channels SET defaultSpeed = :speed WHERE channelId = :channelId")
    suspend fun setDefaultSpeed(channelId: String, speed: Float?)

    @Query(
        """
        UPDATE channels
        SET autoSaveTarget = :target, autoSaveTags = :tagIds
        WHERE channelId = :channelId
        """,
    )
    suspend fun setAutoSave(channelId: String, target: String?, tagIds: String?)
}
