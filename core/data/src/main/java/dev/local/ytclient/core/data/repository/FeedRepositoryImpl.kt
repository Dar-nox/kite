package dev.local.ytclient.core.data.repository

import dev.local.ytclient.core.data.filter.FilterConfig
import dev.local.ytclient.core.data.filter.FilterPipeline
import dev.local.ytclient.core.data.filter.KeywordRule
import dev.local.ytclient.core.data.model.FeedChip
import dev.local.ytclient.core.data.model.FeedContent
import dev.local.ytclient.core.data.model.FeedItem
import dev.local.ytclient.core.data.model.SyncResult
import dev.local.ytclient.core.database.dao.ChannelDao
import dev.local.ytclient.core.database.dao.HiddenVideoDao
import dev.local.ytclient.core.database.dao.KeywordFilterDao
import dev.local.ytclient.core.database.dao.VideoDao
import dev.local.ytclient.core.database.dao.WatchStateDao
import dev.local.ytclient.core.database.model.ChannelEntity
import dev.local.ytclient.core.database.model.KeywordFilterEntity
import dev.local.ytclient.core.database.model.VideoEntity
import dev.local.ytclient.core.database.model.WatchStateEntity
import dev.local.ytclient.core.datastore.KiteSettings
import dev.local.ytclient.core.datastore.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Room-backed feed.
 *
 * Reads only. Nothing here issues a network call: the sync workers write to the same tables and the
 * `Flow` carries the change to the screen, which is what keeps "never block the UI on a network
 * call" true by construction rather than by discipline.
 *
 * Chip filters run before the pipeline so the hidden count only ever describes removals the user
 * did not ask for. Watching a video and choosing "Unwatched" is a choice; having twelve videos
 * withheld by a block list is something the UI owes an explanation for.
 */
@Singleton
class FeedRepositoryImpl @Inject constructor(
    private val videoDao: VideoDao,
    private val channelDao: ChannelDao,
    private val watchStateDao: WatchStateDao,
    private val hiddenVideoDao: HiddenVideoDao,
    private val keywordFilterDao: KeywordFilterDao,
    private val settingsRepository: SettingsRepository,
    private val filterPipeline: FilterPipeline,
    private val syncRepository: SyncRepository,
) : FeedRepository {

    override suspend fun refresh(): SyncResult = syncRepository.refreshFeed()

    /** The five inputs the feed is derived from, captured together so they cannot tear. */
    private data class Input(
        val videos: List<VideoEntity>,
        val channels: List<ChannelEntity>,
        val watchStates: List<WatchStateEntity>,
        val hiddenVideoIds: List<String>,
        val keywordFilters: List<KeywordFilterEntity>,
    )

    override fun observeFeed(
        activeChips: Set<FeedChip>,
        revealHidden: Boolean,
    ): Flow<FeedContent> {
        val rows: Flow<Input> = combine(
            videoDao.observeSubscribedUploads(),
            channelDao.observeSubscribed(),
            watchStateDao.observeAll(),
            hiddenVideoDao.observeIds(),
            keywordFilterDao.observeAll(),
        ) { videos, channels, watchStates, hidden, keywords ->
            Input(videos, channels, watchStates, hidden, keywords)
        }

        return combine(rows, settingsRepository.settings) { input, settings ->
            build(input, settings, activeChips, revealHidden)
        }
    }

    private fun build(
        input: Input,
        settings: KiteSettings,
        activeChips: Set<FeedChip>,
        revealHidden: Boolean,
    ): FeedContent {
        val channelsById = input.channels.associateBy { it.channelId }
        val stateByVideo = input.watchStates.associateBy { it.videoId }

        val all = input.videos.map { video ->
            val channel = channelsById[video.channelId]
            val state = stateByVideo[video.videoId]
            FeedItem(
                videoId = video.videoId,
                title = video.title,
                channelId = video.channelId,
                description = video.description,
                isShort = video.isShort,
                isVertical = video.isVertical,
                channelName = channel?.title,
                channelAvatarUrl = channel?.avatarUrl,
                thumbnailUrl = video.thumbnailUrl,
                durationSec = video.durationSec,
                publishedAt = video.publishedAt,
                viewCount = video.viewCount,
                positionSec = state?.positionSec ?: 0,
                completed = state?.completed == true,
            )
        }

        val chipFiltered = all.filter { video -> matchesChips(video, activeChips) }

        val filtered = filterPipeline.apply(
            items = chipFiltered,
            config = FilterConfig(
                // The subscribed list is enough here: the feed only ever contains uploads from
                // subscribed channels, so a blocked channel that could contribute is in it. Other
                // surfaces (search, up next) use channelDao.observeBlockedIds() for the full set.
                blockedChannelIds = input.channels.filter { it.isBlocked }.map { it.channelId }.toSet(),
                keywordRules = input.keywordFilters.map {
                    KeywordRule(
                        term = it.term,
                        matchTitle = it.matchTitle,
                        matchDescription = it.matchDescription,
                    )
                },
                hiddenVideoIds = input.hiddenVideoIds.toSet(),
                shortsMode = settings.shortsMode,
                treatVerticalAsShort = settings.treatVerticalAsShort,
                revealHidden = revealHidden,
            ),
        )

        return FeedContent(
            items = filtered.visible,
            hiddenByReason = filtered.hiddenByReason,
            activeChips = activeChips,
        )
    }

    /** Chips are additive: an item survives if it satisfies every chip that is switched on. */
    private fun matchesChips(video: FeedItem, chips: Set<FeedChip>): Boolean {
        if (chips.isEmpty() || FeedChip.All in chips) return true
        return chips.all { chip ->
            when (chip) {
                FeedChip.All -> true
                FeedChip.Unwatched -> !video.completed
                FeedChip.Under10m -> video.durationSec in 1 until UNDER_TEN_MINUTES_SEC
            }
        }
    }

    override suspend fun hideVideo(videoId: String) =
        hiddenVideoDao.hide(videoId, System.currentTimeMillis())

    override suspend fun unhideVideo(videoId: String) = hiddenVideoDao.unhide(videoId)

    private companion object {
        const val UNDER_TEN_MINUTES_SEC = 600
    }
}
