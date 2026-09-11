package dev.local.ytclient.core.datastore

import kotlinx.coroutines.flow.Flow

/**
 * Read and write every setting.
 *
 * Exposed as one [settings] flow plus a few narrow ones: the filter pipeline and the tab bar each
 * need a single value, and mapping the whole snapshot in a dozen places just to read one field
 * makes recomposition wider than it needs to be.
 *
 * Every setter is a `suspend fun` because DataStore writes off the main thread.
 */
interface SettingsRepository {

    val settings: Flow<KiteSettings>

    val feedLayout: Flow<FeedLayout>
    val shortsMode: Flow<ShortsMode>
    val sponsorBlockEnabled: Flow<Boolean>
    val sponsorBlockCategories: Flow<Set<SponsorCategory>>
    val defaultSpeed: Flow<Float>
    val rememberSpeedPerChannel: Flow<Boolean>

    suspend fun setFeedLayout(layout: FeedLayout)
    suspend fun setGridColumns(columns: Int)
    suspend fun setExpandColumnsLandscape(enabled: Boolean)
    suspend fun setFeedFilters(filters: Set<String>)

    suspend fun setShortsMode(mode: ShortsMode)
    suspend fun setTreatVerticalAsShort(enabled: Boolean)

    suspend fun setBackgroundPlay(enabled: Boolean)
    suspend fun setRememberSpeedPerChannel(enabled: Boolean)
    suspend fun setDefaultSpeed(speed: Float)
    suspend fun setQualityWifi(quality: String)
    suspend fun setQualityCellular(quality: String)
    suspend fun setSeekIntervalSec(seconds: Int)
    suspend fun setGestureGuidesShown()

    suspend fun setWatchTabDefault(tab: WatchTab)

    suspend fun setAutoArchiveDays(days: Int)
    suspend fun setAutoRemoveWatched(enabled: Boolean)
    suspend fun setAutoRemoveGraceHours(hours: Int)

    suspend fun setSponsorBlockEnabled(enabled: Boolean)
    suspend fun setSponsorBlockCategories(categories: Set<SponsorCategory>)
    suspend fun setShowDislikeEstimates(enabled: Boolean)
}
