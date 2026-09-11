package dev.local.ytclient.core.designsystem.util

import java.util.Locale

/**
 * Presentation-only formatting shared by every feature.
 *
 * Kept in the design system because these strings are part of the visual language — the metadata
 * line reads the same on a feed card, a library row, and a playlist item. They are deliberately
 * locale-aware but allocation-light; none of them touch the network or the clock on their own
 * (callers pass `now`), which keeps them trivially testable.
 */
object KiteFormat {

    /** `4:12`, or `1:02:03` once an hour is reached. Negative and zero collapse to `0:00`. */
    fun duration(totalSeconds: Int): String {
        val s = totalSeconds.coerceAtLeast(0)
        val hours = s / 3600
        val minutes = (s % 3600) / 60
        val seconds = s % 60
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%d:%02d", minutes, seconds)
        }
    }

    /** `1.2M views`, `845K views`, `12 views`. Null hides the whole segment. */
    fun views(count: Long?): String? {
        if (count == null || count < 0) return null
        return "${compactNumber(count)} ${plural(count, "view")}"
    }

    /** `1.2M`, `845K`, `12`. Used for view and subscriber counts. */
    fun compactNumber(value: Long): String {
        val abs = kotlin.math.abs(value)
        return when {
            abs >= 1_000_000_000L -> trimZero(value / 1_000_000_000.0) + "B"
            abs >= 1_000_000L -> trimZero(value / 1_000_000.0) + "M"
            abs >= 1_000L -> trimZero(value / 1_000.0) + "K"
            else -> value.toString()
        }
    }

    /** Relative age of a publish date. `now` is injected so previews and tests stay deterministic. */
    fun relativeDate(publishedAt: Long, now: Long): String {
        val seconds = ((now - publishedAt) / 1000L).coerceAtLeast(0)
        return when {
            seconds < 60 -> "just now"
            seconds < 3600 -> plural(seconds / 60, "minute") + " ago"
            seconds < 86_400 -> plural(seconds / 3600, "hour") + " ago"
            seconds < 604_800 -> plural(seconds / 86_400, "day") + " ago"
            seconds < 2_592_000 -> plural(seconds / 604_800, "week") + " ago"
            seconds < 31_536_000 -> plural(seconds / 2_592_000, "month") + " ago"
            else -> plural(seconds / 31_536_000, "year") + " ago"
        }
    }

    /**
     * Joins the parts of a metadata line with the separator the design uses, dropping nulls and
     * blanks so an unavailable value never leaves a dangling ` · `.
     */
    fun metaLine(vararg parts: String?): String =
        parts.filterNotNull().filter { it.isNotBlank() }.joinToString(" · ")

    /** `2:14 left` for a partially watched item, falling back to the plain duration. */
    fun timeRemaining(positionSec: Int, durationSec: Int): String =
        duration((durationSec - positionSec).coerceAtLeast(0)) + " left"

    /** `N videos · M hours` for a playlist header. */
    fun playlistSummary(itemCount: Int, totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val time = when {
            hours > 0 -> "$hours ${plural(hours, "hour").substringAfter(' ')}"
            else -> "$minutes ${plural(minutes, "minute").substringAfter(' ')}"
        }
        return metaLine(
            "$itemCount ${plural(itemCount.toLong(), "video")}",
            time,
        )
    }

    /** `12%` for the progressive-loading banner. */
    fun percent(loaded: Int, total: Int): String {
        if (total <= 0) return "0%"
        val pct = ((loaded.toFloat() / total) * 100).toInt().coerceIn(0, 100)
        return "$pct%"
    }

    private fun plural(count: Long, noun: String): String =
        "$count $noun" + if (count == 1L) "" else "s"

    /** Drops a trailing `.0` so `2.0M` reads `2M` while `1.2M` keeps its decimal. */
    private fun trimZero(value: Double): String {
        val rounded = Math.round(value * 10) / 10.0
        return if (rounded % 1.0 == 0.0) rounded.toInt().toString()
        else String.format(Locale.US, "%.1f", rounded)
    }
}
