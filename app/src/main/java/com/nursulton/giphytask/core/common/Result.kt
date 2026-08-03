package com.nursulton.giphytask.core.common

sealed interface Result<out T> {
    data class Success<out T>(val data: T) : Result<T>
    data class Error(val error: AppError) : Result<Nothing>
    data object Loading : Result<Nothing>
}

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> Result.Error(error)
        is Result.Loading -> Result.Loading
    }
}
