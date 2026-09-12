package dev.local.ytclient.core.network.di

import dev.local.ytclient.core.network.BuildConfig
import dev.local.ytclient.core.network.account.AccountAuthenticator
import dev.local.ytclient.core.network.account.KeyOnlyAccountAuthenticator
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import dev.local.ytclient.core.network.api.YouTubeApi

/**
 * Network wiring.
 *
 * The API key lives in an untracked `secrets.properties` at the repo root and reaches the app as a
 * BuildConfig field, so it is never in source control and never in a committed file. A build without
 * one still runs — every call fails with [dev.local.ytclient.core.network.api.ApiFailure.MissingKey]
 * and the app keeps serving cached data, which is the local-first rule holding under a
 * misconfiguration rather than only under a network outage.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://www.googleapis.com/youtube/v3/"
    private val JSON_MEDIA_TYPE = "application/json".toMediaType()

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        // The API omits fields rather than nulling them, and adds new ones without notice. Either
        // strictness setting here would turn an unrelated API change into a decoding crash.
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(ApiKeyInterceptor())
        .build()

    @Provides
    @Singleton
    fun provideYouTubeApi(client: OkHttpClient, json: Json): YouTubeApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory(JSON_MEDIA_TYPE))
        .build()
        .create(YouTubeApi::class.java)
}

/** Appends `key=` to every request when a key is configured. */
private class ApiKeyInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val key = BuildConfig.YOUTUBE_API_KEY
        if (key.isBlank()) return chain.proceed(original)

        val url = original.url.newBuilder()
            .addQueryParameter("key", key)
            .build()
        return chain.proceed(original.newBuilder().url(url).build())
    }
}

/** Binds the account seam. Swap this binding to add Google Sign-In. */
@Module
@InstallIn(SingletonComponent::class)
abstract class AccountModule {

    @Binds
    @Singleton
    abstract fun bindAccountAuthenticator(
        impl: KeyOnlyAccountAuthenticator,
    ): AccountAuthenticator
}
