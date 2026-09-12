package dev.local.ytclient.core.data.mapper

import java.time.Instant
import java.time.format.DateTimeParseException

/**
 * The two formats the Data API uses that are not directly storable.
 *
 * Durations arrive as ISO 8601 (`PT1H2M3S`) and the schema stores seconds; timestamps arrive as
 * ISO 8601 instants and the schema stores epoch millis. Both parsers return a fallback rather than
 * throwing: one malformed field in a 50-item page must not cost the whole page.
 */
object Iso8601 {

    private val DURATION = Regex(
        """^P(?:(\d+)D)?T?(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?$""",
        RegexOption.IGNORE_CASE,
    )

    /** `PT4M12S` -> 252. Returns 0 for null, blank, or unparseable input. */
    fun durationSeconds(value: String?): Int {
        if (value.isNullOrBlank()) return 0
        val match = DURATION.matchEntire(value.trim()) ?: return 0
        val (days, hours, minutes, seconds) = match.destructured
        return days.toIntOrZero() * 86_400 +
            hours.toIntOrZero() * 3_600 +
            minutes.toIntOrZero() * 60 +
            seconds.toIntOrZero()
    }

    /** `2024-01-01T00:00:00Z` -> epoch millis. Returns [fallback] for anything unparseable. */
    fun instantMillis(value: String?, fallback: Long = 0L): Long {
        if (value.isNullOrBlank()) return fallback
        return try {
            Instant.parse(value).toEpochMilli()
        } catch (e: DateTimeParseException) {
            fallback
        }
    }

    /** The API sends counts as decimal strings, sometimes with the field absent entirely. */
    fun long(value: String?): Long? = value?.toLongOrNull()

    private fun String.toIntOrZero(): Int = if (isEmpty()) 0 else toIntOrNull() ?: 0
}
