package dev.local.ytclient.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.local.ytclient.core.database.KiteDatabase
import dev.local.ytclient.core.database.Migrations
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
import javax.inject.Singleton

/**
 * Room wiring.
 *
 * DAOs are provided individually rather than injecting [KiteDatabase] everywhere: a repository then
 * depends only on the tables it touches, which keeps the dependency graph honest and makes a
 * repository testable against a single fake DAO.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KiteDatabase =
        Room.databaseBuilder(context, KiteDatabase::class.java, KiteDatabase.NAME)
            .addMigrations(*Migrations.all)
            .addCallback(Migrations.foreignKeyCallback)
            .build()

    @Provides
    fun provideVideoDao(db: KiteDatabase): VideoDao = db.videoDao()

    @Provides
    fun provideChannelDao(db: KiteDatabase): ChannelDao = db.channelDao()

    @Provides
    fun provideSavedItemDao(db: KiteDatabase): SavedItemDao = db.savedItemDao()

    @Provides
    fun provideTagDao(db: KiteDatabase): TagDao = db.tagDao()

    @Provides
    fun provideWatchStateDao(db: KiteDatabase): WatchStateDao = db.watchStateDao()

    @Provides
    fun provideNoteDao(db: KiteDatabase): NoteDao = db.noteDao()

    @Provides
    fun providePlaylistDao(db: KiteDatabase): PlaylistDao = db.playlistDao()

    @Provides
    fun provideFolderDao(db: KiteDatabase): FolderDao = db.folderDao()

    @Provides
    fun provideKeywordFilterDao(db: KiteDatabase): KeywordFilterDao = db.keywordFilterDao()

    @Provides
    fun provideHiddenVideoDao(db: KiteDatabase): HiddenVideoDao = db.hiddenVideoDao()
}
