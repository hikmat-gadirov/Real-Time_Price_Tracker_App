package io.github.hikmat.gadirov.realtimepricetrackerapp.domain.model

data class SymbolPrice(
    val symbol: String,
    val currentPrice: Double,
    val trend: PriceTrend = PriceTrend.NEUTRAL
)
