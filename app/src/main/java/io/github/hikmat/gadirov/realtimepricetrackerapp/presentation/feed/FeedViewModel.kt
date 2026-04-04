package io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.usecase.GetSortedFeedUseCase
import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.usecase.ObserveConnectionUseCase
import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.usecase.StartFeedUseCase
import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.usecase.StopFeedUseCase
import io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.mapper.toUiModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    observeConnectionUseCase: ObserveConnectionUseCase,
    getSortedFeedUseCase: GetSortedFeedUseCase,
    private val startFeedUseCase: StartFeedUseCase,
    private val stopFeedUseCase: StopFeedUseCase
) : ViewModel() {

    val uiState: StateFlow<FeedContract.State> = combine(
        observeConnectionUseCase(),
        getSortedFeedUseCase()
    ) { connected, sortedList ->
        FeedContract.State(
            isConnected = connected,
            symbols = sortedList.map { it.toUiModel() }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FeedContract.State()
    )

    private val _effect = MutableSharedFlow<FeedContract.Effect>()
    val effect: SharedFlow<FeedContract.Effect> = _effect.asSharedFlow()

    fun onEvent(event: FeedContract.Event) {
        when (event) {
            is FeedContract.Event.StartFeed -> startFeedUseCase()
            is FeedContract.Event.StopFeed -> stopFeedUseCase()
            is FeedContract.Event.NavigateToDetails -> handleNavigation(event.symbol)
        }
    }

    private fun handleNavigation(symbol: String) {
        viewModelScope.launch {
            _effect.emit(FeedContract.Effect.NavigateTo(symbol))
        }
    }
}
