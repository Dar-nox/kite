package dev.local.ytclient.core.datastore

/**
 * Settings, following the DataStore key list in `DATA_MODEL.md`.
 *
 * Stored as Preferences rather than Room because these are single scalar values the app reads on
 * every frame of navigation, not rows anything queries. The persisted form of each enum is its
 * `name`, so renaming an entry is a migration rather than a refactor.
 */

/** The five feed layouts. All are arrangements of the same three card components. */
enum class FeedLayout(val displayName: String, val description: String) {
    Large("Large", "One per row, biggest thumbnails"),
    Compact("Compact", "Thumbnail left, text right"),
    Hybrid("Hybrid", "One large card, then compact rows"),
    Grid("Grid", "Even tiles"),
    HybridGrid("Hybrid grid", "One large card, then grid tiles"),
    ;

    val isGrid: Boolean get() = this == Grid || this == HybridGrid

    companion object {
        val Default: FeedLayout = Large

        fun fromName(value: String?): FeedLayout =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: Default
    }
}

/** Shorts containment. One setting, three modes. */
enum class ShortsMode {
    /** Shorts appear only in the Shorts tab — never in feeds, search, or up next. */
    Contained,

    /** The Shorts tab is removed entirely; no Shorts anywhere. */
    Off,

    /** Mixed into feeds, like the official app. */
    Everywhere;

    companion object {
        val Default: ShortsMode = Contained

        fun fromName(value: String?): ShortsMode =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: Default
    }
}

/** Which watch-screen tab opens first. */
enum class WatchTab(val displayName: String) {
    UpNext("Up next"),
    Comments("Comments");

    companion object {
        val Default: WatchTab = UpNext

        fun fromName(value: String?): WatchTab =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: Default
    }
}

/** SponsorBlock categories. The first three are on by default, per `FEATURES.md`. */
enum class SponsorCategory(val displayName: String, val enabledByDefault: Boolean) {
    Sponsor("Sponsor", true),
    Intro("Intro", true),
    SelfPromo("Self-promo", true),
    Outro("Outro", false),
    Interaction("Interaction", false),
    ;

    companion object {
        val Defaults: Set<SponsorCategory> = entries.filter { it.enabledByDefault }.toSet()

        fun fromName(value: String?): SponsorCategory? =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
    }
}

/** Quality presets offered per network type. */
object Quality {
    const val Auto = "auto"
    val Options: List<String> = listOf(Auto, "2160p", "1440p", "1080p", "720p", "480p", "360p")
}

/** Every setting, read as one immutable snapshot. */
data class KiteSettings(
    // --- Feed ---
    val feedLayout: FeedLayout = FeedLayout.Default,
    val gridColumns: Int = GridColumnLimits.Default,
    val expandColumnsLandscape: Boolean = false,
    val feedFilters: Set<String> = emptySet(),

    // --- Shorts ---
    val shortsMode: ShortsMode = ShortsMode.Default,
    val treatVerticalAsShort: Boolean = false,

    // --- Player ---
    val backgroundPlay: Boolean = true,
    val rememberSpeedPerChannel: Boolean = true,
    val defaultSpeed: Float = 1.0f,
    val qualityWifi: String = Quality.Auto,
    val qualityCellular: String = "720p",
    val seekIntervalSec: Int = 10,
    val showGestureGuides: Boolean = true,

    // --- Watch ---
    val watchTabDefault: WatchTab = WatchTab.Default,

    // --- Library ---
    val autoArchiveDays: Int = 0,
    val autoRemoveWatched: Boolean = false,
    val autoRemoveGraceHours: Int = 24,

    // --- SponsorBlock and dislikes ---
    val sponsorBlockEnabled: Boolean = true,
    val sponsorBlockCategories: Set<SponsorCategory> = SponsorCategory.Defaults,
    val showDislikeEstimates: Boolean = true,
)

/**
 * Limits for the stored columns preference.
 *
 * Only the 2-3 phone range lives here; how many columns are *drawn* is derived from available width
 * at render time by `GridColumns` in `:core:designsystem`, which is the piece that makes tablets
 * and foldables work without their own setting.
 */
object GridColumnLimits {
    const val Min: Int = 2
    const val Max: Int = 3
    const val Default: Int = 2
}
