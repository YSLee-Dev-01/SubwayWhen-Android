package com.yslee.subwaywhen.di

import com.yslee.subwaywhen.data.network.DefaultTokenProvider
import com.yslee.subwaywhen.data.network.NetworkManager
import com.yslee.subwaywhen.data.network.NetworkManagerImpl
import com.yslee.subwaywhen.data.network.TokenProvider
import com.yslee.subwaywhen.data.remote.loadmodel.LoadModel
import com.yslee.subwaywhen.data.remote.loadmodel.LoadModelImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Singleton
    @Binds
    abstract fun bindNetworkManager(impl: NetworkManagerImpl): NetworkManager

    @Singleton
    @Binds
    abstract fun bindTokenProvider(impl: DefaultTokenProvider): TokenProvider

    @Singleton
    @Binds
    abstract fun bindLoadModel(impl: LoadModelImpl): LoadModel

    companion object {
        @Singleton
        @Provides
        fun provideJson(): Json = Json { ignoreUnknownKeys = true }

        @Singleton
        @Provides
        fun provideHttpClient(): HttpClient = HttpClient(OkHttp) {
            install(HttpTimeout) { requestTimeoutMillis = 10_000 }
        }
    }
}
