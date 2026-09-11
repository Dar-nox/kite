package dev.local.ytclient.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.local.ytclient.core.designsystem.ui.theme.AppTheme
import dev.local.ytclient.feature.feed.FeedRoute

/** Route for the watch screen. Kept here so callers build it one way. */
fun watchRoute(videoId: String): String = "watch/$videoId"

private const val WATCH_ROUTE_PATTERN = "watch/{videoId}"

/**
 * App shell: the tab bar and the navigation graph.
 *
 * Tab navigation uses `saveState`/`restoreState`, which is what makes returning to the feed put you
 * back at the scroll position and the filter chips you left, rather than at the top. The bar itself
 * is rendered from [MainUiState.tabs], so removing a tab is a data change, not a layout change.
 */
@Composable
fun KiteApp(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
    navController: NavHostController = rememberNavController(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val selectedTab = state.tabs.firstOrNull { it.route == currentRoute }

    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.background,
        bottomBar = {
            // Hidden on detail screens: the bar belongs to the top level of each tab.
            if (selectedTab != null) {
                KiteTabBar(
                    tabs = state.tabs,
                    selected = selectedTab,
                    onSelect = { navController.navigateToTab(it) },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = KiteTab.Home.route,
            modifier = modifier.padding(innerPadding),
        ) {
            composable(KiteTab.Home.route) {
                FeedRoute(onVideoClick = { videoId -> navController.navigate(watchRoute(videoId)) })
            }

            composable(KiteTab.Shorts.route) {
                PendingScreen(
                    title = "Shorts",
                    detail = "The Shorts grid lands with filtering, in phase 4.",
                )
            }

            composable(KiteTab.Library.route) {
                PendingScreen(
                    title = "Library",
                    detail = "Queue, favorites, and archive land with saving, in phase 3.",
                )
            }

            composable(KiteTab.Playlists.route) {
                PendingScreen(
                    title = "Playlists",
                    detail = "Folders and progressive loading land in phase 5.",
                )
            }

            composable(KiteTab.Settings.route) {
                PendingScreen(
                    title = "Settings",
                    detail = "The settings screens land in phase 6.",
                )
            }

            composable(WATCH_ROUTE_PATTERN) {
                PendingScreen(
                    title = "Playback",
                    detail = "The watch screen lands in phase 2.",
                )
            }
        }
    }
}

/**
 * Switches tab without stacking destinations.
 *
 * Popping to the start destination is what keeps a deep back stack from dumping the user to Home
 * from three levels deep: each tab's own back stack is saved and restored instead.
 */
private fun NavHostController.navigateToTab(tab: KiteTab) {
    navigate(tab.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
