package com.yslee.subwaywhen.data.network

sealed interface NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>
    data class Failure(val error: NetworkError) : NetworkResult<Nothing>
}
