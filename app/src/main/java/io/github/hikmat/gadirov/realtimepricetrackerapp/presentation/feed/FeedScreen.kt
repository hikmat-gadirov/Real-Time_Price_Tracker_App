package io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.feed

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.hikmat.gadirov.realtimepricetrackerapp.R
import io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.components.ConnectionIndicator
import io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.components.PriceRow
import io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.components.StableTopBar
import io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.mapper.SymbolPriceUiModel
import io.github.hikmat.gadirov.realtimepricetrackerapp.ui.theme.RealTimePriceTrackerAppTheme

@Composable
fun FeedScreen(
    onNavigateToDetails: (String) -> Unit,
    viewModel: FeedViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is FeedContract.Effect.NavigateTo -> onNavigateToDetails(effect.route)
            }
        }
    }

    FeedScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun rememberIosFlingBehavior(): FlingBehavior {
    val flingSpec = remember { exponentialDecay<Float>(frictionMultiplier = 0.85f, absVelocityThreshold = 0.1f) }
    return remember(flingSpec) {
        object : FlingBehavior {
            override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                var velocityLeft = initialVelocity
                var lastValue = 0f
                AnimationState(
                    initialValue = 0f,
                    initialVelocity = initialVelocity,
                ).animateDecay(flingSpec) {
                    val delta = value - lastValue
                    val consumed = scrollBy(delta)
                    lastValue = value
                    velocityLeft = this.velocity
                    if (kotlin.math.abs(delta - consumed) > 0.5f) this.cancelAnimation()
                }
                return velocityLeft
            }
        }
    }
}

@Composable
fun FeedScreenContent(
    uiState: FeedContract.State,
    onEvent: (FeedContract.Event) -> Unit
) {
    val iosFlingBehavior = rememberIosFlingBehavior()
    Scaffold(
        topBar = { FeedTopBar(uiState, onEvent) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            flingBehavior = iosFlingBehavior
        ) {
            items(
                items = uiState.symbols,
                key = { it.symbol }
            ) { uiModel ->
                PriceRow(
                    uiModel = uiModel,
                    onClick = { onEvent(FeedContract.Event.NavigateToDetails(uiModel.symbol)) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
private fun FeedTopBar(
    uiState: FeedContract.State,
    onEvent: (FeedContract.Event) -> Unit
) {
    StableTopBar(
        title = stringResource(id = R.string.live_tracker_title),
        actions = {
            ConnectionIndicator(isConnected = uiState.isConnected)
            Spacer(modifier = Modifier.size(16.dp))
            Button(
                onClick = {
                    val event = if (uiState.isConnected) FeedContract.Event.StopFeed else FeedContract.Event.StartFeed
                    onEvent(event)
                },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(stringResource(id = if (uiState.isConnected) R.string.button_stop else R.string.button_start))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun FeedScreenPreview() {
    val mockSymbols = listOf(
        SymbolPriceUiModel("AAPL", "$150.00", Color.Green, Color.Transparent, R.string.trend_up),
        SymbolPriceUiModel("TSLA", "$250.00", Color.Red, Color.Transparent, R.string.trend_down),
    )
    
    RealTimePriceTrackerAppTheme {
        FeedScreenContent(
            uiState = FeedContract.State(isConnected = true, symbols = mockSymbols),
            onEvent = {}
        )
    }
}
