package dev.local.ytclient.core.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.local.ytclient.core.data.model.SyncResult
import dev.local.ytclient.core.data.repository.SyncRepository
import dev.local.ytclient.core.network.api.ApiFailure

/**
 * Background work.
 *
 * The retry policy is the interesting part. `Unreachable` retries — a train tunnel is not a reason
 * to stop syncing. Everything else returns success and stops, because retrying cannot help: quota
 * comes back at midnight, a missing key needs a rebuild, and an unauthenticated call will fail the
 * same way forever. A worker that retries those burns battery and quota to produce the same error.
 */
private fun SyncResult.toWorkerResult(): androidx.work.ListenableWorker.Result = when (this) {
    is SyncResult.Success -> androidx.work.ListenableWorker.Result.success()
    is SyncResult.Failure ->
        if (failure is ApiFailure.Unreachable) {
            androidx.work.ListenableWorker.Result.retry()
        } else {
            androidx.work.ListenableWorker.Result.failure()
        }
}

/**
 * Pulls recent uploads per subscribed channel.
 *
 * Scheduled on app open and on pull-to-refresh, per `ARCHITECTURE.md`. One page per channel, which
 * is 2 quota units each — deep backfill is [SubscriptionSyncWorker]'s job.
 */
@HiltWorker
class FeedRefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncRepository: SyncRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result =
        syncRepository.refreshFeed(maxPagesPerChannel = REFRESH_PAGES).toWorkerResult()

    private companion object {
        const val REFRESH_PAGES = 1
    }
}

/**
 * Refreshes the subscribed channel list every 6 hours.
 *
 * With the key-only account binding this fails as `Unauthenticated` every run and stops rather than
 * retrying, which is correct behaviour for a build that has no sign-in: it should not sit in a retry
 * loop for the life of the app.
 */
@HiltWorker
class SubscriptionSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncRepository: SyncRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result =
        syncRepository.syncSubscriptions().toWorkerResult()
}
