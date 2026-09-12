package dev.local.ytclient

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import dev.local.ytclient.core.data.sync.SyncScheduler
import dev.local.ytclient.core.designsystem.ui.theme.KiteTheme
import dev.local.ytclient.ui.KiteApp
import javax.inject.Inject

/**
 * The single activity.
 *
 * Everything else is a Compose destination inside [KiteApp]. Edge to edge is enabled here so the
 * dark background runs under the system bars rather than being letterboxed by them.
 *
 * `configChanges` in the manifest keeps rotation from recreating the activity, which is what lets
 * playback survive a turn to landscape without a re-resolve of the stream.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var syncScheduler: SyncScheduler

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Sync on app open, per ARCHITECTURE.md. Both enqueue calls are idempotent, so a recreation
        // does not stack work, and the feed renders from cache immediately either way — the worker
        // writes to Room and the Flow carries it in.
        syncScheduler.onAppOpen()

        setContent {
            KiteTheme {
                KiteApp()
            }
        }
    }
}
