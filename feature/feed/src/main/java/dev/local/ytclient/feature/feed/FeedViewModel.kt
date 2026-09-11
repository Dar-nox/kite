package dev.local.ytclient.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.local.ytclient.core.data.model.FeedChip
import dev.local.ytclient.core.data.model.FeedContent
import dev.local.ytclient.core.data.model.FeedItem
import dev.local.ytclient.core.data.repository.FeedRepository
import dev.local.ytclient.core.datastore.SettingsRepository
import dev.local.ytclient.core.designsystem.ui.model.VideoCardData
import dev.local.ytclient.core.designsystem.util.KiteFormat
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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
)

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val feedRepository: FeedRepository,
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

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<FeedUiState> =
        combine(activeChips, hiddenRevealed) { chips, revealed -> chips to revealed }
            .flatMapLatest { (chips, revealed) ->
                feedRepository.observeFeed(activeChips = chips, revealHidden = revealed)
            }
            .map { content -> content.toUiState(activeChips.value, hiddenRevealed.value) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), FeedUiState())

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
    ): FeedUiState {
        val now = System.currentTimeMillis()
        // `this.` is explicit on the receiver's properties: the FeedUiState parameters share their
        // names, and a silent self-assignment here would read as a bug.
        return FeedUiState(
            isLoading = false,
            items = this.items.map { it.toCardData(now) },
            chips = FeedChip.entries.toList(),
            activeChips = chips,
            hiddenCount = this.hiddenCount,
            hiddenRevealed = revealed,
            isEmpty = this.isEmpty,
            emptiedByFilters = this.emptiedByFilters,
        )
    }

    private fun FeedItem.toCardData(now: Long): VideoCardData = VideoCardData(
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
        dimmed = completed && FeedChip.Unwatched !in activeChips.value,
        isShort = isShort,
    )

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
