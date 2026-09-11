package dev.local.ytclient.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material.icons.outlined.QueueMusic
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.ui.graphics.vector.ImageVector
import dev.local.ytclient.core.datastore.ShortsMode

/**
 * The bottom tab destinations.
 *
 * The bar renders from this list rather than from a fixed set of slots: in Shorts Off mode the
 * Shorts entry is removed and the remaining tabs reflow across the full width, with no gap where
 * Shorts used to be. That is a `FEATURES.md` requirement, and it only works if the bar is built
 * from data.
 */
enum class KiteTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
) {
    Home("home", "Home", Icons.Outlined.Home, Icons.Rounded.Home),
    Shorts("shorts", "Shorts", Icons.Outlined.PlayCircle, Icons.Rounded.PlayCircle),
    Library("library", "Library", Icons.Outlined.QueueMusic, Icons.Rounded.QueueMusic),
    Playlists("playlists", "Playlists", Icons.Outlined.VideoLibrary, Icons.Rounded.VideoLibrary),
    Settings("settings", "Settings", Icons.Outlined.Settings, Icons.Rounded.Settings),
    ;

    companion object {
        /** The tabs to render for a given Shorts mode. */
        fun visibleFor(shortsMode: ShortsMode): List<KiteTab> =
            entries.filter { tab -> tab != Shorts || shortsMode != ShortsMode.Off }
    }
}
