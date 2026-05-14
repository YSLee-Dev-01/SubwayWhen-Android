package com.yslee.subwaywhen.data.network

import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

interface NetworkManager {
    suspend fun <T> requestData(url: String, serializer: KSerializer<T>): NetworkResult<T>
    suspend fun <T> requestData(
        url: String,
        headers: Map<String, String>,
        query: Map<String, String>,
        serializer: KSerializer<T>
    ): NetworkResult<T>
}

suspend inline fun <reified T> NetworkManager.requestData(url: String): NetworkResult<T> =
    requestData(url, serializer())

suspend inline fun <reified T> NetworkManager.requestData(
    url: String,
    headers: Map<String, String>,
    query: Map<String, String>
): NetworkResult<T> = requestData(url, headers, query, serializer())
