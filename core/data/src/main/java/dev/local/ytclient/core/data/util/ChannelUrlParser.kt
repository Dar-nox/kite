package dev.local.ytclient.core.data.util

/**
 * What kind of channel reference the user pasted.
 *
 * The API resolves these differently — `id`, `forHandle`, and `forUsername` are separate parameters
 * and only one may be used per call — so guessing wrong costs 100 units of `search` for something a
 * free `list` would have answered.
 */
sealed interface ChannelRef {
    /** `UC…` channel id. */
    data class Id(val value: String) : ChannelRef

    /** `@handle`, with or without the `@`. */
    data class Handle(val value: String) : ChannelRef

    /** Legacy `/user/` name. */
    data class Username(val value: String) : ChannelRef

    /**
     * Legacy `/c/` custom name, or anything else. Not resolvable by any `list` parameter, so it
     * falls back to `search` — the one case where 100 units is justified.
     */
    data class Custom(val value: String) : ChannelRef
}

/**
 * Turns a pasted URL, handle, or bare id into a [ChannelRef].
 *
 * Deliberately tolerant: users paste from the share sheet, so the input arrives with `?si=` tracking
 * parameters, a `/videos` or `/featured` suffix, `youtu.be` short links, and sometimes no scheme at
 * all.
 */
object ChannelUrlParser {

    private val CHANNEL_ID = Regex("""^UC[\w-]{20,}$""")
    private val HANDLE = Regex("""^@?[\w.\-]{2,}$""")

    fun parse(raw: String): ChannelRef? {
        val input = raw.trim()
        if (input.isEmpty()) return null

        val withoutScheme = input
            .removePrefix("https://")
            .removePrefix("http://")
            .removePrefix("www.")
            .removePrefix("m.")

        val host = withoutScheme.substringBefore('/', missingDelimiterValue = "")
        val isYouTube = host.isEmpty() ||
            host == "youtube.com" ||
            host.endsWith(".youtube.com") ||
            host == "youtu.be"

        // Strip the query and fragment, then any trailing slash.
        val path = if (isYouTube) {
            withoutScheme
                .substringAfter('/', missingDelimiterValue = withoutScheme)
                .substringBefore('?')
                .substringBefore('#')
                .trim('/')
        } else {
            return null
        }

        // A bare id or handle, pasted without a URL.
        if (path.isEmpty()) {
            return classifyBare(input)
        }

        val segments = path.split('/')
        return when (segments.first()) {
            "channel" -> segments.getOrNull(1)
                ?.takeIf { CHANNEL_ID.matches(it) }
                ?.let { ChannelRef.Id(it) }

            "user" -> segments.getOrNull(1)?.let { ChannelRef.Username(it) }
            "c" -> segments.getOrNull(1)?.let { ChannelRef.Custom(it) }

            // "/@handle", "/@handle/videos", "/@handle/featured"
            else -> segments.first()
                .removePrefix("@")
                .takeIf { it.isNotEmpty() && HANDLE.matches("@$it") }
                ?.let { ChannelRef.Handle(it) }
        }
    }

    private fun classifyBare(input: String): ChannelRef? = when {
        input.startsWith("@") -> ChannelRef.Handle(input.removePrefix("@"))
        CHANNEL_ID.matches(input) -> ChannelRef.Id(input)
        else -> null
    }
}
