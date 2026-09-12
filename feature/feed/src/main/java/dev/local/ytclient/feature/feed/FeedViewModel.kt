package dev.local.ytclient.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.local.ytclient.core.data.model.FeedChip
import dev.local.ytclient.core.data.model.FeedContent
import dev.local.ytclient.core.data.model.FeedItem
import dev.local.ytclient.core.data.model.SyncResult
import dev.local.ytclient.core.data.repository.ChannelRepository
import dev.local.ytclient.core.data.repository.FeedRepository
import dev.local.ytclient.core.data.repository.SubscribeResult
import dev.local.ytclient.core.datastore.SettingsRepository
import dev.local.ytclient.core.designsystem.ui.model.VideoCardData
import dev.local.ytclient.core.designsystem.util.KiteFormat
import dev.local.ytclient.core.network.api.ApiFailure
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Feed screen state.
 *
 * One immutable state per screen, per `CLAUDE.md`: the composable renders this and calls back into
 * the ViewModel, and never reaches past it for data.
 */
data class FeedUiState(
    val isLoading: Boolean = true,
    val items: List<VideoCardData> = emptyList(),
    val chips: List<FeedChip> = listOf(FeedChip.All),
    val activeChips: Set<FeedChip> = setOf(FeedChip.All),
    val hiddenCount: Int = 0,
    val hiddenRevealed: Boolean = false,
    val isEmpty: Boolean = false,
    val emptiedByFilters: Boolean = false,
    val isRefreshing: Boolean = false,
    /** Set when a refresh failed but cached rows are still on screen. */
    val staleNotice: String? = null,
    val addChannelOpen: Boolean = false,
    val addChannelBusy: Boolean = false,
    val addChannelError: String? = null,
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
    private val channelRepository: ChannelRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    /** Chips persist across sessions, so they live in settings and are read back from there. */
    private val activeChips: StateFlow<Set<FeedChip>> = settingsRepository.settings
        .map { settings ->
            settings.feedFilters
                .mapNotNull { FeedChip.fromName(it) }
                .toSet()
                .ifEmpty { setOf(FeedChip.All) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), setOf(FeedChip.All))

    private val hiddenRevealed = MutableStateFlow(false)
    private val transient = MutableStateFlow(Transient())

    /** Refresh/subscribe UI state that must not survive a process death. */
    private data class Transient(
        val isRefreshing: Boolean = false,
        val staleNotice: String? = null,
        val addChannelOpen: Boolean = false,
        val addChannelBusy: Boolean = false,
        val addChannelError: String? = null,
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<FeedUiState> =
        combine(activeChips, hiddenRevealed, transient) { chips, revealed, ui ->
            Triple(chips, revealed, ui)
        }.flatMapLatest { (chips, revealed, ui) ->
            feedRepository.observeFeed(activeChips = chips, revealHidden = revealed)
                // The chips and the transient UI state come from the combined stream, not from
                // `.value`: reading the source flows here would make this recomposition depend on
                // when the collector happened to run.
                .map { content -> content.toUiState(chips, revealed, ui) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), FeedUiState())

    /**
     * Chips are additive, except "All" which clears the others — leaving "All" selected alongside
     * "Unwatched" would imply a filter that does nothing.
     */
    fun onChipToggled(chip: FeedChip) {
        val next: Set<FeedChip> = when {
            chip == FeedChip.All -> setOf(FeedChip.All)
            chip in activeChips.value -> (activeChips.value - chip).ifEmpty { setOf(FeedChip.All) }
            else -> (activeChips.value + chip) - FeedChip.All
        }
        hiddenRevealed.value = false
        viewModelScope.launch {
            settingsRepository.setFeedFilters(next.map { it.name }.toSet())
        }
    }

    /**
     * Pull to refresh.
     *
     * On failure the cached list stays exactly as it is and a quiet notice appears — never an empty
     * screen, because there is almost always something cached to show.
     */
    fun onRefresh() {
        viewModelScope.launch {
            transient.update { it.copy(isRefreshing = true, staleNotice = null) }
            when (val result = feedRepository.refresh()) {
                is SyncResult.Success -> transient.update { it.copy(isRefreshing = false) }
                is SyncResult.Failure -> transient.update {
                    it.copy(isRefreshing = false, staleNotice = result.failure.userMessage())
                }
            }
        }
    }

    fun onDismissStaleNotice() = transient.update { it.copy(staleNotice = null) }

    fun onOpenAddChannel() = transient.update {
        it.copy(addChannelOpen = true, addChannelError = null)
    }

    fun onCloseAddChannel() = transient.update {
        it.copy(addChannelOpen = false, addChannelBusy = false, addChannelError = null)
    }

    /**
     * Subscribes to a pasted URL, handle, or channel id.
     *
     * With no OAuth this is how the feed gets populated at all, so the failure messages are written
     * to be actionable rather than technical.
     */
    fun onAddChannel(input: String) {
        if (input.isBlank()) return
        viewModelScope.launch {
            transient.update { it.copy(addChannelBusy = true, addChannelError = null) }
            when (val result = channelRepository.subscribe(input)) {
                is SubscribeResult.Success -> {
                    transient.update {
                        it.copy(addChannelOpen = false, addChannelBusy = false, addChannelError = null)
                    }
                    onRefresh()
                }
                is SubscribeResult.InvalidInput -> transient.update {
                    it.copy(
                        addChannelBusy = false,
                        addChannelError = "That doesn't look like a YouTube channel link.",
                    )
                }
                is SubscribeResult.NotFound -> transient.update {
                    it.copy(addChannelBusy = false, addChannelError = "No channel found for that.")
                }
                is SubscribeResult.Failure -> transient.update {
                    it.copy(addChannelBusy = false, addChannelError = result.failure.userMessage())
                }
            }
        }
    }

    /** Swipe left on a card. Reversible from settings, and undoable for the rest of the session. */
    fun onHideVideo(videoId: String) {
        viewModelScope.launch { feedRepository.hideVideo(videoId) }
    }

    fun onUndoHide(videoId: String) {
        viewModelScope.launch { feedRepository.unhideVideo(videoId) }
    }

    /** "Show" on the hidden-results notice: this session only, no setting is touched. */
    fun onRevealHidden() {
        hiddenRevealed.value = true
    }

    private fun FeedContent.toUiState(
        chips: Set<FeedChip>,
        revealed: Boolean,
        ui: Transient,
    ): FeedUiState {
        val now = System.currentTimeMillis()
        // `this.` is explicit on the receiver's properties: the FeedUiState parameters share their
        // names, and a silent self-assignment here would read as a bug.
        return FeedUiState(
            isLoading = false,
            items = this.items.map { it.toCardData(now, chips) },
            chips = FeedChip.entries.toList(),
            activeChips = chips,
            hiddenCount = this.hiddenCount,
            hiddenRevealed = revealed,
            isEmpty = this.isEmpty,
            emptiedByFilters = this.emptiedByFilters,
            isRefreshing = ui.isRefreshing,
            staleNotice = ui.staleNotice,
            addChannelOpen = ui.addChannelOpen,
            addChannelBusy = ui.addChannelBusy,
            addChannelError = ui.addChannelError,
        )
    }

    private fun FeedItem.toCardData(now: Long, chips: Set<FeedChip>): VideoCardData = VideoCardData(
        videoId = videoId,
        title = title,
        thumbnailUrl = thumbnailUrl,
        durationSec = durationSec,
        metaLine = KiteFormat.metaLine(
            channelName,
            KiteFormat.views(viewCount),
            KiteFormat.relativeDate(publishedAt, now),
        ),
        channelName = channelName,
        channelAvatarUrl = channelAvatarUrl,
        progress = progress,
        // Fully watched items dim to 60% unless the Unwatched chip excluded them upstream.
        dimmed = completed && FeedChip.Unwatched !in chips,
        isShort = isShort,
    )

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

/**
 * Turns a classified failure into something worth putting on screen.
 *
 * The distinction that matters is between "try again later" and "this will never work until
 * something changes" — a retry button on a quota error is a lie.
 */
private fun ApiFailure.userMessage(): String = when (this) {
    is ApiFailure.QuotaExceeded -> "Daily API quota used up. It resets at midnight Pacific."
    is ApiFailure.MissingKey ->
        "No YouTube API key in this build. Add YOUTUBE_API_KEY to secrets.properties and rebuild."
    is ApiFailure.Unauthenticated -> message
    is ApiFailure.Forbidden -> "The API key was rejected. Check that the Data API v3 is enabled."
    is ApiFailure.Unreachable -> message
    is ApiFailure.Unknown -> message
}
