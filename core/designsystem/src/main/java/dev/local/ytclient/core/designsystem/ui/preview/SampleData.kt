package dev.local.ytclient.core.designsystem.ui.preview

import dev.local.ytclient.core.designsystem.ui.model.TagChipData
import dev.local.ytclient.core.designsystem.ui.model.VideoCardData
import dev.local.ytclient.core.designsystem.ui.theme.TagColorKey

/**
 * Sample data for component previews.
 *
 * One set of samples keeps every preview honest about real content: long titles that must clamp, a
 * partially watched item that must draw a progress bar, and tags spread across the palette.
 */
internal val sampleTags: List<TagChipData> = listOf(
    TagChipData(id = 1, name = "watch later", colorKey = TagColorKey.Green),
    TagChipData(id = 2, name = "reference", colorKey = TagColorKey.Blue),
    TagChipData(id = 3, name = "revisit", colorKey = TagColorKey.Amber),
)

internal val sampleVideoCard: VideoCardData = VideoCardData(
    videoId = "dQw4w9WgXcQ",
    title = "Building a room database that survives schema changes across a dozen releases",
    thumbnailUrl = null,
    durationSec = 1274,
    metaLine = "Some channel name · 845K views · 2 days ago",
    channelName = "Some channel name",
    channelAvatarUrl = null,
    progress = 0.42f,
    dimmed = false,
    isShort = false,
    tags = sampleTags,
)

internal val sampleCompletedCard: VideoCardData = sampleVideoCard.copy(
    progress = 1f,
    dimmed = true,
)

internal val sampleShortCard: VideoCardData = sampleVideoCard.copy(
    title = "Short vertical upload",
    durationSec = 47,
    isShort = true,
)
