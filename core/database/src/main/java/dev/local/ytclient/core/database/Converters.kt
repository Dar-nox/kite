package dev.local.ytclient.core.database

import androidx.room.TypeConverter
import dev.local.ytclient.core.database.model.Collection

/** Persists enums by name so a stored value stays readable in an exported database. */
class Converters {

    @TypeConverter
    fun collectionToString(value: Collection): String = value.name

    @TypeConverter
    fun stringToCollection(value: String?): Collection = Collection.fromName(value)
}
