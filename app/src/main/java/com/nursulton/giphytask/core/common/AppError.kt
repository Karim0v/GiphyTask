package com.nursulton.giphytask.core.common

/**
 * Domain-level error taxonomy. Everything thrown by the data layer is normalised into one of
 * these via `Throwable.toAppError()` so the UI never has to reason about Retrofit/OkHttp types.
 */
sealed class AppError : Exception() {
    data object NoInternet : AppError()
    data object Timeout : AppError()
    data object Unauthorized : AppError()
    data object NotFound : AppError()
    data object RateLimited : AppError()
    data object EmptySearch : AppError()
    data class ServerError(val code: Int, override val message: String?) : AppError()
    data class Unknown(
        override val message: String? = null,
        override val cause: Throwable? = null
    ) : AppError()

    fun toUserFriendlyMessage(): String {
        return when (this) {
            is NoInternet -> "No internet connection available. Please check your network."
            is Timeout -> "Request timed out. Please try again."
            is Unauthorized -> "Invalid or missing API key. Please check your configuration."
            is NotFound -> "The requested GIF could not be found."
            is RateLimited -> "Too many requests to Giphy. Please wait a moment and try again."
            is EmptySearch -> "Please enter a search term to find GIFs."
            is ServerError -> "Server error ($code): ${message ?: "Please try again later."}"
            is Unknown -> message ?: "An unexpected error occurred. Please try again."
        }
    }
}
