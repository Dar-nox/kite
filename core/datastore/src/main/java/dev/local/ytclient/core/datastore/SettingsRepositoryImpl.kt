package dev.local.ytclient.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore-backed settings.
 *
 * Keys match the list in `DATA_MODEL.md` exactly, so a stored preferences file stays readable and
 * portable between builds. Defaults are applied at read time rather than written on first launch,
 * which means a fresh install never pays a write and a new setting added later picks up its default
 * without a migration.
 */
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    private object Keys {
        val FEED_LAYOUT = stringPreferencesKey("feed.layout")
        val FEED_GRID_COLUMNS = intPreferencesKey("feed.gridColumns")
        val FEED_EXPAND_COLUMNS_LANDSCAPE = booleanPreferencesKey("feed.expandColumnsLandscape")
        val FEED_FILTERS = stringSetPreferencesKey("feed.filters")

        val SHORTS_MODE = stringPreferencesKey("shorts.mode")
        val SHORTS_TREAT_VERTICAL_AS_SHORT = booleanPreferencesKey("shorts.treatVerticalAsShort")

        val PLAYER_BACKGROUND_PLAY = booleanPreferencesKey("player.backgroundPlay")
        val PLAYER_REMEMBER_SPEED = booleanPreferencesKey("player.rememberSpeedPerChannel")
        val PLAYER_DEFAULT_SPEED = floatPreferencesKey("player.defaultSpeed")
        val PLAYER_QUALITY_WIFI = stringPreferencesKey("player.qualityWifi")
        val PLAYER_QUALITY_CELLULAR = stringPreferencesKey("player.qualityCellular")
        val PLAYER_SEEK_INTERVAL = intPreferencesKey("player.seekIntervalSec")

        val WATCH_TAB_DEFAULT = stringPreferencesKey("watch.tabDefault")
        val WATCH_SHOW_GESTURE_GUIDES = booleanPreferencesKey("watch.showGestureGuides")

        val LIBRARY_AUTO_ARCHIVE_DAYS = intPreferencesKey("library.autoArchiveDays")
        val LIBRARY_AUTO_REMOVE_WATCHED = booleanPreferencesKey("library.autoRemoveWatched")
        val LIBRARY_AUTO_REMOVE_GRACE_HOURS = intPreferencesKey("library.autoRemoveGraceHours")

        val SPONSORBLOCK_ENABLED = booleanPreferencesKey("sponsorblock.enabled")
        val SPONSORBLOCK_CATEGORIES = stringSetPreferencesKey("sponsorblock.categories")

        val DISLIKES_SHOW_ESTIMATES = booleanPreferencesKey("dislikes.showEstimates")
    }

    private val defaults = KiteSettings()

    override val settings: Flow<KiteSettings> = dataStore.data.map { p ->
        KiteSettings(
            feedLayout = FeedLayout.fromName(p[Keys.FEED_LAYOUT]),
            // Clamped on read, per FEATURES.md: a stored 4 from a tablet must not survive onto a phone.
            gridColumns = (p[Keys.FEED_GRID_COLUMNS] ?: defaults.gridColumns)
                .coerceIn(GridColumnLimits.Min, GridColumnLimits.Max),
            expandColumnsLandscape = p[Keys.FEED_EXPAND_COLUMNS_LANDSCAPE]
                ?: defaults.expandColumnsLandscape,
            feedFilters = p[Keys.FEED_FILTERS] ?: emptySet(),
            shortsMode = ShortsMode.fromName(p[Keys.SHORTS_MODE]),
            treatVerticalAsShort = p[Keys.SHORTS_TREAT_VERTICAL_AS_SHORT]
                ?: defaults.treatVerticalAsShort,
            backgroundPlay = p[Keys.PLAYER_BACKGROUND_PLAY] ?: defaults.backgroundPlay,
            rememberSpeedPerChannel = p[Keys.PLAYER_REMEMBER_SPEED]
                ?: defaults.rememberSpeedPerChannel,
            defaultSpeed = p[Keys.PLAYER_DEFAULT_SPEED] ?: defaults.defaultSpeed,
            qualityWifi = p[Keys.PLAYER_QUALITY_WIFI] ?: defaults.qualityWifi,
            qualityCellular = p[Keys.PLAYER_QUALITY_CELLULAR] ?: defaults.qualityCellular,
            seekIntervalSec = p[Keys.PLAYER_SEEK_INTERVAL] ?: defaults.seekIntervalSec,
            showGestureGuides = p[Keys.WATCH_SHOW_GESTURE_GUIDES] ?: defaults.showGestureGuides,
            watchTabDefault = WatchTab.fromName(p[Keys.WATCH_TAB_DEFAULT]),
            autoArchiveDays = p[Keys.LIBRARY_AUTO_ARCHIVE_DAYS] ?: defaults.autoArchiveDays,
            autoRemoveWatched = p[Keys.LIBRARY_AUTO_REMOVE_WATCHED]
                ?: defaults.autoRemoveWatched,
            autoRemoveGraceHours = p[Keys.LIBRARY_AUTO_REMOVE_GRACE_HOURS]
                ?: defaults.autoRemoveGraceHours,
            sponsorBlockEnabled = p[Keys.SPONSORBLOCK_ENABLED] ?: defaults.sponsorBlockEnabled,
            sponsorBlockCategories = p[Keys.SPONSORBLOCK_CATEGORIES]
                ?.mapNotNull { SponsorCategory.fromName(it) }
                ?.toSet()
                ?: defaults.sponsorBlockCategories,
            showDislikeEstimates = p[Keys.DISLIKES_SHOW_ESTIMATES] ?: defaults.showDislikeEstimates,
        )
    }

    override val feedLayout: Flow<FeedLayout> = settings.map { it.feedLayout }
    override val shortsMode: Flow<ShortsMode> = settings.map { it.shortsMode }
    override val sponsorBlockEnabled: Flow<Boolean> = settings.map { it.sponsorBlockEnabled }
    override val sponsorBlockCategories: Flow<Set<SponsorCategory>> =
        settings.map { it.sponsorBlockCategories }
    override val defaultSpeed: Flow<Float> = settings.map { it.defaultSpeed }
    override val rememberSpeedPerChannel: Flow<Boolean> = settings.map { it.rememberSpeedPerChannel }

    override suspend fun setFeedLayout(layout: FeedLayout) = dataStore.edit { it[Keys.FEED_LAYOUT] = layout.name }

    override suspend fun setGridColumns(columns: Int) = dataStore.edit {
        it[Keys.FEED_GRID_COLUMNS] = columns.coerceIn(GridColumnLimits.Min, GridColumnLimits.Max)
    }

    override suspend fun setExpandColumnsLandscape(enabled: Boolean) =
        dataStore.edit { it[Keys.FEED_EXPAND_COLUMNS_LANDSCAPE] = enabled }

    override suspend fun setFeedFilters(filters: Set<String>) =
        dataStore.edit { it[Keys.FEED_FILTERS] = filters }

    override suspend fun setShortsMode(mode: ShortsMode) = dataStore.edit { it[Keys.SHORTS_MODE] = mode.name }

    override suspend fun setTreatVerticalAsShort(enabled: Boolean) =
        dataStore.edit { it[Keys.SHORTS_TREAT_VERTICAL_AS_SHORT] = enabled }

    override suspend fun setBackgroundPlay(enabled: Boolean) =
        dataStore.edit { it[Keys.PLAYER_BACKGROUND_PLAY] = enabled }

    override suspend fun setRememberSpeedPerChannel(enabled: Boolean) =
        dataStore.edit { it[Keys.PLAYER_REMEMBER_SPEED] = enabled }

    override suspend fun setDefaultSpeed(speed: Float) =
        dataStore.edit { it[Keys.PLAYER_DEFAULT_SPEED] = speed.coerceIn(MIN_SPEED, MAX_SPEED) }

    override suspend fun setQualityWifi(quality: String) =
        dataStore.edit { it[Keys.PLAYER_QUALITY_WIFI] = quality }

    override suspend fun setQualityCellular(quality: String) =
        dataStore.edit { it[Keys.PLAYER_QUALITY_CELLULAR] = quality }

    override suspend fun setSeekIntervalSec(seconds: Int) =
        dataStore.edit { it[Keys.PLAYER_SEEK_INTERVAL] = seconds.coerceIn(MIN_SEEK_SEC, MAX_SEEK_SEC) }

    /** Gesture guides show on first launch only, so this is a one-way write. */
    override suspend fun setGestureGuidesShown() =
        dataStore.edit { it[Keys.WATCH_SHOW_GESTURE_GUIDES] = false }

    override suspend fun setWatchTabDefault(tab: WatchTab) =
        dataStore.edit { it[Keys.WATCH_TAB_DEFAULT] = tab.name }

    override suspend fun setAutoArchiveDays(days: Int) =
        dataStore.edit { it[Keys.LIBRARY_AUTO_ARCHIVE_DAYS] = days.coerceAtLeast(0) }

    override suspend fun setAutoRemoveWatched(enabled: Boolean) =
        dataStore.edit { it[Keys.LIBRARY_AUTO_REMOVE_WATCHED] = enabled }

    override suspend fun setAutoRemoveGraceHours(hours: Int) =
        dataStore.edit {
            it[Keys.LIBRARY_AUTO_REMOVE_GRACE_HOURS] =
                hours.coerceIn(MIN_GRACE_HOURS, MAX_GRACE_HOURS)
        }

    override suspend fun setSponsorBlockEnabled(enabled: Boolean) =
        dataStore.edit { it[Keys.SPONSORBLOCK_ENABLED] = enabled }

    override suspend fun setSponsorBlockCategories(categories: Set<SponsorCategory>) = dataStore.edit {
        it[Keys.SPONSORBLOCK_CATEGORIES] = categories.map { c -> c.name }.toSet()
    }

    override suspend fun setShowDislikeEstimates(enabled: Boolean) =
        dataStore.edit { it[Keys.DISLIKES_SHOW_ESTIMATES] = enabled }

    private companion object {
        /** ExoPlayer accepts 0.25x to 4x; the useful range is narrower. */
        const val MIN_SPEED = 0.25f
        const val MAX_SPEED = 3.0f

        const val MIN_SEEK_SEC = 5
        const val MAX_SEEK_SEC = 30

        /** The 24h default is the spec's; going below an hour would undo the point of the grace. */
        const val MIN_GRACE_HOURS = 1
        const val MAX_GRACE_HOURS = 168
    }
}
