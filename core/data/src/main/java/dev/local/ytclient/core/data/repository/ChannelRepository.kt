package dev.local.ytclient.core.data.repository

import dev.local.ytclient.core.data.model.SubscribeResult
import dev.local.ytclient.core.database.model.ChannelEntity
import dev.local.ytclient.core.network.api.ApiFailure
import kotlinx.coroutines.flow.Flow

/**
 * Subscriptions and blocks.
 *
 * With OAuth unconfigured this is the only way to build a subscription list, and it is a real path
 * rather than a fallback: paste a channel URL, get its uploads in the feed. See
 * `dev.local.ytclient.core.network.account.AccountAuthenticator`.
 */
interface ChannelRepository {

    fun observeSubscriptions(): Flow<List<ChannelEntity>>

    fun observeBlocked(): Flow<List<ChannelEntity>>

    fun observeBlockedIds(): Flow<List<String>>

    fun observeChannel(channelId: String): Flow<ChannelEntity?>

    /**
     * Subscribes to whatever [input] is: a full URL, a bare `@handle`, or a `UC…` id.
     *
     * Never throws. An unparseable string and a handle that resolves to nothing are different
     * outcomes and the UI says something different for each.
     */
    suspend fun subscribe(input: String): SubscribeResult

    suspend fun unsubscribe(channelId: String)

    /**
     * Blocking is complete: the channel disappears from home, subscriptions, search results, up
     * next, playlist views, and comments. The row and its videos stay, so unblocking restores
     * everything with no re-sync.
     */
    suspend fun setBlocked(channelId: String, blocked: Boolean)

    suspend fun setDefaultSpeed(channelId: String, speed: Float?)

    suspend fun setAutoSave(channelId: String, collectionName: String?, tagIds: String?)
}

/** Outcome of a subscribe attempt. */
sealed interface SubscribeResult {
    data class Success(val channel: ChannelEntity) : SubscribeResult
    data class NotFound(val input: String) : SubscribeResult
    data class InvalidInput(val input: String) : SubscribeResult
    data class Failure(val failure: ApiFailure) : SubscribeResult
}
