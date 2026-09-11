package dev.local.ytclient.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.local.ytclient.core.datastore.SettingsRepository
import dev.local.ytclient.core.datastore.SettingsRepositoryImpl
import javax.inject.Singleton

/** Settings DataStore wiring. */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    private const val FILE_NAME = "kite-settings.preferences_pb"

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile(FILE_NAME) },
        )

    @Provides
    @Singleton
    fun provideSettingsRepository(dataStore: DataStore<Preferences>): SettingsRepository =
        SettingsRepositoryImpl(dataStore)
}

/**
 * Resolves the preferences file inside the app's data dir.
 *
 * Written out rather than using the `preferencesDataStore` property delegate so the file name has
 * one owner and the store can be constructed by Hilt with an explicit scope.
 */
private fun Context.preferencesDataStoreFile(name: String) =
    java.io.File(filesDir, "datastore/$name").apply { parentFile?.mkdirs() }
