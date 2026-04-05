package io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.feed

import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.model.SymbolPrice
import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.repository.PriceRepository
import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.usecase.GetSortedFeedUseCase
import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.usecase.StartFeedUseCase
import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.usecase.StopFeedUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakePriceRepository : PriceRepository {
        val _isConnected = MutableStateFlow(false)
        val _prices = MutableStateFlow<Map<String, SymbolPrice>>(emptyMap())
        
        var startCalled = 0
        var stopCalled = 0

        override val prices: Flow<Map<String, SymbolPrice>> = _prices
        override val isConnected: Flow<Boolean> = _isConnected
        override fun startFeed() { startCalled++ }
        override fun stopFeed() { stopCalled++ }
        override fun getSymbol(symbol: String): Flow<SymbolPrice?> = MutableStateFlow(null)
    }

    private lateinit var repository: FakePriceRepository
    private lateinit var viewModel: FeedViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakePriceRepository()
        val getSortedFeedUseCase = GetSortedFeedUseCase(repository)
        val startFeedUseCase = StartFeedUseCase(repository)
        val stopFeedUseCase = StopFeedUseCase(repository)
        
        viewModel = FeedViewModel(
            repository = repository,
            getSortedFeedUseCase = getSortedFeedUseCase,
            startFeedUseCase = startFeedUseCase,
            stopFeedUseCase = stopFeedUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onEvent StartFeed should call repository startFeed`() = runTest {
        // When
        viewModel.onEvent(FeedContract.Event.StartFeed)
        
        // Then
        assertEquals(1, repository.startCalled)
    }

    @Test
    fun `onEvent StopFeed should call repository stopFeed`() = runTest {
        // When
        viewModel.onEvent(FeedContract.Event.StopFeed)
        
        // Then
        assertEquals(1, repository.stopCalled)
    }

    @Test
    fun `viewModel should emit UI state from repository`() = runTest {
        // Given
        repository._isConnected.value = true
        repository._prices.value = mapOf("AAPL" to SymbolPrice("AAPL", 150.0))
        
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals(true, state.isConnected)
        assertEquals(1, state.symbols.size)
        assertEquals("AAPL", state.symbols[0].symbol)
        assertEquals("$150.00", state.symbols[0].formattedPrice)
    }
}
