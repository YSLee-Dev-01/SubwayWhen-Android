package com.yslee.subwaywhen.data.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.takeFrom
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject

class NetworkManagerImpl @Inject constructor(
    private val client: HttpClient,
    private val json: Json
) : NetworkManager {

    override suspend fun <T> requestData(urlString: String, serializer: KSerializer<T>): NetworkResult<T> =
        requestData(urlString, emptyMap(), emptyMap(), serializer)

    override suspend fun <T> requestData(
        urlString: String,
        headers: Map<String, String>,
        query: Map<String, String>,
        serializer: KSerializer<T>
    ): NetworkResult<T> = try {
        val response = client.get {
            url {
                takeFrom(urlString)
                query.forEach { (key, value) -> parameters.append(key, value) }
            }
            headers.forEach { (key, value) -> header(key, value) }
        }
        val statusCode = response.status.value
        when {
            statusCode in 200..299 -> try {
                NetworkResult.Success(json.decodeFromString(serializer, response.bodyAsText()))
            } catch (e: SerializationException) {
                NetworkResult.Failure(NetworkError.CannotParseResponse)
            }
            statusCode in 400..499 -> NetworkResult.Failure(NetworkError.BadUrl)
            else -> NetworkResult.Failure(NetworkError.BadServerResponse)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        NetworkResult.Failure(NetworkError.NotConnectedToInternet)
    } catch (e: Exception) {
        NetworkResult.Failure(NetworkError.NotConnectedToInternet)
    }
}
