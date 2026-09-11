package dev.local.ytclient.core.data.filter

import dev.local.ytclient.core.datastore.ShortsMode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The minimum a list item must expose to be filterable.
 *
 * Domain models across features implement this, which is what lets one pipeline serve the home
 * feed, subscriptions, search, up next, playlist contents, and comments without any of them knowing
 * about each other.
 */
interface Filterable {
    val videoId: String
    val channelId: String
    val title: String
    val description: String?
    val isShort: Boolean
    val isVertical: Boolean
}

/** A stored keyword rule. */
data class KeywordRule(
    val term: String,
    val matchTitle: Boolean,
    val matchDescription: Boolean,
)

/** Why an item was removed. The UI reports these, because filtering is never silent. */
enum class FilterReason {
    BlockedChannel,
    Keyword,
    ShortsContained,
    HiddenByUser,
}

/**
 * What a filter pass removed.
 *
 * Both the visible list and the hidden counts come back together: `CLAUDE.md` requires that a list
 * which looks empty for an unexplained reason is treated as a bug, so every caller has the numbers
 * needed to render "N results hidden — Show".
 */
data class Filtered<T>(
    val visible: List<T>,
    val hiddenByReason: Map<FilterReason, Int> = emptyMap(),
) {
    val hiddenCount: Int get() = hiddenByReason.values.sum()

    /** True when the list is empty *only* because filters removed everything. */
    val emptiedByFilters: Boolean get() = visible.isEmpty() && hiddenCount > 0

    companion object {
        fun <T> unchanged(items: List<T>): Filtered<T> = Filtered(items)
    }
}

/** Everything the pipeline needs to decide, resolved once per emission. */
data class FilterConfig(
    val blockedChannelIds: Set<String> = emptySet(),
    val keywordRules: List<KeywordRule> = emptyList(),
    val hiddenVideoIds: Set<String> = emptySet(),
    val shortsMode: ShortsMode = ShortsMode.Default,
    val treatVerticalAsShort: Boolean = false,
    /**
     * Set on the Shorts tab, which deliberately inverts the Shorts rule: it shows only Shorts
     * instead of hiding them. In Off mode the tab does not exist, so this never combines with it.
     */
    val shortsOnly: Boolean = false,
    /** "Show" was tapped: hidden items are revealed inline for this session only. */
    val revealHidden: Boolean = false,
)

/**
 * Blocked channels, Shorts containment, keyword filters, and swipe-hidden items, applied at query
 * time and never at insert time.
 *
 * Cached rows stay pristine, so toggling a setting takes effect on the next emission with no
 * re-fetch, and unblocking a channel restores its videos instead of requiring a re-sync.
 *
 * Pure and synchronous: no coroutine, no database, no clock. That is deliberate — it makes the
 * rules testable in isolation, which matters because a filter that is wrong in one direction hides
 * content the user asked for and in the other shows content they blocked.
 */
@Singleton
class FilterPipeline @Inject constructor() {

    fun <T : Filterable> apply(items: List<T>, config: FilterConfig): Filtered<T> {
        val visible = ArrayList<T>(items.size)
        val hidden = LinkedHashMap<FilterReason, Int>()

        for (item in items) {
            val reason = reasonFor(item, config)
            if (reason == null) {
                visible.add(item)
            } else {
                hidden[reason] = (hidden[reason] ?: 0) + 1
            }
        }

        if (config.revealHidden && hidden.isNotEmpty()) {
            // Revealing is additive for the session: the counts stay so the notice can explain
            // itself, but nothing is withheld.
            return Filtered(visible = items, hiddenByReason = hidden)
        }
        return Filtered(visible = visible, hiddenByReason = hidden)
    }

    /** The first rule that removes [item], or null when it survives. Order defines the reason shown. */
    private fun <T : Filterable> reasonFor(item: T, config: FilterConfig): FilterReason? = when {
        item.channelId in config.blockedChannelIds -> FilterReason.BlockedChannel
        item.videoId in config.hiddenVideoIds -> FilterReason.HiddenByUser
        matchesKeyword(item, config.keywordRules) -> FilterReason.Keyword
        shortsBlocked(item, config) -> FilterReason.ShortsContained
        else -> null
    }

    /**
     * Shorts containment.
     *
     * Contained hides Shorts from every surface except the Shorts tab; Off hides them everywhere
     * including that tab; Everywhere passes everything through. On the Shorts tab the rule inverts,
     * so non-Shorts are what get removed.
     */
    private fun <T : Filterable> shortsBlocked(item: T, config: FilterConfig): Boolean {
        if (config.shortsMode == ShortsMode.Everywhere) return false
        val isShort = isShort(item, config.treatVerticalAsShort)
        return if (config.shortsOnly) !isShort else isShort
    }

    private fun <T : Filterable> matchesKeyword(item: T, rules: List<KeywordRule>): Boolean {
        if (rules.isEmpty()) return false
        val title = item.title.lowercase()
        val description = item.description?.lowercase()
        return rules.any { rule ->
            val term = rule.term.trim().lowercase()
            if (term.isEmpty()) return@any false
            (rule.matchTitle && title.contains(term)) ||
                (rule.matchDescription && description?.contains(term) == true)
        }
    }

    companion object {
        /**
         * Whether an item counts as a Short.
         *
         * Detection is the `/shorts/` path plus aspect ratio. Duration alone is deliberately not
         * used — Shorts run up to three minutes, so a duration rule would sweep up normal videos.
         */
        fun isShort(isShort: Boolean, isVertical: Boolean, treatVerticalAsShort: Boolean): Boolean =
            isShort || (treatVerticalAsShort && isVertical)

        private fun <T : Filterable> isShort(item: T, treatVerticalAsShort: Boolean): Boolean =
            isShort(item.isShort, item.isVertical, treatVerticalAsShort)
    }
}
