package dev.local.ytclient.core.database

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration list.
 *
 * Empty at version 1. The pattern for later versions is `MIGRATION_1_2` added to [all] and covered
 * by a `MigrationTestHelper` test against the exported schema — a migration that is not tested
 * against the real previous shape is a way to lose user data silently, and user data here is the
 * one thing that cannot be re-fetched.
 */
object Migrations {

    val all: Array<androidx.room.migration.Migration> = emptyArray()

    /**
     * Foreign keys are enforced from the first writable connection. Room does not enable them by
     * default, and the cascading keys on `saved_item_tags` are only real if this is on.
     */
    val foreignKeyCallback: RoomDatabase.Callback = object : RoomDatabase.Callback() {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            db.execSQL("PRAGMA foreign_keys = ON")
        }
    }
}
