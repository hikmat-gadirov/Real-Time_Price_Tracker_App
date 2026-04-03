package io.github.hikmat.gadirov.realtimepricetrackerapp.data.generator

import io.github.hikmat.gadirov.realtimepricetrackerapp.data.remote.model.PriceUpdateDto
import io.github.hikmat.gadirov.realtimepricetrackerapp.util.DEFAULT_FALLBACK_PRICE
import io.github.hikmat.gadirov.realtimepricetrackerapp.util.MAX_INITIAL_PRICE
import io.github.hikmat.gadirov.realtimepricetrackerapp.util.MAX_PRICE_CHANGE_PERCENT
import io.github.hikmat.gadirov.realtimepricetrackerapp.util.MIN_INITIAL_PRICE
import io.github.hikmat.gadirov.realtimepricetrackerapp.util.POLLING_INTERVAL
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class PriceGenerator @Inject constructor() {

    private val symbols = listOf(
        "AAPL", "GOOG", "TSLA", "AMZN", "MSFT", 
        "NVDA", "META", "NFLX", "AMD", "INTC", 
        "CSCO", "PEP", "NOK", "PLUG", "NKE",
        "ADBE", "AAL", "AMAT", "SNAP", "PYPL",
        "SBUX", "ISRG", "GILD", "MBG-USD", "PLTR"
    )

    // Initial baseline prices for the 25 symbols
    private val currentPrices = symbols.associateWith {
        Random.nextDouble(MIN_INITIAL_PRICE, MAX_INITIAL_PRICE)
    }.toMutableMap()

    val fakeUpdates: Flow<List<PriceUpdateDto>> = flow {
        // Emit initial values first
        emit(currentPrices.map { PriceUpdateDto(it.key, it.value) })

        while (true) {
            delay(POLLING_INTERVAL.seconds)
            val updates = symbols.map { symbol ->
                val oldPrice = currentPrices[symbol] ?: DEFAULT_FALLBACK_PRICE
                // Fluctuate price by up to defined max volatility
                val changePercent = Random.nextDouble(-MAX_PRICE_CHANGE_PERCENT, MAX_PRICE_CHANGE_PERCENT)
                // Calculate the new price by applying the percentage change multiplier
                // e.g., $100 * (1 + 0.05) = $105 (+5%) or $100 * (1 - 0.05) = $95 (-5%)
                val newPrice = oldPrice * (1 + changePercent)
                currentPrices[symbol] = newPrice
                PriceUpdateDto(symbol, newPrice)
            }
            emit(updates)
        }
    }
}
