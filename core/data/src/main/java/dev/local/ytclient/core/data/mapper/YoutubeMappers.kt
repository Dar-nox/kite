package dev.local.ytclient.core.data.mapper

import dev.local.ytclient.core.database.model.ChannelEntity
import dev.local.ytclient.core.database.model.PlaylistEntity
import dev.local.ytclient.core.database.model.PlaylistItemEntity
import dev.local.ytclient.core.database.model.VideoEntity
import dev.local.ytclient.core.network.dto.ChannelDto
import dev.local.ytclient.core.network.dto.PlaylistDto
import dev.local.ytclient.core.network.dto.PlaylistItemDto
import dev.local.ytclient.core.network.dto.VideoDto

/**
 * DTO -> entity mapping.
 *
 * The split that matters: nothing here decides whether a channel is blocked or a video is hidden.
 * Those are read-time concerns, so a mapper never has to know about them and a re-sync can never
 * overwrite a filter decision.
 *
 * Mappers preserve user-owned columns. Re-syncing a channel must not reset `defaultSpeed`,
 * `autoSaveTarget`, or `isBlocked`, so [ChannelDto.toEntity] produces a row with those at their
 * defaults and the repository upserts with an explicit column list.
 *
 * **Shorts detection is the one thing this layer cannot do.** The Data API exposes no `/shorts/`
 * path and no real aspect ratio — its thumbnails are always 16:9 — and duration alone is ruled out
 * by `FEATURES.md` because Shorts run up to three minutes. So metadata sync leaves `isShort` false,
 * records `isVertical` only when a thumbnail genuinely is, and the stream resolver in `:core:player`
 * writes `isShort` back once it has seen the watch page. Containment is exact for anything the
 * extractor has resolved and approximate before that; the "treat vertical uploads as Shorts"
 * setting exists for users who want it aggressive.
 */
fun ChannelDto.toEntity(): ChannelEntity = ChannelEntity(
    channelId = id,
    title = snippet?.title.orEmpty().ifBlank { id },
    avatarUrl = snippet?.thumbnails?.best()?.url,
    uploadsPlaylistId = contentDetails?.relatedPlaylists?.uploads,
    subscriberCount = Iso8601.long(statistics?.subscriberCount),
)

/**
     * @param channelId the channel this video was fetched under. The snippet carries one, but for a
     *   playlist item belonging to someone else's playlist that is the *uploader*, which is what we
     *   want; the fallback covers a snippet the API omitted.
     */
fun VideoDto.toEntity(channelIdFallback: String? = null, cachedAt: Long): VideoEntity {
    val thumb = snippet?.thumbnails?.best()
    val aspect = thumb?.aspectRatio
    return VideoEntity(
        videoId = id,
        title = snippet?.title.orEmpty().ifBlank { id },
        channelId = snippet?.channelId?.takeIf { it.isNotBlank() } ?: channelIdFallback.orEmpty(),
        durationSec = Iso8601.durationSeconds(contentDetails?.duration),
        publishedAt = Iso8601.instantMillis(snippet?.publishedAt, fallback = cachedAt),
        thumbnailUrl = thumb?.url.orEmpty(),
        viewCount = Iso8601.long(statistics?.viewCount),
        // The Data API cannot answer this; the stream resolver corrects it. See the class doc.
        isShort = false,
        isVertical = aspect != null && aspect < 1.0f,
        description = snippet?.description,
        categoryId = snippet?.categoryId,
        cachedAt = cachedAt,
    )
}

fun PlaylistDto.toEntity(folderId: Long? = null): PlaylistEntity = PlaylistEntity(
    playlistId = id,
    title = snippet?.title.orEmpty().ifBlank { id },
    thumbnailUrl = snippet?.thumbnails?.best()?.url,
    itemCount = contentDetails?.itemCount ?: 0,
    folderId = folderId,
)

/**
     * @param position the item's index in the playlist, assigned by the caller as pages arrive.
     *   The API does not return a position, and `playlist_items` keys on it, so the sync loop is
     *   what makes it stable.
     */
fun PlaylistItemDto.toEntity(
playlistId: String,
position: Int,
cachedAt: Long,
): PlaylistItemEntity? {
    val videoId = contentDetails?.videoId ?: snippet?.resourceId?.videoId ?: return null
    return PlaylistItemEntity(
        playlistId = playlistId,
        position = position,
        videoId = videoId,
        addedAt = Iso8601.instantMillis(
            contentDetails?.videoPublishedAt ?: snippet?.publishedAt,
            fallback = cachedAt,
        ),
    )
}
