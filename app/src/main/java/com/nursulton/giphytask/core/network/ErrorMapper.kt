package com.nursulton.giphytask.core.network

import com.nursulton.giphytask.core.common.AppError
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/** Normalises any [Throwable] from the data layer into a domain [AppError]. */
fun Throwable.toAppError(): AppError {
    return when (this) {
        is AppError -> this
        is UnknownHostException -> AppError.NoInternet
        is SocketTimeoutException -> AppError.Timeout
        is IOException -> AppError.NoInternet
        is HttpException -> toHttpAppError()
        else -> AppError.Unknown(message = message, cause = this)
    }
}

private fun HttpException.toHttpAppError(): AppError = when (val code = code()) {
    HTTP_UNAUTHORIZED, HTTP_FORBIDDEN -> AppError.Unauthorized
    HTTP_NOT_FOUND -> AppError.NotFound
    HTTP_TOO_MANY_REQUESTS -> AppError.RateLimited
    in HTTP_SERVER_ERROR_RANGE -> AppError.ServerError(code, message())
    else -> AppError.Unknown("HTTP Error $code: ${message()}")
}

private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_FORBIDDEN = 403
private const val HTTP_NOT_FOUND = 404
private const val HTTP_TOO_MANY_REQUESTS = 429
private val HTTP_SERVER_ERROR_RANGE = 500..599
