package dev.local.ytclient

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application entry point.
 *
 * Deliberately thin. No analytics SDK is initialised here and none will be: `CLAUDE.md` bans
 * telemetry outright, so there is no crash reporter, no session tracking, and no network call that
 * isn't fetching content the user asked for.
 *
 * WorkManager is configured here rather than by its default initialiser, because the sync workers
 * are `@HiltWorker`s and need the Hilt worker factory to have their repositories injected. The
 * matching `tools:node="remove"` is in the manifest — without it WorkManager initialises twice and
 * the injected factory is never used.
 */
@HiltAndroidApp
class KiteApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
