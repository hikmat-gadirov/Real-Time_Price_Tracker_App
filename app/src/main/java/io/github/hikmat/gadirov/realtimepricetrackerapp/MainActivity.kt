package io.github.hikmat.gadirov.realtimepricetrackerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import io.github.hikmat.gadirov.realtimepricetrackerapp.ui.theme.RealTimePriceTrackerAppTheme
import io.github.hikmat.gadirov.realtimepricetrackerapp.presentation.navigation.PriceTrackerNavHost

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RealTimePriceTrackerAppTheme {
                PriceTrackerNavHost()
            }
        }
    }
}