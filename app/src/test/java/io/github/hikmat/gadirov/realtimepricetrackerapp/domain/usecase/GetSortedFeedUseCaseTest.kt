package io.github.hikmat.gadirov.realtimepricetrackerapp.domain.usecase

import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.model.SymbolPrice
import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.repository.PriceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetSortedFeedUseCaseTest {

    private class FakePriceRepository : PriceRepository {
        val _prices = MutableStateFlow<Map<String, SymbolPrice>>(emptyMap())
        override val prices: Flow<Map<String, SymbolPrice>> = _prices
        override val isConnected: Flow<Boolean> = MutableStateFlow(false)
        override fun startFeed() {}
        override fun stopFeed() {}
        override fun getSymbol(symbol: String): Flow<SymbolPrice?> = MutableStateFlow(null)
    }

    @Test
    fun `invoke should return list sorted by price descending`() = runTest {
        // Given
        val repository = FakePriceRepository()
        val useCase = GetSortedFeedUseCase(repository)
        
        val unsortedMap = mapOf(
            "AAPL" to SymbolPrice("AAPL", 150.0),
            "TSLA" to SymbolPrice("TSLA", 250.0),
            "GOOG" to SymbolPrice("GOOG", 200.0)
        )
        repository._prices.value = unsortedMap

        // When
        val result = useCase().first()

        // Then
        assertEquals(3, result.size)
        assertEquals("TSLA", result[0].symbol)
        assertEquals("GOOG", result[1].symbol)
        assertEquals("AAPL", result[2].symbol)
        assertEquals(250.0, result[0].currentPrice, 0.0)
        assertEquals(200.0, result[1].currentPrice, 0.0)
        assertEquals(150.0, result[2].currentPrice, 0.0)
    }
}
