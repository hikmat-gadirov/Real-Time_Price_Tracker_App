package io.github.hikmat.gadirov.realtimepricetrackerapp.data.repository

import io.github.hikmat.gadirov.realtimepricetrackerapp.data.generator.PriceGenerator
import io.github.hikmat.gadirov.realtimepricetrackerapp.data.remote.WebSocketDataSource
import io.github.hikmat.gadirov.realtimepricetrackerapp.di.IoDispatcher
import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.model.PriceTrend
import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.model.SymbolPrice
import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.repository.PriceRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PriceRepositoryImpl @Inject constructor(
    private val webSocketDataSource: WebSocketDataSource,
    private val priceGenerator: PriceGenerator,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PriceRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)
    private var feedJob: Job? = null
    private var listeningJob: Job? = null

    private val _prices = MutableStateFlow<Map<String, SymbolPrice>>(emptyMap())
    override val prices: StateFlow<Map<String, SymbolPrice>> = _prices.asStateFlow()

    override val isConnected: StateFlow<Boolean> = webSocketDataSource.isConnected

    override fun startFeed() {
        if (feedJob?.isActive == true) return

        webSocketDataSource.connect()
        startListening()

        // Start generating fake updates and sending them to the echo server
        feedJob = priceGenerator.fakeUpdates
            .onEach { updates ->
                webSocketDataSource.sendPrices(updates)
            }
            .launchIn(repositoryScope)
    }

    override fun stopFeed() {
        feedJob?.cancel()
        listeningJob?.cancel()
        webSocketDataSource.disconnect()
    }

    private fun startListening() {
        if (listeningJob?.isActive == true) return
        
        listeningJob = webSocketDataSource.incomingPrices
            .onEach { updates ->
                _prices.update { currentMap ->
                    val newMap = currentMap.toMutableMap()
                    updates.forEach { update ->
                        val oldSymbolPrice = currentMap[update.symbol]
                        val trend = when {
                            oldSymbolPrice == null -> PriceTrend.NEUTRAL
                            update.price > oldSymbolPrice.currentPrice -> PriceTrend.UP
                            update.price < oldSymbolPrice.currentPrice -> PriceTrend.DOWN
                            else -> PriceTrend.NEUTRAL
                        }
                        newMap[update.symbol] = SymbolPrice(
                            symbol = update.symbol,
                            currentPrice = update.price,
                            trend = trend
                        )
                    }
                    newMap
                }
            }
            .launchIn(repositoryScope)
    }
}
