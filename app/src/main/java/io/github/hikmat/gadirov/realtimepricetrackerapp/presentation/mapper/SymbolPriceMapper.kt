package io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.mapper

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import io.github.hikmat.gadirov.realtimepricetrackerapp.R
import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.model.PriceTrend
import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.model.SymbolPrice
import io.github.hikmat.gadirov.realtimepricetrackerapp.ui.theme.SolidGreen
import io.github.hikmat.gadirov.realtimepricetrackerapp.ui.theme.SolidRed
import io.github.hikmat.gadirov.realtimepricetrackerapp.ui.theme.TransparentGreen
import io.github.hikmat.gadirov.realtimepricetrackerapp.ui.theme.TransparentRed
import java.util.Locale

@Immutable
data class SymbolPriceUiModel(
    val symbol: String,
    val formattedPrice: String,
    val trendColorRes: Color,
    val trendFlashColorRes: Color,
    val trendIconStringRes: Int
)

fun SymbolPrice.toUiModel(): SymbolPriceUiModel {
    val formattedStr = String.format(Locale.US, "%.2f", this.currentPrice)
    return SymbolPriceUiModel(
        symbol = this.symbol,
        formattedPrice = "$$formattedStr",
        trendColorRes = this.trend.toTextColor(),
        trendFlashColorRes = this.trend.toFlashColor(),
        trendIconStringRes = this.trend.toIconRes()
    )
}

fun PriceTrend.toTextColor(): Color = when (this) {
    PriceTrend.UP -> SolidGreen
    PriceTrend.DOWN -> SolidRed
    PriceTrend.NEUTRAL -> Color.Gray
}

fun PriceTrend.toFlashColor(): Color = when (this) {
    PriceTrend.UP -> TransparentGreen
    PriceTrend.DOWN -> TransparentRed
    PriceTrend.NEUTRAL -> Color.Transparent
}

fun PriceTrend.toIconRes(): Int = when (this) {
    PriceTrend.UP -> R.string.trend_up
    PriceTrend.DOWN -> R.string.trend_down
    PriceTrend.NEUTRAL -> R.string.trend_neutral
}