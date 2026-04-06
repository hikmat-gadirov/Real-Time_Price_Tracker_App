package io.github.hikmat.gadirov.realtimepricetrackerapp.domain.usecase

import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.model.PriceTrend
import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.model.SymbolPrice
import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.repository.PriceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetSortedFeedUseCaseTest {

    /**
     * Minimal fake that satisfies the current PriceRepository interface.
     * Uses StateFlow (not plain Flow) to match the interface exactly.
     */
    private class FakePriceRepository : PriceRepository {
        val pricesFlow = MutableStateFlow<Map<String, SymbolPrice>>(emptyMap())
        override val prices: StateFlow<Map<String, SymbolPrice>> = pricesFlow
        override val isConnected: StateFlow<Boolean> = MutableStateFlow(false)
        override fun startFeed() {}
        override fun stopFeed() {}
    }

    @Test
    fun `invoke should return list sorted by price descending`() = runTest {
        // Given
        val repository = FakePriceRepository()
        val useCase = GetSortedFeedUseCase(repository)

        repository.pricesFlow.value = mapOf(
            "AAPL" to SymbolPrice("AAPL", 150.0, PriceTrend.NEUTRAL),
            "TSLA" to SymbolPrice("TSLA", 250.0, PriceTrend.UP),
            "GOOG" to SymbolPrice("GOOG", 200.0, PriceTrend.DOWN)
        )

        // When
        val result = useCase().first()

        // Then — highest price first
        assertEquals(3, result.size)
        assertEquals("TSLA", result[0].symbol)
        assertEquals("GOOG", result[1].symbol)
        assertEquals("AAPL", result[2].symbol)
        assertEquals(250.0, result[0].currentPrice, 0.0)
        assertEquals(200.0, result[1].currentPrice, 0.0)
        assertEquals(150.0, result[2].currentPrice, 0.0)
    }

    @Test
    fun `invoke should return empty list when repository has no prices`() = runTest {
        // Given
        val repository = FakePriceRepository()
        val useCase = GetSortedFeedUseCase(repository)

        // When
        val result = useCase().first()

        // Then
        assertEquals(0, result.size)
    }

    @Test
    fun `invoke should handle single symbol correctly`() = runTest {
        // Given
        val repository = FakePriceRepository()
        val useCase = GetSortedFeedUseCase(repository)

        repository.pricesFlow.value = mapOf(
            "NVDA" to SymbolPrice("NVDA", 900.0, PriceTrend.UP)
        )

        // When
        val result = useCase().first()

        // Then
        assertEquals(1, result.size)
        assertEquals("NVDA", result[0].symbol)
        assertEquals(900.0, result[0].currentPrice, 0.0)
    }

    @Test
    fun `invoke should keep order stable when prices are equal`() = runTest {
        // Given
        val repository = FakePriceRepository()
        val useCase = GetSortedFeedUseCase(repository)

        repository.pricesFlow.value = mapOf(
            "AAPL" to SymbolPrice("AAPL", 100.0, PriceTrend.NEUTRAL),
            "MSFT" to SymbolPrice("MSFT", 100.0, PriceTrend.NEUTRAL)
        )

        // When
        val result = useCase().first()

        // Then — both symbols present, sorted descending (stable for equal values)
        assertEquals(2, result.size)
        assertEquals(100.0, result[0].currentPrice, 0.0)
        assertEquals(100.0, result[1].currentPrice, 0.0)
    }
}
