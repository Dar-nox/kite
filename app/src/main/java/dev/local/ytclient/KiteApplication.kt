package dev.local.ytclient

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point.
 *
 * Deliberately thin. No analytics SDK is initialised here and none will be: `CLAUDE.md` bans
 * telemetry outright, so there is no crash reporter, no session tracking, and no network call that
 * isn't fetching content the user asked for.
 *
 * WorkManager is configured on demand by the sync workers rather than eagerly, so a cold start does
 * not pay for a scheduler it may not need.
 */
@HiltAndroidApp
class KiteApplication : Application()
