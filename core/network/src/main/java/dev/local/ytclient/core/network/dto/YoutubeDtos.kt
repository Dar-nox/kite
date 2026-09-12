package dev.local.ytclient.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * YouTube Data API v3 response shapes.
 *
 * Every field is nullable or defaulted on purpose. The API omits fields it has no data for rather
 * than sending nulls or zeros — a video with no stats has no `statistics` object at all — and a
 * non-nullable DTO would turn that into a decoding failure that takes the whole page down with it.
 *
 * `part=` in the request decides which of these sections are populated, so a call asking for
 * `snippet` gets no `contentDetails` even though the field exists here.
 */

@Serializable
data class ApiListResponse<T>(
    val nextPageToken: String? = null,
    val pageInfo: PageInfo? = null,
    val items: List<T> = emptyList(),
)

@Serializable
data class PageInfo(
    val totalResults: Int = 0,
    val resultsPerPage: Int = 0,
)

// --- Channels ---

@Serializable
data class ChannelDto(
    val id: String,
    val snippet: ChannelSnippet? = null,
    val contentDetails: ChannelContentDetails? = null,
    val statistics: ChannelStatistics? = null,
)

@Serializable
data class ChannelSnippet(
    val title: String = "",
    val description: String = "",
    val customUrl: String? = null,
    val thumbnails: Thumbnails? = null,
)

@Serializable
data class ChannelContentDetails(
    val relatedPlaylists: RelatedPlaylists? = null,
)

@Serializable
data class RelatedPlaylists(
    val uploads: String? = null,
)

@Serializable
data class ChannelStatistics(
    /** The API sends counts as strings. */
    val subscriberCount: String? = null,
    val videoCount: String? = null,
    val hiddenSubscriberCount: Boolean = false,
)

// --- Thumbnails ---

@Serializable
data class Thumbnails(
    @SerialName("default") val defaultThumb: Thumb? = null,
    val medium: Thumb? = null,
    val high: Thumb? = null,
    val standard: Thumb? = null,
    val maxres: Thumb? = null,
) {
    /** Highest available, falling back down the ladder. Matches the `thumbnailUrl` spec note. */
    fun best(): Thumb? = maxres ?: standard ?: high ?: medium ?: defaultThumb
}

@Serializable
data class Thumb(
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null,
) {
    /** Aspect as width/height. Null when the API omitted the dimensions. */
    val aspectRatio: Float?
        get() {
            val w = width ?: return null
            val h = height ?: return null
            return if (h == 0) null else w.toFloat() / h
        }
}

// --- Videos ---

@Serializable
data class VideoDto(
    val id: String,
    val snippet: VideoSnippet? = null,
    val contentDetails: VideoContentDetails? = null,
    val statistics: VideoStatistics? = null,
)

@Serializable
data class VideoSnippet(
    val title: String = "",
    val description: String = "",
    val channelId: String = "",
    val channelTitle: String = "",
    val publishedAt: String = "",
    val categoryId: String? = null,
    val thumbnails: Thumbnails? = null,
)

@Serializable
data class VideoContentDetails(
    /** ISO 8601 duration, e.g. `PT4M12S`. */
    val duration: String? = null,
    /** `2d` or `3d`. */
    val dimension: String? = null,
    val caption: String? = null,
)

@Serializable
data class VideoStatistics(
    val viewCount: String? = null,
    val likeCount: String? = null,
    /** Removed from the API in December 2021; always absent now. See the RYD client. */
    val dislikeCount: String? = null,
)

// --- Playlists and their items ---

@Serializable
data class PlaylistDto(
    val id: String,
    val snippet: PlaylistSnippet? = null,
    val contentDetails: PlaylistContentDetails? = null,
)

@Serializable
data class PlaylistSnippet(
    val title: String = "",
    val thumbnails: Thumbnails? = null,
)

@Serializable
data class PlaylistContentDetails(
    val itemCount: Int = 0,
)

@Serializable
data class PlaylistItemDto(
    val id: String? = null,
    val snippet: PlaylistItemSnippet? = null,
    val contentDetails: PlaylistItemContentDetails? = null,
)

@Serializable
data class PlaylistItemSnippet(
    val title: String = "",
    val description: String = "",
    val channelId: String = "",
    val channelTitle: String = "",
    val publishedAt: String = "",
    val thumbnails: Thumbnails? = null,
    val resourceId: ResourceId? = null,
)

@Serializable
data class ResourceId(
    val kind: String? = null,
    val videoId: String? = null,
)

@Serializable
data class PlaylistItemContentDetails(
    val videoId: String? = null,
    val videoPublishedAt: String? = null,
)

// --- Search ---

@Serializable
data class SearchResultDto(
    val id: SearchResourceId? = null,
    val snippet: SearchSnippet? = null,
)

@Serializable
data class SearchResourceId(
    val kind: String? = null,
    val videoId: String? = null,
    val channelId: String? = null,
    val playlistId: String? = null,
)

@Serializable
data class SearchSnippet(
    val title: String = "",
    val description: String = "",
    val channelId: String = "",
    val channelTitle: String = "",
    val publishedAt: String = "",
    val thumbnails: Thumbnails? = null,
)

// --- Comments ---

@Serializable
data class CommentThreadDto(
    val id: String? = null,
    val snippet: CommentThreadSnippet? = null,
    val replies: CommentReplies? = null,
)

@Serializable
data class CommentThreadSnippet(
    val topLevelComment: CommentDto? = null,
    val totalReplyCount: Int = 0,
)

@Serializable
data class CommentReplies(
    val comments: List<CommentDto> = emptyList(),
)

@Serializable
data class CommentDto(
    val id: String? = null,
    val snippet: CommentSnippet? = null,
)

@Serializable
data class CommentSnippet(
    val textDisplay: String = "",
    val authorDisplayName: String = "",
    val authorChannelId: AuthorChannelId? = null,
    val publishedAt: String = "",
    val likeCount: Int = 0,
)

@Serializable
data class AuthorChannelId(
    val value: String? = null,
)
