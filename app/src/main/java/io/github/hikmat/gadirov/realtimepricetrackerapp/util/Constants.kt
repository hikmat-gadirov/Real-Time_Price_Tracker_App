package io.github.hikmat.gadirov.realtimepricetrackerapp.util

const val BASE_URL = "wss://ws.postman-echo.com/raw"

const val READ_TIMEOUT_SECONDS = 30L
const val CONNECT_TIMEOUT_SECONDS = 30L

const val POLLING_INTERVAL = 2L

// Initial Simulation Bounds
const val MIN_INITIAL_PRICE = 50.0
const val MAX_INITIAL_PRICE = 500.0
const val DEFAULT_FALLBACK_PRICE = 100.0

// Volatility (Max 5% change per tick)
const val MAX_PRICE_CHANGE_PERCENT = 0.05
