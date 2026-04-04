package io.github.hikmat.gadirov.realtimepricetrackerapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.hikmat.gadirov.realtimepricetrackerapp.BuildConfig
import io.github.hikmat.gadirov.realtimepricetrackerapp.util.BASE_WEBSOCKET_URL
import io.github.hikmat.gadirov.realtimepricetrackerapp.util.CONNECT_TIMEOUT_SECONDS
import io.github.hikmat.gadirov.realtimepricetrackerapp.util.READ_TIMEOUT_SECONDS
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Named(BASE_WEBSOCKET_URL)
    fun provideWebSocketUrl(): String {
        return BuildConfig.BASE_URL
    }
}
