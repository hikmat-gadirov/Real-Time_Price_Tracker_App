package io.github.hikmat.gadirov.realtimepricetrackerapp.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class SymbolPrice(
    val symbol: String,
    val currentPrice: Double,
    val trend: PriceTrend = PriceTrend.NEUTRAL
)
