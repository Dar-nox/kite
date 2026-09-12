package dev.local.ytclient.feature.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.local.ytclient.core.data.model.FeedChip
import dev.local.ytclient.core.designsystem.ui.component.AppChip
import dev.local.ytclient.core.designsystem.ui.component.HiddenResultsNotice
import dev.local.ytclient.core.designsystem.ui.component.VideoCardLarge
import dev.local.ytclient.core.designsystem.ui.preview.KitePreviewEdgeToEdge
import dev.local.ytclient.core.designsystem.ui.theme.AppTheme
import dev.local.ytclient.core.designsystem.ui.theme.KiteRadius
import dev.local.ytclient.core.designsystem.ui.theme.KiteSpacing

/**
 * The feed.
 *
 * Header, chip row, then the list. Only the Large layout is rendered so far: `ROADMAP.md` is
 * explicit that the other four wait until one layout is correct, because five variants of a card
 * that isn't right yet is five times the rework.
 *
 * The empty state has a subscribe action on it. With no OAuth in the build, adding a channel is the
 * only way the feed ever fills, so an empty feed that offers nothing to do would be a dead end.
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
        onRefresh = viewModel::onRefresh,
        onDismissStaleNotice = viewModel::onDismissStaleNotice,
        onOpenAddChannel = viewModel::onOpenAddChannel,
        onCloseAddChannel = viewModel::onCloseAddChannel,
        onAddChannel = viewModel::onAddChannel,
        modifier = modifier,
    )
}

@Composable
fun FeedScreen(
    state: FeedUiState,
    onChipToggled: (FeedChip) -> Unit,
    onVideoClick: (String) -> Unit,
    onRevealHidden: () -> Unit,
    onRefresh: () -> Unit,
    onDismissStaleNotice: () -> Unit,
    onOpenAddChannel: () -> Unit,
    onCloseAddChannel: () -> Unit,
    onAddChannel: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            FeedHeader()

            FeedChipRow(
                chips = state.chips,
                activeChips = state.activeChips,
                onChipToggled = onChipToggled,
            )

            if (state.staleNotice != null) {
                StaleNotice(
                    message = state.staleNotice,
                    onDismiss = onDismissStaleNotice,
                    modifier = Modifier.padding(
                        start = KiteSpacing.screenMargin,
                        end = KiteSpacing.screenMargin,
                        bottom = KiteSpacing.space8,
                    ),
                )
            }

            if (state.isEmpty) {
                FeedEmptyState(onAddChannel = onOpenAddChannel)
            } else {
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = KiteSpacing.screenMargin,
                            end = KiteSpacing.screenMargin,
                            bottom = KiteSpacing.space24,
                        ),
                        verticalArrangement = Arrangement.spacedBy(KiteSpacing.feedItemGapLarge),
                    ) {
                        // The notice sits at the top of the list, in flow. A filtered-to-empty list
                        // shows it as its only row, which is the case the spec says must never look
                        // like a bug.
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

        if (state.addChannelOpen) {
            AddChannelDialog(
                busy = state.addChannelBusy,
                error = state.addChannelError,
                onDismiss = onCloseAddChannel,
                onConfirm = onAddChannel,
            )
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
 * A refresh failed while cached rows are on screen.
 *
 * Quiet by design: the list is still usable, so this explains itself and gets out of the way rather
 * than blocking the content it is annotating.
 */
@Composable
private fun StaleNotice(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(KiteRadius.banner))
            .background(colors.surfaceRaised)
            .padding(KiteSpacing.insideSurface),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = message,
            style = AppTheme.type.label,
            color = colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(KiteSpacing.space8))
        Text(
            text = "Dismiss",
            style = AppTheme.type.label,
            color = colors.infoBlueText,
            modifier = Modifier.clickable(onClickLabel = "Dismiss", onClick = onDismiss),
        )
    }
}

/**
 * Shown only when there is genuinely nothing cached.
 *
 * Deliberately a different message from a filtered-to-empty list: before the first subscription
 * there is nothing to filter, and "no results" here would send the user hunting for a setting that
 * isn't the problem.
 */
@Composable
private fun FeedEmptyState(onAddChannel: () -> Unit, modifier: Modifier = Modifier) {
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
            Spacer(Modifier.height(KiteSpacing.space16))
            Text(
                text = "Add a channel",
                style = AppTheme.type.label,
                color = colors.infoBlueText,
                modifier = Modifier.clickable(onClickLabel = "Add a channel", onClick = onAddChannel),
            )
        }
    }
}

/**
 * Subscribe by URL, handle, or id.
 *
 * Accepts the forms people actually paste — full watch/channel URLs with tracking parameters, bare
 * `@handles`, bare `UC…` ids — because the parser handles all of them and rejecting a valid paste is
 * a support request.
 */
@Composable
private fun AddChannelDialog(
    busy: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val colors = AppTheme.colors
    var input by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        containerColor = colors.surfaceRaised,
        title = {
            Text(text = "Add a channel", style = AppTheme.type.sectionTitle, color = colors.textPrimary)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    singleLine = true,
                    enabled = !busy,
                    label = { Text("Channel link, @handle, or id") },
                )
                if (error != null) {
                    Spacer(Modifier.height(KiteSpacing.space8))
                    Text(
                        text = error,
                        style = AppTheme.type.micro,
                        color = colors.dangerText,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(input) }, enabled = !busy && input.isNotBlank()) {
                Text(text = if (busy) "Adding…" else "Subscribe", color = colors.infoBlueText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !busy) {
                Text(text = "Cancel", color = colors.textSecondary)
            }
        },
    )
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
            onRefresh = {},
            onDismissStaleNotice = {},
            onOpenAddChannel = {},
            onCloseAddChannel = {},
            onAddChannel = {},
        )
    }
}
