package io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.usecase.GetSymbolUseCase
import io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.mapper.toUiModel
import io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.navigation.DetailsRoute
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.usecase.StartFeedUseCase
import kotlinx.coroutines.flow.onStart

@HiltViewModel
class SymbolDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getSymbolUseCase: GetSymbolUseCase,
    startFeedUseCase: StartFeedUseCase
) : ViewModel() {

    private val routeDetails = savedStateHandle.toRoute<DetailsRoute>()
    private val requestedSymbol = routeDetails.symbol

    val uiState: StateFlow<SymbolDetailsContract.State> = getSymbolUseCase(requestedSymbol)
        .map { priceDomainObj -> 
            SymbolDetailsContract.State(
                symbol = requestedSymbol,
                symbolPrice = priceDomainObj?.toUiModel()
            )
        }.onStart {
            startFeedUseCase()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SymbolDetailsContract.State(symbol = requestedSymbol)
        )

    private val _effect = MutableSharedFlow<SymbolDetailsContract.Effect>()
    val effect: SharedFlow<SymbolDetailsContract.Effect> = _effect.asSharedFlow()

    fun onEvent(event: SymbolDetailsContract.Event) {
        when (event) {
            is SymbolDetailsContract.Event.NavigateBack -> handleNavigateBack()
        }
    }

    private fun handleNavigateBack() {
        viewModelScope.launch {
            _effect.emit(SymbolDetailsContract.Effect.NavigateBack)
        }
    }
}
