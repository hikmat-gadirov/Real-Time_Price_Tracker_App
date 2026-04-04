package io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.feed

import androidx.compose.runtime.Immutable
import io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.mapper.SymbolPriceUiModel

interface FeedContract {
    @Immutable
    data class State(
        val isConnected: Boolean = false,
        val symbols: List<SymbolPriceUiModel> = emptyList()
    )

    sealed class Event {
        data object StartFeed : Event()
        data object StopFeed : Event()
        data class NavigateToDetails(val symbol: String) : Event()
    }

    sealed class Effect {
        data class NavigateTo(val route: String) : Effect()
    }
}
