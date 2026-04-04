package io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.details.SymbolDetailsScreen
import io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.feed.FeedScreen

@Composable
fun PriceTrackerNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = FeedRoute
    ) {
        composable<FeedRoute> {
            FeedScreen(
                onNavigateToDetails = { symbol ->
                    navController.navigate(DetailsRoute(symbol))
                }
            )
        }

        composable<DetailsRoute>(
            deepLinks = listOf(
                navDeepLink<DetailsRoute>(basePath = io.github.hikmat.gadirov.realtimepricetrackerapp.BuildConfig.DEEP_LINK_URI_PATTERN)
            )
        ) {
            SymbolDetailsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
