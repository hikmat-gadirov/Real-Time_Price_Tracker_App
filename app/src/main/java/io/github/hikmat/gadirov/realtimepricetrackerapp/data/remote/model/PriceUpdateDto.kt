package io.github.hikmat.gadirov.realtimepricetrackerapp.data.remote.model

import kotlinx.serialization.Serializable

@Serializable
data class PriceUpdateDto(
    val symbol: String,
    val price: Double
)
