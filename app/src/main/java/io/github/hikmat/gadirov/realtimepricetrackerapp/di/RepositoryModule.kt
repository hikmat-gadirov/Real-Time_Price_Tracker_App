package io.github.hikmat.gadirov.realtimepricetrackerapp.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.hikmat.gadirov.realtimepricetrackerapp.data.remote.WebSocketDataSource
import io.github.hikmat.gadirov.realtimepricetrackerapp.data.remote.WebSocketDataSourceImpl
import io.github.hikmat.gadirov.realtimepricetrackerapp.data.repository.PriceRepositoryImpl
import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.repository.PriceRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindWebSocketDataSource(
        impl: WebSocketDataSourceImpl
    ): WebSocketDataSource

    @Binds
    abstract fun bindPriceRepository(
        impl: PriceRepositoryImpl
    ): PriceRepository
}
