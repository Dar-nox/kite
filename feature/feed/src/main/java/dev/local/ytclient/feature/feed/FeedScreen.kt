package dev.local.ytclient.feature.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.local.ytclient.core.data.model.FeedChip
import dev.local.ytclient.core.designsystem.ui.component.AppChip
import dev.local.ytclient.core.designsystem.ui.component.HiddenResultsNotice
import dev.local.ytclient.core.designsystem.ui.component.VideoCardLarge
import dev.local.ytclient.core.designsystem.ui.preview.KitePreviewEdgeToEdge
import dev.local.ytclient.core.designsystem.ui.theme.AppTheme
import dev.local.ytclient.core.designsystem.ui.theme.KiteSpacing

/**
 * The feed.
 *
 * Header, then a horizontally scrollable chip row, then the list. Only the Large layout is rendered
 * so far: `ROADMAP.md` is explicit that the other four wait until one layout is correct, because
 * five variants of a card that isn't right yet is five times the rework.
 *
 * Not here yet, and deliberately so rather than stubbed: the header search box (lands with search),
 * pull to refresh (lands with the sync workers), and the swipe gestures plus the long-press context
 * sheet (land with saving). Each needs the feature behind it to be real before the affordance is.
 */
@Composable
fun FeedRoute(
    onVideoClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    FeedScreen(
        state = state,
        onChipToggled = viewModel::onChipToggled,
        onVideoClick = onVideoClick,
        onRevealHidden = viewModel::onRevealHidden,
        modifier = modifier,
    )
}

@Composable
fun FeedScreen(
    state: FeedUiState,
    onChipToggled: (FeedChip) -> Unit,
    onVideoClick: (String) -> Unit,
    onRevealHidden: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        FeedHeader()

        FeedChipRow(
            chips = state.chips,
            activeChips = state.activeChips,
            onChipToggled = onChipToggled,
        )

        if (state.isEmpty) {
            FeedEmptyState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = KiteSpacing.screenMargin,
                    end = KiteSpacing.screenMargin,
                    bottom = KiteSpacing.space24,
                ),
                verticalArrangement = Arrangement.spacedBy(KiteSpacing.feedItemGapLarge),
            ) {
                // The notice sits at the top of the list, in flow. A filtered-to-empty list shows
                // it as its only row, which is the case the spec says must never look like a bug.
                if (state.hiddenCount > 0 && !state.hiddenRevealed) {
                    item(key = "hidden-notice", contentType = "notice") {
                        HiddenResultsNotice(
                            hiddenCount = state.hiddenCount,
                            onShow = onRevealHidden,
                        )
                    }
                }

                items(items = state.items, key = { it.videoId }, contentType = { "card" }) { card ->
                    VideoCardLarge(
                        data = card,
                        onClick = { onVideoClick(card.videoId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = KiteSpacing.screenMargin,
                end = KiteSpacing.screenMargin,
                top = KiteSpacing.space12,
                bottom = KiteSpacing.space4,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Home",
            style = AppTheme.type.screenTitle,
            color = AppTheme.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun FeedChipRow(
    chips: List<FeedChip>,
    activeChips: Set<FeedChip>,
    onChipToggled: (FeedChip) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = KiteSpacing.screenMargin),
        horizontalArrangement = Arrangement.spacedBy(KiteSpacing.space8),
    ) {
        items(items = chips, key = { it.name }) { chip ->
            AppChip(
                label = chip.label,
                selected = chip in activeChips,
                onClick = { onChipToggled(chip) },
            )
        }
    }
}

/**
 * Shown only when there is genuinely nothing cached.
 *
 * Deliberately a different message from a filtered-to-empty list: the feed is built from
 * subscriptions, so before the first sync there is nothing to filter, and "no results" here would
 * send the user hunting for a setting that isn't the problem.
 */
@Composable
private fun FeedEmptyState(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(KiteSpacing.screenMargin),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Nothing here yet",
                style = AppTheme.type.sectionTitle,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(KiteSpacing.space8))
            Text(
                text = "The feed is built from the uploads of channels you subscribe to.",
                style = AppTheme.type.body,
                color = colors.textSecondary,
            )
        }
    }
}

@Preview
@Composable
private fun FeedScreenPreview() {
    KitePreviewEdgeToEdge {
        FeedScreen(
            state = FeedUiState(isLoading = false, isEmpty = true),
            onChipToggled = {},
            onVideoClick = {},
            onRevealHidden = {},
        )
    }
}
