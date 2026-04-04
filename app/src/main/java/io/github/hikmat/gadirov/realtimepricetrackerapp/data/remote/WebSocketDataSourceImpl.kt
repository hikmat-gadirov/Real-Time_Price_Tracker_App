package io.github.hikmat.gadirov.realtimepricetrackerapp.data.remote

import io.github.hikmat.gadirov.realtimepricetrackerapp.data.remote.model.PriceUpdateDto
import io.github.hikmat.gadirov.realtimepricetrackerapp.util.BASE_WEBSOCKET_URL
import io.github.hikmat.gadirov.realtimepricetrackerapp.util.POLLING_INTERVAL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Named
import kotlin.time.Duration.Companion.seconds

class WebSocketDataSourceImpl @Inject constructor(
    private val client: OkHttpClient,
    @Named(BASE_WEBSOCKET_URL) private val webSocketUrl: String
) : WebSocketDataSource {

    private var webSocket: WebSocket? = null

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _incomingPrices = MutableSharedFlow<List<PriceUpdateDto>>(extraBufferCapacity = 1)
    override val incomingPrices: Flow<List<PriceUpdateDto>> = _incomingPrices

    private var reconnectAttempts = 0
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var pendingReconnectJob: Job? = null

    override fun connect() {
        connectInternal(isRetry = false)
    }

    private fun connectInternal(isRetry: Boolean) {
        if (!isRetry) {
            reconnectAttempts = 0
        }

        if (_isConnected.value && !isRetry) return

        val request = Request.Builder()
            .url(webSocketUrl)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                reconnectAttempts = 0
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
                // Only retry if we haven't been explicitly disconnected by the user/network observer
                if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                    reconnectAttempts++
                    pendingReconnectJob = scope.launch {
                        delay(POLLING_INTERVAL.seconds)
                        connectInternal(isRetry = true)
                    }
                } else {
                    // All retries exhausted - give up and signal disconnected
                    _isConnected.value = false
                    reconnectAttempts = 0
                }
            }
        })
    }

    override fun disconnect() {
        // Cancel any pending retry before closing - prevents zombie reconnections
        pendingReconnectJob?.cancel()
        pendingReconnectJob = null
        reconnectAttempts = 0

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

    companion object {
        private const val MAX_RECONNECT_ATTEMPTS = 3
    }
}
