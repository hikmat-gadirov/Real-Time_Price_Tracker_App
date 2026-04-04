package io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.components

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.hikmat.gadirov.realtimepricetrackerapp.R
import io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.mapper.SymbolPriceUiModel
import io.github.hikmat.gadirov.realtimepricetrackerapp.ui.theme.RealTimePriceTrackerAppTheme

@Composable
fun PriceRow(
    uiModel: SymbolPriceUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val flashColor = remember { Animatable(Color.Transparent) }

    LaunchedEffect(uiModel.formattedPrice) {
        val targetColor = uiModel.trendFlashColorRes
        if (targetColor != Color.Transparent) {
            flashColor.snapTo(targetColor)
            flashColor.animateTo(
                targetValue = Color.Transparent,
                animationSpec = tween(durationMillis = 1000)
            )
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = uiModel.symbol,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${uiModel.formattedPrice} ${stringResource(id = uiModel.trendIconStringRes)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = uiModel.trendColorRes,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .background(
                            color = flashColor.value, 
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PriceRowPreview() {
    RealTimePriceTrackerAppTheme {
        Column {
            PriceRow(
                uiModel = SymbolPriceUiModel("AAPL", "$150.25", Color.Green, Color(0x3300FF00), R.string.trend_up),
                onClick = {}
            )
            PriceRow(
                uiModel = SymbolPriceUiModel("TSLA", "$200.00", Color.Red, Color(0x33FF0000), R.string.trend_down),
                onClick = {}
            )
        }
    }
}
