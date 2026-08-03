package com.nursulton.giphytask.di

import com.nursulton.giphytask.BuildConfig
import com.nursulton.giphytask.core.network.ApiKeyInterceptor
import com.nursulton.giphytask.core.network.ConnectivityManagerNetworkMonitor
import com.nursulton.giphytask.core.network.NetworkMonitor
import com.nursulton.giphytask.data.remote.api.GiphyApi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private const val TIMEOUT_SECONDS = 15L

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        apiKeyInterceptor: ApiKeyInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(apiKeyInterceptor)
            .apply {
                if (BuildConfig.DEBUG) {
                    val loggingInterceptor = HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                    addInterceptor(loggingInterceptor)
                }
            }
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(GiphyApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideGiphyApi(retrofit: Retrofit): GiphyApi {
        return retrofit.create(GiphyApi::class.java)
    }
}

/** Interface -> implementation bindings; @Binds avoids hand-writing a @Provides factory. */
@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkBindsModule {

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(
        impl: ConnectivityManagerNetworkMonitor
    ): NetworkMonitor
}
