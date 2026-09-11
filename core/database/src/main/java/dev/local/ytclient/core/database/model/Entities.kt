package dev.local.ytclient.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room schema, following `DATA_MODEL.md`. Timestamps are epoch millis; durations and positions are
 * seconds.
 *
 * One deliberate departure from the spec, and the reason matters: the doc annotates user-data
 * columns such as `saved_items.videoId` as `FK → videos`, but it also requires that cached rows can
 * be wiped and re-fetched "without losing anything the user created". A real foreign key with
 * cascade would delete the user's saves the moment the cache is cleared, and one without cascade
 * would make a cache clear fail on a constraint. So cache tables (`videos`, `channels`,
 * `playlists`, `playlist_items`) are joined by plain indexed columns, with the repository upserting
 * a minimal video row whenever it saves something. User-data-to-user-data keys (`saved_item_tags`)
 * do carry real cascading foreign keys, since both sides are exported and neither is ever pruned.
 */

/** Cached video metadata. Re-fetchable, never user-created. */
@Entity(
    tableName = "videos",
    indices = [
        Index(value = ["channelId"]),
        Index(value = ["publishedAt"]),
        Index(value = ["isShort"]),
    ],
)
data class VideoEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    /** Logical reference to [ChannelEntity]; not a database-level foreign key, see above. */
    val channelId: String,
    val durationSec: Int,
    val publishedAt: Long,
    /** Highest available thumbnail. */
    val thumbnailUrl: String,
    val viewCount: Long? = null,
    /** `/shorts/` path or vertical aspect. */
    val isShort: Boolean = false,
    /** Aspect < 1.0 — feeds the optional stricter Shorts rule, which is off by default. */
    val isVertical: Boolean = false,
    /** Fetched lazily on watch; null until then. */
    val description: String? = null,
    val cachedAt: Long,
)

/** Channel metadata plus the per-channel rules that live on the channel rather than in settings. */
@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey val channelId: String,
    val title: String,
    val avatarUrl: String? = null,
    /** Source for the feed: this channel's uploads playlist. */
    val uploadsPlaylistId: String? = null,
    val subscriberCount: Long? = null,
    val isSubscribed: Boolean = false,
    /** Blocking is a read-time filter; the row and its videos stay. */
    val isBlocked: Boolean = false,
    /** Null = use the global default speed. */
    val defaultSpeed: Float? = null,
    /** Null = off, otherwise the collection name an upload is auto-saved to. */
    val autoSaveTarget: String? = null,
    /** Comma-separated tag ids. */
    val autoSaveTags: String? = null,
)

/**
 * The app's own Watch Later. YouTube's native Watch Later is not modifiable by any app — the API
 * removed programmatic access in 2016 — so this table is the answer, not a mirror.
 */
@Entity(
    tableName = "saved_items",
    indices = [
        Index(value = ["videoId", "collection"], unique = true),
        Index(value = ["collection", "sortOrder"]),
        Index(value = ["collection", "addedAt"]),
    ],
)
data class SavedItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Logical reference to [VideoEntity]; the repository upserts the video row alongside. */
    val videoId: String,
    @ColumnInfo(name = "collection") val collection: Collection,
    val addedAt: Long,
    /** Manual reordering within the collection. */
    val sortOrder: Int = 0,
    /** True when a channel rule added it rather than the user. */
    val autoAdded: Boolean = false,
)

/** This app's own watch history, deliberately separate from the Google account's. */
@Entity(tableName = "watch_state")
data class WatchStateEntity(
    @PrimaryKey val videoId: String,
    val positionSec: Int,
    val durationSec: Int,
    /** Set at ≥90% watched. */
    val completed: Boolean = false,
    /** Starts the auto-remove grace window. */
    val completedAt: Long? = null,
    val lastWatchedAt: Long,
) {
    /** 0f..1f, guarding against a zero duration. */
    val fractionWatched: Float
        get() = if (durationSec <= 0) 0f else (positionSec.toFloat() / durationSec).coerceIn(0f, 1f)
}

/** A timestamped bookmark. `timestampSec` null means a note about the whole video. */
@Entity(
    tableName = "notes",
    indices = [Index(value = ["videoId"]), Index(value = ["createdAt"])],
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoId: String,
    val timestampSec: Int? = null,
    val body: String,
    val createdAt: Long,
)

/** FTS4 index over note bodies, backing library search. */
@Fts4(contentEntity = NoteEntity::class)
@Entity(tableName = "notes_fts")
data class NoteFtsEntity(val body: String)

/** User-created tag. `colorKey` names a palette entry, never a hex value. */
@Entity(
    tableName = "tags",
    indices = [Index(value = ["name"], unique = true)],
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorKey: String = "neutral",
)

/** Many-to-many between saves and tags. Both sides are user data, so real cascading keys are safe. */
@Entity(
    tableName = "saved_item_tags",
    primaryKeys = ["savedItemId", "tagId"],
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = SavedItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["savedItemId"],
            onDelete = androidx.room.ForeignKey.CASCADE,
        ),
        androidx.room.ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = androidx.room.ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["tagId"])],
)
data class SavedItemTagEntity(
    val savedItemId: Long,
    val tagId: Long,
)

/** A YouTube playlist. `cachedItemCount` vs `itemCount` drives the progressive-loading banner. */
@Entity(
    tableName = "playlists",
    indices = [Index(value = ["folderId"]), Index(value = ["sortOrder"])],
)
data class PlaylistEntity(
    @PrimaryKey val playlistId: String,
    val title: String,
    val thumbnailUrl: String? = null,
    /** Reported by the API. */
    val itemCount: Int = 0,
    /** How many rows are actually local. */
    val cachedItemCount: Int = 0,
    /** Null = unfiled. */
    val folderId: Long? = null,
    val lastSyncedAt: Long? = null,
    /** Within its folder. */
    val sortOrder: Int = 0,
) {
    /** True while pages are still streaming in. */
    val isSyncing: Boolean get() = cachedItemCount < itemCount

    fun progress(): Float =
        if (itemCount <= 0) 0f else (cachedItemCount.toFloat() / itemCount).coerceIn(0f, 1f)
}

/** A playlist position. Sorting and filtering inside a playlist are local queries against this. */
@Entity(
    tableName = "playlist_items",
    primaryKeys = ["playlistId", "position"],
    indices = [Index(value = ["playlistId"]), Index(value = ["videoId"])],
)
data class PlaylistItemEntity(
    val playlistId: String,
    val position: Int,
    val videoId: String,
    val addedAt: Long? = null,
)

/** Purely local grouping. YouTube has no equivalent. */
@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sortOrder: Int = 0,
)

/** A term hidden from lists. Match flags decide which fields it applies to. */
@Entity(tableName = "keyword_filters")
data class KeywordFilterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val term: String,
    val matchTitle: Boolean = true,
    val matchDescription: Boolean = false,
)

/**
 * A feed item the user swiped away. Local only, reversible from settings, and deliberately its own
 * table rather than a column on `videos` so a cache refresh cannot resurrect or lose it.
 */
@Entity(
    tableName = "hidden_videos",
    indices = [Index(value = ["videoId"], unique = true)],
)
data class HiddenVideoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoId: String,
    val hiddenAt: Long,
)
