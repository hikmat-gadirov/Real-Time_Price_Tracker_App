package io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.components.StableTopBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.hikmat.gadirov.realtimepricetrackerapp.R
import io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.mapper.SymbolPriceUiModel
import io.github.hikmat.gadirov.realtimepricetrackerapp.ui.theme.RealTimePriceTrackerAppTheme

@Composable
fun SymbolDetailsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SymbolDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SymbolDetailsContract.Effect.NavigateBack -> onNavigateBack()
            }
        }
    }

    SymbolDetailsContent(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun SymbolDetailsContent(
    uiState: SymbolDetailsContract.State,
    onEvent: (SymbolDetailsContract.Event) -> Unit
) {
    Scaffold(
        topBar = { DetailsTopBar(uiState.symbol, onEvent) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = uiState.symbol,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            val currentStr = uiState.symbolPrice?.formattedPrice ?: stringResource(R.string.empty_price)
            val color = uiState.symbolPrice?.trendColorRes ?: MaterialTheme.colorScheme.onSurface
            val iconResId = uiState.symbolPrice?.trendIconStringRes ?: R.string.trend_neutral
            
            Text(
                text = "$currentStr ${stringResource(id = iconResId)}",
                style = MaterialTheme.typography.displayMedium,
                color = color
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(id = R.string.about_title, uiState.symbol),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.about_description, uiState.symbol),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
private fun DetailsTopBar(
    symbol: String,
    onEvent: (SymbolDetailsContract.Event) -> Unit
) {
    StableTopBar(
        title = stringResource(id = R.string.details_title, symbol),
        navigationIcon = {
            TextButton(
                onClick = { onEvent(SymbolDetailsContract.Event.NavigateBack) }
            ) {
                Text(stringResource(id = R.string.back))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun SymbolDetailsScreenPreview() {
    RealTimePriceTrackerAppTheme {
        SymbolDetailsContent(
            uiState = SymbolDetailsContract.State(
                symbol = "AAPL",
                symbolPrice = SymbolPriceUiModel("AAPL", "$152.00", Color.Green, Color.Transparent, R.string.trend_up)
            ),
            onEvent = {}
        )
    }
}
