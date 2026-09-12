package dev.local.ytclient.core.data.model

import dev.local.ytclient.core.network.api.ApiFailure

/**
 * What a sync did.
 *
 * A failure is a value, not an exception, because the caller's job on failure is to keep showing
 * cached data with a quiet stale indicator — `ARCHITECTURE.md`'s rule. Throwing would push every
 * call site into a try/catch that mostly does the same thing badly.
 */
sealed interface SyncResult {

    /** @param newItems videos written that were not already cached */
    data class Success(val newItems: Int, val channelsSynced: Int) : SyncResult

    data class Failure(val failure: ApiFailure) : SyncResult

    val isSuccess: Boolean get() = this is Success

    companion object {
        val Empty: SyncResult = Success(newItems = 0, channelsSynced = 0)
    }
}
