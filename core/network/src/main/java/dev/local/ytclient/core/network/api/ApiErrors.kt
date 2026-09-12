package dev.local.ytclient.core.network.api

import java.io.IOException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.HttpException

/**
 * Failures, classified.
 *
 * `ARCHITECTURE.md` requires failures to degrade rather than block, and "retry" is the wrong answer
 * for three of these five. Quota is spent until midnight, a missing key is a build configuration
 * problem, and an unauthenticated call will fail identically forever — so the UI has to be able to
 * tell them apart instead of showing one generic error.
 */
sealed interface ApiFailure {
    val message: String

    /** Daily quota spent. Nothing to do until it resets. */
    data class QuotaExceeded(override val message: String) : ApiFailure

    /** No API key in this build. */
    data class MissingKey(override val message: String) : ApiFailure

    /** The endpoint needs OAuth, or the token expired. */
    data class Unauthenticated(override val message: String) : ApiFailure

    /** The key was rejected or the API isn't enabled on the project. */
    data class Forbidden(override val message: String) : ApiFailure

    /** Network unreachable. Cached data stays on screen; a retry is legitimate. */
    data class Unreachable(override val message: String) : ApiFailure

    /** Anything else, including a 404 or a response we could not decode. */
    data class Unknown(override val message: String) : ApiFailure
}

/** The Data API's error envelope, decoded only to read the `reason`. */
@Serializable
private data class ApiErrorEnvelope(val error: ApiErrorBody? = null)

@Serializable
private data class ApiErrorBody(
    val message: String = "",
    val errors: List<ApiErrorDetail> = emptyList(),
)

@Serializable
private data class ApiErrorDetail(
    val reason: String = "",
    @SerialName("domain") val domain: String = "",
)

/** Classifies a thrown call into an [ApiFailure]. Never throws. */
fun Throwable.toApiFailure(): ApiFailure = when (this) {
    is IOException -> ApiFailure.Unreachable("No connection. Showing what's already cached.")
    is HttpException -> httpFailure(this)
    else -> ApiFailure.Unknown(message ?: "Something went wrong.")
}

private fun httpFailure(exception: HttpException): ApiFailure {
    val body = runCatching { exception.response()?.errorBody()?.string() }.getOrNull()
    val reason = body?.let { decodeReason(it) }
    val text = body?.let { decodeMessage(it) } ?: exception.message()

    return when (exception.code()) {
        400 ->
            if (reason == "keyInvalid" || text.contains("API key", ignoreCase = true)) {
                ApiFailure.MissingKey(text)
            } else {
                ApiFailure.Unknown(text)
            }
        401 -> ApiFailure.Unauthenticated(text)
        403 ->
            if (reason == "quotaExceeded" || reason == "dailyLimitExceeded") {
                ApiFailure.QuotaExceeded(text)
            } else {
                ApiFailure.Forbidden(text)
            }
        else -> ApiFailure.Unknown(text)
    }
}

private val json = kotlinx.serialization.json.Json {
    ignoreUnknownKeys = true
    isLenient = true
}

private fun decodeReason(body: String): String? = runCatching {
    json.decodeFromString(ApiErrorEnvelope.serializer(), body)
        .error?.errors?.firstOrNull()?.reason?.takeIf { it.isNotBlank() }
}.getOrNull()

private fun decodeMessage(body: String): String? = runCatching {
    json.decodeFromString(ApiErrorEnvelope.serializer(), body)
        .error?.message?.takeIf { it.isNotBlank() }
}.getOrNull()
