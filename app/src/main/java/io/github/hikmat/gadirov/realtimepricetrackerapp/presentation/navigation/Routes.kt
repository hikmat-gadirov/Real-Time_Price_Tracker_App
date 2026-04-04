package io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object FeedRoute

@Serializable
data class DetailsRoute(val symbol: String)
