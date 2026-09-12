package dev.local.ytclient.core.network.account

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** Outcome of a sign-in attempt. */
sealed interface AuthResult {
    data object Success : AuthResult

    /** Sign-in exists but failed; [message] is safe to show. */
    data class Failed(val message: String) : AuthResult

    /**
     * No sign-in path is configured in this build. Distinct from [Failed] because the UI should
     * offer the manual alternative rather than a retry button.
     */
    data class Unavailable(val message: String) : AuthResult
}

/**
 * The seam where a Google account plugs in.
 *
 * Everything that needs "the user's own subscriptions and playlists" goes through this, and
 * nothing else in the app knows which implementation is bound. Swapping in real Google Sign-In is
 * therefore a DI change plus an OAuth client id — no repository changes.
 */
interface AccountAuthenticator {

    /** Access token for authenticated endpoints, or null when signed out. */
    val accessToken: Flow<String?>

    val isSignedIn: Flow<Boolean>

    /**
     * Starts sign-in. Implementations that need an Activity get it from the call site through a
     * separate launcher; this returns the outcome once the flow settles.
     */
    suspend fun signIn(): AuthResult

    suspend fun signOut()
}

/**
 * The default binding: no Google account, API key only.
 *
 * This is a real limitation, not a stub pretending otherwise. `subscriptions?mine=true` needs OAuth,
 * so with this bound the subscription list is built by the user adding channels by URL or id — see
 * `ChannelRepository.subscribe`. Everything else in the app (uploads feeds, playlists, videos,
 * comments, search) works on the API key alone.
 *
 * It returns [AuthResult.Unavailable] rather than [AuthResult.Failed] so the UI can offer "add a
 * channel" instead of a retry that will fail again.
 */
@Singleton
class KeyOnlyAccountAuthenticator @Inject constructor() : AccountAuthenticator {

    override val accessToken: Flow<String?> = flowOf(null)

    override val isSignedIn: Flow<Boolean> = flowOf(false)

    override suspend fun signIn(): AuthResult = AuthResult.Unavailable(
        message = "Google sign-in isn't configured in this build. Add channels by URL instead.",
    )

    override suspend fun signOut() = Unit
}
