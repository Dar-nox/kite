package dev.local.ytclient.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.local.ytclient.core.database.dao.ChannelDao
import dev.local.ytclient.core.database.dao.FolderDao
import dev.local.ytclient.core.database.dao.HiddenVideoDao
import dev.local.ytclient.core.database.dao.KeywordFilterDao
import dev.local.ytclient.core.database.dao.NoteDao
import dev.local.ytclient.core.database.dao.PlaylistDao
import dev.local.ytclient.core.database.dao.SavedItemDao
import dev.local.ytclient.core.database.dao.TagDao
import dev.local.ytclient.core.database.dao.VideoDao
import dev.local.ytclient.core.database.dao.WatchStateDao
import dev.local.ytclient.core.database.model.ChannelEntity
import dev.local.ytclient.core.database.model.FolderEntity
import dev.local.ytclient.core.database.model.HiddenVideoEntity
import dev.local.ytclient.core.database.model.KeywordFilterEntity
import dev.local.ytclient.core.database.model.NoteEntity
import dev.local.ytclient.core.database.model.NoteFtsEntity
import dev.local.ytclient.core.database.model.PlaylistEntity
import dev.local.ytclient.core.database.model.PlaylistItemEntity
import dev.local.ytclient.core.database.model.SavedItemEntity
import dev.local.ytclient.core.database.model.SavedItemTagEntity
import dev.local.ytclient.core.database.model.TagEntity
import dev.local.ytclient.core.database.model.VideoEntity
import dev.local.ytclient.core.database.model.WatchStateEntity

/**
 * The single local database. Room is the source of truth: the UI observes it and the network only
 * ever writes into it, which is what makes every screen work offline.
 *
 * Schemas are exported to `core/database/schemas/` so a future migration can be tested against the
 * real historical shape rather than a guess.
 */
@Database(
    entities = [
        VideoEntity::class,
        ChannelEntity::class,
        SavedItemEntity::class,
        WatchStateEntity::class,
        NoteEntity::class,
        NoteFtsEntity::class,
        TagEntity::class,
        SavedItemTagEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class,
        FolderEntity::class,
        KeywordFilterEntity::class,
        HiddenVideoEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class KiteDatabase : RoomDatabase() {

    abstract fun videoDao(): VideoDao
    abstract fun channelDao(): ChannelDao
    abstract fun savedItemDao(): SavedItemDao
    abstract fun tagDao(): TagDao
    abstract fun watchStateDao(): WatchStateDao
    abstract fun noteDao(): NoteDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun folderDao(): FolderDao
    abstract fun keywordFilterDao(): KeywordFilterDao
    abstract fun hiddenVideoDao(): HiddenVideoDao

    companion object {
        const val NAME = "kite.db"
    }
}
