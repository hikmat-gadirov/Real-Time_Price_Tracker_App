package io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.feed

import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.model.PriceTrend
import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.model.SymbolPrice
import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.repository.PriceRepository
import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.usecase.GetSortedFeedUseCase
import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.usecase.ObserveConnectionUseCase
import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.usecase.StartFeedUseCase
import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.usecase.StopFeedUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [FeedViewModel].
 *
 * Uses [StandardTestDispatcher] to replace the Main dispatcher so that
 * StateFlow emissions driven by [viewModelScope] are fully controllable.
 *
 * Fake dependencies implement the real [PriceRepository] interface exactly —
 * StateFlow for both [prices] and [isConnected], matching the production code.
 */

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    // ---------------------------------------------------------------------------
    // Fake dependencies
    // ---------------------------------------------------------------------------

    private class FakePriceRepository : PriceRepository {
        val connectedFlow = MutableStateFlow(false)
        val pricesFlow = MutableStateFlow<Map<String, SymbolPrice>>(emptyMap())

        var startCalled = 0
        var stopCalled = 0

        override val prices: StateFlow<Map<String, SymbolPrice>> = pricesFlow
        override val isConnected: StateFlow<Boolean> = connectedFlow
        override fun startFeed() { startCalled++ }
        override fun stopFeed() { stopCalled++ }
    }

    // ---------------------------------------------------------------------------
    // SUT
    // ---------------------------------------------------------------------------

    private lateinit var repository: FakePriceRepository
    private lateinit var viewModel: FeedViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakePriceRepository()

        // FeedViewModel takes UseCases — never the repository directly (Clean Architecture)
        viewModel = FeedViewModel(
            observeConnectionUseCase = ObserveConnectionUseCase(repository),
            getSortedFeedUseCase = GetSortedFeedUseCase(repository),
            startFeedUseCase = StartFeedUseCase(repository),
            stopFeedUseCase = StopFeedUseCase(repository)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---------------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------------

    @Test
    fun `initial state has isConnected false and empty symbol list`() = runTest {
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isConnected)
        assertEquals(0, state.symbols.size)
    }

    @Test
    fun `StartFeed event calls repository startFeed once`() = runTest {
        viewModel.onEvent(FeedContract.Event.StartFeed)
        advanceUntilIdle()

        assertEquals(1, repository.startCalled)
    }

    @Test
    fun `StopFeed event calls repository stopFeed once`() = runTest {
        viewModel.onEvent(FeedContract.Event.StopFeed)
        advanceUntilIdle()

        assertEquals(1, repository.stopCalled)
    }

    @Test
    fun `uiState reflects connected status from repository`() = runTest {
        repository.connectedFlow.value = true
        advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.isConnected)
    }

    @Test
    fun `uiState reflects disconnected status from repository`() = runTest {
        repository.connectedFlow.value = true
        advanceUntilIdle()

        repository.connectedFlow.value = false
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.isConnected)
    }

    @Test
    fun `uiState maps symbol prices from repository sorted by price descending`() = runTest {
        repository.pricesFlow.value = mapOf(
            "AAPL" to SymbolPrice("AAPL", 150.0, PriceTrend.NEUTRAL),
            "TSLA" to SymbolPrice("TSLA", 250.0, PriceTrend.UP),
            "GOOG" to SymbolPrice("GOOG", 200.0, PriceTrend.DOWN)
        )
        advanceUntilIdle()

        val symbols = viewModel.uiState.value.symbols

        assertEquals(3, symbols.size)
        assertEquals("TSLA", symbols[0].symbol)
        assertEquals("GOOG", symbols[1].symbol)
        assertEquals("AAPL", symbols[2].symbol)
    }

    @Test
    fun `uiState formats prices as USD currency string`() = runTest {
        repository.pricesFlow.value = mapOf(
            "AAPL" to SymbolPrice("AAPL", 150.0, PriceTrend.NEUTRAL)
        )
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.symbols.size)
        assertEquals("AAPL", state.symbols[0].symbol)
        assertEquals($$"$150.00", state.symbols[0].formattedPrice)
    }

    @Test
    fun `NavigateToDetails event emits navigation effect with correct symbol`() = runTest {
        val receivedEffects = mutableListOf<FeedContract.Effect>()

        // Collect effects in a background coroutine during the test
        backgroundScope.launch {
            viewModel.effect.collect { receivedEffects.add(it) }
        }

        viewModel.onEvent(FeedContract.Event.NavigateToDetails("NVDA"))
        advanceUntilIdle()

        assertEquals(1, receivedEffects.size)
        assertEquals(FeedContract.Effect.NavigateTo("NVDA"), receivedEffects[0])
    }
}
