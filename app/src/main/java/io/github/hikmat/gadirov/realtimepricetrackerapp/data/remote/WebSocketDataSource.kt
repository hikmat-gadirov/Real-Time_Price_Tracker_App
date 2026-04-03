package io.github.hikmat.gadirov.realtimepricetrackerapp.data.remote

import io.github.hikmat.gadirov.realtimepricetrackerapp.data.remote.model.PriceUpdateDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface WebSocketDataSource {
    val incomingPrices: Flow<List<PriceUpdateDto>>
    val isConnected: StateFlow<Boolean>
    
    fun connect()
    fun disconnect()
    fun sendPrices(prices: List<PriceUpdateDto>)
}
