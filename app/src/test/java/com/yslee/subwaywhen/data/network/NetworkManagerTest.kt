package com.yslee.subwaywhen.data.network

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class TestResponse(val name: String)

class NetworkManagerTest : FunSpec({

    val testJson = Json { ignoreUnknownKeys = true }

    fun buildManager(engine: MockEngine): NetworkManagerImpl =
        NetworkManagerImpl(HttpClient(engine) { followRedirects = false }, testJson)

    test("200 응답 + 유효한 JSON → NetworkResult.Success 반환") {
        val engine = MockEngine { _ ->
            respond(
                content = """{"name":"test"}""",
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "application/json")
            )
        }

        val result = buildManager(engine).requestData<TestResponse>("https://example.com")

        result.shouldBeInstanceOf<NetworkResult.Success<TestResponse>>()
        (result as NetworkResult.Success).data shouldBe TestResponse("test")
    }

    test("301 응답 → NetworkResult.Failure(NetworkError.BadServerResponse)") {
        val engine = MockEngine { _ ->
            respond(content = "", status = HttpStatusCode.MovedPermanently)
        }

        val result = buildManager(engine).requestData<TestResponse>("https://example.com")

        result shouldBe NetworkResult.Failure(NetworkError.BadServerResponse)
    }

    test("500 응답 → NetworkResult.Failure(NetworkError.BadServerResponse)") {
        val engine = MockEngine { _ ->
            respondError(HttpStatusCode.InternalServerError)
        }

        val result = buildManager(engine).requestData<TestResponse>("https://example.com")

        result shouldBe NetworkResult.Failure(NetworkError.BadServerResponse)
    }

    test("404 응답 → NetworkResult.Failure(NetworkError.BadUrl)") {
        val engine = MockEngine { _ ->
            respondError(HttpStatusCode.NotFound)
        }

        val result = buildManager(engine).requestData<TestResponse>("https://example.com")

        result shouldBe NetworkResult.Failure(NetworkError.BadUrl)
    }

    test("200 응답 + 잘못된 JSON → NetworkResult.Failure(NetworkError.CannotParseResponse)") {
        val engine = MockEngine { _ ->
            respond(
                content = "not-json",
                status = HttpStatusCode.OK,
                headers = headersOf("Content-Type", "application/json")
            )
        }

        val result = buildManager(engine).requestData<TestResponse>("https://example.com")

        result shouldBe NetworkResult.Failure(NetworkError.CannotParseResponse)
    }

    test("네트워크 예외 발생 → NetworkResult.Failure(NetworkError.NotConnectedToInternet)") {
        val engine = MockEngine { _ ->
            throw java.io.IOException("Network unavailable")
        }

        val result = buildManager(engine).requestData<TestResponse>("https://example.com")

        result shouldBe NetworkResult.Failure(NetworkError.NotConnectedToInternet)
    }
})
