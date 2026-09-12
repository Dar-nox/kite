package dev.local.ytclient.core.data.repository

import dev.local.ytclient.core.data.mapper.toEntity
import dev.local.ytclient.core.data.util.ChannelRef
import dev.local.ytclient.core.data.util.ChannelUrlParser
import dev.local.ytclient.core.database.dao.ChannelDao
import dev.local.ytclient.core.database.model.ChannelEntity
import dev.local.ytclient.core.network.api.YouTubeApi
import dev.local.ytclient.core.network.api.toApiFailure
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class ChannelRepositoryImpl @Inject constructor(
    private val api: YouTubeApi,
    private val channelDao: ChannelDao,
) : ChannelRepository {

    override fun observeSubscriptions(): Flow<List<ChannelEntity>> = channelDao.observeSubscribed()

    override fun observeBlocked(): Flow<List<ChannelEntity>> = channelDao.observeBlocked()

    override fun observeBlockedIds(): Flow<List<String>> = channelDao.observeBlockedIds()

    override fun observeChannel(channelId: String): Flow<ChannelEntity?> =
        channelDao.observe(channelId)

    override suspend fun subscribe(input: String): SubscribeResult {
        val ref = ChannelUrlParser.parse(input)
            ?: return SubscribeResult.InvalidInput(input)

        val existing = ref.asExistingChannel()
        if (existing != null) {
            channelDao.setSubscribed(existing.channelId, true)
            return SubscribeResult.Success(channelDao.find(existing.channelId) ?: existing)
        }

        return try {
            val channel = ref.resolve()
            if (channel == null) {
                SubscribeResult.NotFound(input)
            } else {
                channelDao.syncMetadata(
                    channelId = channel.channelId,
                    title = channel.title,
                    avatarUrl = channel.avatarUrl,
                    uploadsPlaylistId = channel.uploadsPlaylistId,
                    subscriberCount = channel.subscriberCount,
                )
                channelDao.setSubscribed(channel.channelId, true)
                val saved = channelDao.find(channel.channelId)
                if (saved == null) SubscribeResult.NotFound(input) else SubscribeResult.Success(saved)
            }
        } catch (t: Throwable) {
            SubscribeResult.Failure(t.toApiFailure())
        }
    }

    /**
     * A channel already in the database can be re-subscribed for free — a block or an unsubscribe
     * leaves the row behind on purpose, so most re-subscribes never touch the network.
     */
    private suspend fun ChannelRef.asExistingChannel(): ChannelEntity? = when (this) {
        is ChannelRef.Id -> channelDao.find(value)
        is ChannelRef.Handle, is ChannelRef.Username, is ChannelRef.Custom -> null
    }

    private suspend fun ChannelRef.resolve(): ChannelEntity? {
        val response = when (this) {
            is ChannelRef.Id -> api.channels(id = value)
            is ChannelRef.Handle -> api.channels(forHandle = "@$value")
            is ChannelRef.Username -> api.channels(forUsername = value)
            // The one case that needs `search`: a legacy /c/ name has no `list` parameter.
            is ChannelRef.Custom -> {
                val found = api.search(q = value, type = "channel", maxResults = 1)
                    .items.firstOrNull()?.id?.channelId
                return if (found == null) null else api.channels(id = found).items
                    .firstOrNull()?.toEntity()
            }
        }
        return response.items.firstOrNull()?.toEntity()
    }

    override suspend fun unsubscribe(channelId: String) =
        channelDao.setSubscribed(channelId, false)

    override suspend fun setBlocked(channelId: String, blocked: Boolean) =
        channelDao.setBlocked(channelId, blocked)

    override suspend fun setDefaultSpeed(channelId: String, speed: Float?) =
        channelDao.setDefaultSpeed(channelId, speed)

    override suspend fun setAutoSave(channelId: String, collectionName: String?, tagIds: String?) =
        channelDao.setAutoSave(channelId, collectionName, tagIds)
}
