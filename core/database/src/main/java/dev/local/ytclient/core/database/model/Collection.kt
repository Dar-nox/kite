package dev.local.ytclient.core.database.model

/**
 * The three library collections, from `FEATURES.md`.
 *
 * Tabs, not folders: a video lives in exactly one collection at a time, which is what makes
 * "auto-remove watched" safe — removal moves Queue → Archive rather than deleting anything.
 *
 * Persisted as the enum name. Renaming an entry is therefore a data migration, not a refactor.
 */
enum class Collection(val displayName: String) {
    Queue("Queue"),
    Favorites("Favorites"),
    Archive("Archive");

    companion object {
        fun fromName(value: String?): Collection =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: Queue
    }
}
