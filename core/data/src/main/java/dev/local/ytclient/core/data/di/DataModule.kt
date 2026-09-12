package dev.local.ytclient.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.local.ytclient.core.data.repository.ChannelRepository
import dev.local.ytclient.core.data.repository.ChannelRepositoryImpl
import dev.local.ytclient.core.data.repository.FeedRepository
import dev.local.ytclient.core.data.repository.FeedRepositoryImpl
import dev.local.ytclient.core.data.repository.SyncRepository
import dev.local.ytclient.core.data.repository.SyncRepositoryImpl
import javax.inject.Singleton

/** Repository bindings. Features depend on the interfaces, never the implementations. */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindFeedRepository(impl: FeedRepositoryImpl): FeedRepository

    @Binds
    @Singleton
    abstract fun bindChannelRepository(impl: ChannelRepositoryImpl): ChannelRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository
}
