package io.github.hikmat.gadirov.realtimepricetrackerapp.data.repository

import io.github.hikmat.gadirov.realtimepricetrackerapp.data.generator.PriceGenerator
import io.github.hikmat.gadirov.realtimepricetrackerapp.data.network.NetworkConnectivityObserver
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PriceRepositoryImpl @Inject constructor(
    private val webSocketDataSource: WebSocketDataSource,
    private val priceGenerator: PriceGenerator,
    private val networkConnectivityObserver: NetworkConnectivityObserver,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : PriceRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private val feedMutex = Mutex()
    private var feedJob: Job? = null
    private var listeningJob: Job? = null

    // Pre-populate with baseline prices so the UI never shows empty states on cold start / deep links
    private val _prices = MutableStateFlow<Map<String, SymbolPrice>>(
        priceGenerator.getInitialBaselinePrices().mapValues { (symbol, price) ->
            SymbolPrice(symbol, price, PriceTrend.NEUTRAL)
        }
    )
    override val prices: StateFlow<Map<String, SymbolPrice>> = _prices.asStateFlow()

    override val isConnected: StateFlow<Boolean> = webSocketDataSource.isConnected

    init {
        // When WebSocket drops (server closed / idle timeout), cancel local jobs
        webSocketDataSource.isConnected.onEach { connected ->
            if (!connected) {
                feedMutex.withLock {
                    feedJob?.cancel()
                    listeningJob?.cancel()
                }
            }
        }.launchIn(repositoryScope)

        // When OS-level internet is lost, stop everything cleanly
        networkConnectivityObserver.isOnline.onEach { isOnline ->
            if (!isOnline) {
                feedMutex.withLock {
                    feedJob?.cancel()
                    listeningJob?.cancel()
                    webSocketDataSource.disconnect()
                }
            }
        }.launchIn(repositoryScope)
    }

    override fun startFeed() {
        // Fire-and-forget on the IO dispatcher, protected by mutex to avoid race conditions
        repositoryScope.launch(ioDispatcher) {
            feedMutex.withLock {
                if (feedJob?.isActive == true) return@withLock

                feedJob?.cancel()
                listeningJob?.cancel()

                webSocketDataSource.connect()
                startListening()

                // Send generated price ticks to the WebSocket echo server
                feedJob = priceGenerator.fakeUpdates
                    .onEach { updates -> webSocketDataSource.sendPrices(updates) }
                    .launchIn(repositoryScope)
            }
        }
    }

    override fun stopFeed() {
        repositoryScope.launch(ioDispatcher) {
            feedMutex.withLock {
                feedJob?.cancel()
                listeningJob?.cancel()
                webSocketDataSource.disconnect()
            }
        }
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
