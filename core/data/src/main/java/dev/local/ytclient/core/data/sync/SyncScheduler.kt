package dev.local.ytclient.core.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * When sync happens.
 *
 * Cadence from `ARCHITECTURE.md`: subscription sync every 6 hours and on app open, feed refresh on
 * app open and on pull-to-refresh. Both require network — a queued sync that runs offline would fail
 * immediately and burn its retry budget for nothing.
 */
@Singleton
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val workManager: WorkManager get() = WorkManager.getInstance(context)

    private val network: Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /** Called from the activity on start. Safe to call repeatedly; `KEEP` makes it a no-op. */
    fun onAppOpen() {
        workManager.enqueueUniquePeriodicWork(
            SUBSCRIPTION_SYNC,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<SubscriptionSyncWorker>(
                SUBSCRIPTION_PERIOD_HOURS,
                TimeUnit.HOURS,
            ).setConstraints(network).build(),
        )
        refreshNow()
    }

    /** App open and pull-to-refresh both land here. Replaces a run already in flight. */
    fun refreshNow() {
        workManager.enqueueUniqueWork(
            FEED_REFRESH,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<FeedRefreshWorker>()
                .setConstraints(network)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_SECONDS, TimeUnit.SECONDS)
                .build(),
        )
    }

    private companion object {
        const val SUBSCRIPTION_SYNC = "subscription-sync"
        const val FEED_REFRESH = "feed-refresh"
        const val SUBSCRIPTION_PERIOD_HOURS = 6L
        const val BACKOFF_SECONDS = 30L
    }
}
