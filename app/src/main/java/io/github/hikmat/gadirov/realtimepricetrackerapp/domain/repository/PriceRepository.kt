package io.github.hikmat.gadirov.realtimepricetrackerapp.domain.repository

import io.github.hikmat.gadirov.realtimepricetrackerapp.domain.model.SymbolPrice
import kotlinx.coroutines.flow.StateFlow

interface PriceRepository {
    
    /**
     * A map of Symbol -> SymbolPrice to represent the current known state of all tracked stocks.
     */
    val prices: StateFlow<Map<String, SymbolPrice>>

    /**
     * Represents the connection state to the real-time server (WebSocket)
     */
    val isConnected: StateFlow<Boolean>

    /**
     * Connects to the feed and starts generating live prices.
     */
    fun startFeed()

    /**
     * Disconnects the feed.
     */
    fun stopFeed()
}
