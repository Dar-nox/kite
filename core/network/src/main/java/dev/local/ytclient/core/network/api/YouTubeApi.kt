package dev.local.ytclient.core.network.api

import dev.local.ytclient.core.network.dto.ApiListResponse
import dev.local.ytclient.core.network.dto.ChannelDto
import dev.local.ytclient.core.network.dto.CommentThreadDto
import dev.local.ytclient.core.network.dto.PlaylistDto
import dev.local.ytclient.core.network.dto.PlaylistItemDto
import dev.local.ytclient.core.network.dto.SearchResultDto
import dev.local.ytclient.core.network.dto.VideoDto
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * YouTube Data API v3.
 *
 * Quota, from `ARCHITECTURE.md`: 10,000 units a day, `list` costs 1 and `search` costs 100. So
 * `search` appears exactly once here and everything else is a cheap `list` — the feed is built from
 * uploads playlists, not from search, and a 2,000-item playlist costs 40 units at 50 per page.
 *
 * `maxResults` defaults to 50, the maximum every one of these endpoints accepts, because a smaller
 * page is pure quota waste for the same content.
 */
interface YouTubeApi {

    /**
     * Resolve channel ids, handles, or usernames.
     *
     * Exactly one of the id selectors should be passed. This is the only supported way to turn a
     * URL the user pasted into a channel row — there is no scraping path in this app.
     */
    @GET("channels")
    suspend fun channels(
        @Query("part") part: String = "snippet,contentDetails,statistics",
        @Query("id") id: String? = null,
        @Query("forHandle") forHandle: String? = null,
        @Query("forUsername") forUsername: String? = null,
        @Query("maxResults") maxResults: Int = 50,
    ): ApiListResponse<ChannelDto>

    /** The user's subscriptions. Requires OAuth; returns 401 with an API key alone. */
    @GET("subscriptions")
    suspend fun mySubscriptions(
        @Query("part") part: String = "snippet",
        @Query("mine") mine: Boolean = true,
        @Query("maxResults") maxResults: Int = 50,
        @Query("pageToken") pageToken: String? = null,
    ): ApiListResponse<ChannelDto>

    @GET("videos")
    suspend fun videos(
        @Query("part") part: String = "snippet,contentDetails,statistics",
        @Query("id") ids: String,
        @Query("maxResults") maxResults: Int = 50,
    ): ApiListResponse<VideoDto>

    @GET("playlists")
    suspend fun playlists(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("id") ids: String,
        @Query("maxResults") maxResults: Int = 50,
    ): ApiListResponse<PlaylistDto>

    /**
     * Page through a playlist. Called repeatedly with [pageToken] until `nextPageToken` is null;
     * each page costs 1 unit, so the progressive-loading banner in `FEATURES.md` is also a quota
     * readout in disguise.
     */
    @GET("playlistItems")
    suspend fun playlistItems(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("playlistId") playlistId: String,
        @Query("maxResults") maxResults: Int = 50,
        @Query("pageToken") pageToken: String? = null,
    ): ApiListResponse<PlaylistItemDto>

    /**
     * 100 units. Used only where no cheaper endpoint works — library-wide search and resolving a
     * pasted video URL that has no id in it.
     */
    @GET("search")
    suspend fun search(
        @Query("part") part: String = "snippet",
        @Query("q") query: String? = null,
        @Query("type") type: String = "video",
        @Query("channelId") channelId: String? = null,
        @Query("maxResults") maxResults: Int = 50,
        @Query("pageToken") pageToken: String? = null,
    ): ApiListResponse<SearchResultDto>

    @GET("commentThreads")
    suspend fun commentThreads(
        @Query("part") part: String = "snippet,replies",
        @Query("videoId") videoId: String,
        @Query("maxResults") maxResults: Int = 50,
        @Query("pageToken") pageToken: String? = null,
    ): ApiListResponse<CommentThreadDto>
}
