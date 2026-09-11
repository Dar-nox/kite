package dev.local.ytclient.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/** Video joined with its channel, used wherever a blocked channel has to be excluded. */
data class VideoWithChannel(
    @Embedded val video: VideoEntity,
    @Relation(parentColumn = "channelId", entityColumn = "channelId")
    val channel: ChannelEntity?,
)

/** A saved row with everything the library list renders: video, watch state, and tags. */
data class SavedItemFull(
    @Embedded val savedItem: SavedItemEntity,
    @Relation(parentColumn = "videoId", entityColumn = "videoId")
    val video: VideoEntity?,
    @Relation(parentColumn = "videoId", entityColumn = "videoId")
    val watchState: WatchStateEntity?,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = SavedItemTagEntity::class,
            parentColumn = "savedItemId",
            entityColumn = "tagId",
            entity = TagEntity::class,
        ),
    )
    val tags: List<TagEntity>,
)

/** A playlist row joined to its cached video and watch state. */
data class PlaylistItemFull(
    @Embedded val playlistItem: PlaylistItemEntity,
    @Relation(parentColumn = "videoId", entityColumn = "videoId")
    val video: VideoEntity?,
    @Relation(parentColumn = "videoId", entityColumn = "videoId")
    val watchState: WatchStateEntity?,
)

/** A note with its video, for the library's mixed-type search results. */
data class NoteWithVideo(
    @Embedded val note: NoteEntity,
    @Relation(parentColumn = "videoId", entityColumn = "videoId")
    val video: VideoEntity?,
)

/** Per-collection counts for the library chips. */
data class CollectionCount(
    @ColumnInfo(name = "collection") val collection: String,
    @ColumnInfo(name = "count") val count: Int,
) {
    val asCollection: Collection get() = Collection.fromName(collection)
}

/** One folder with the playlists filed in it; unfiled playlists come back with `folderId == null`. */
data class FolderWithPlaylists(
    @Embedded val folder: FolderEntity,
    @Relation(parentColumn = "id", entityColumn = "folderId")
    val playlists: List<PlaylistEntity>,
)
