package io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.details

import androidx.compose.runtime.Immutable
import io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.mapper.SymbolPriceUiModel

interface SymbolDetailsContract {
    @Immutable
    data class State(
        val symbol: String = "",
        val symbolPrice: SymbolPriceUiModel? = null
    )

    sealed class Event {
        data object NavigateBack : Event()
    }

    sealed class Effect {
        data object NavigateBack : Effect()
    }
}
