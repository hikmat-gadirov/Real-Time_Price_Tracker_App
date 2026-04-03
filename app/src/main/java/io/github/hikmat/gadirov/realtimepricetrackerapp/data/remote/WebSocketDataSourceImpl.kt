package io.github.hikmat.gadirov.realtimepricetrackerapp.data.remote

import io.github.hikmat.gadirov.realtimepricetrackerapp.data.remote.model.PriceUpdateDto
import io.github.hikmat.gadirov.realtimepricetrackerapp.util.BASE_URL
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject

class WebSocketDataSourceImpl @Inject constructor(
    private val client: OkHttpClient
) : WebSocketDataSource {

    private var webSocket: WebSocket? = null

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _incomingPrices = MutableSharedFlow<List<PriceUpdateDto>>(extraBufferCapacity = 1)
    override val incomingPrices: Flow<List<PriceUpdateDto>> = _incomingPrices

    override fun connect() {
        if (_isConnected.value) return

        val request = Request.Builder()
            .url(BASE_URL)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _isConnected.value = true
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val decoded = Json.decodeFromString<List<PriceUpdateDto>>(text)
                    _incomingPrices.tryEmit(decoded)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _isConnected.value = false
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _isConnected.value = false
            }
        })
    }

    override fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        _isConnected.value = false
    }

    override fun sendPrices(prices: List<PriceUpdateDto>) {
        if (_isConnected.value) {
            val json = Json.encodeToString(prices)
            webSocket?.send(json)
        }
    }
}
