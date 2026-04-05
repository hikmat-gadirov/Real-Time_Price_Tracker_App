# Real-Time Price Tracker

A production-grade Android application built with **Jetpack Compose** that displays real-time stock price updates for 25 symbols, powered by a WebSocket echo server. Built as part of the MultiBank Group Android Engineer coding challenge.

---

## 📱 Screenshots

> Launch the app → tap **Start** to connect → watch 25 symbols update live every 2 seconds. Tap any row to view the symbol detail screen. Deep-link directly to any symbol via `stocks://symbol/AAPL`.

---

## ✅ Features

### Core
- **Live Price Feed** — 25 stock symbols (AAPL, GOOG, TSLA, AMZN, MSFT, NVDA, META, NFLX…) updating every 2 seconds via WebSocket echo
- **Sorted List** — prices always sorted highest → lowest
- **Price Change Indicator** — green ↑ on increase, red ↓ on decrease
- **Symbol Details Screen** — selected symbol, current price with trend, and a description

### Bonus (All Implemented)
- **Price Flash Animation** — price badge flashes green for 1 second on increase, red for 1 second on decrease
- **Deep Link** — `stocks://symbol/{symbol}` opens the Details screen directly and auto-starts the feed
- **Light & Dark Theme** — follows system theme automatically

### Production-Grade Reliability
- **3-Retry Mechanism** — on WebSocket `onFailure`, automatically retries up to 3 times (2s delay each) before giving up
- **Internet Connectivity Monitoring** — uses Android `ConnectivityManager` to detect real network loss and stop the feed gracefully
- **Thread-Safe State** — `Mutex`-protected feed lifecycle prevents race conditions between UI and IO threads
- **No Zombie Reconnections** — pending retry coroutines are cancelled immediately on explicit disconnect

---

## 🏗️ Architecture

### Clean Architecture — 3 Layers

```
presentation/    ← Compose UI + ViewModels + Contracts (MVI/UDF)
domain/          ← UseCases + Repository interface + Domain models
data/            ← Repository impl + WebSocket DataSource + Generator + Network observer
```

### MVI / UDF Pattern

Every screen follows a strict **Unidirectional Data Flow**:

```
UI Event → ViewModel.onEvent() → UseCase → Repository → StateFlow → UI State → Recompose
                                                    ↓
                                             Effect (one-off)
```

Each screen has a `Contract` interface defining:
- `State` — `@Immutable` data class (drives UI)  
- `Event` — sealed class (user intents)
- `Effect` — sealed class (one-time side effects like navigation)

### SOLID Principles Applied

| Principle | Implementation |
|---|---|
| **S** — Single Responsibility | Each UseCase does exactly one thing |
| **O** — Open/Closed | `PriceRepository` interface — open for extension, closed for modification |
| **L** — Liskov Substitution | `PriceRepositoryImpl` is a complete drop-in for `PriceRepository` |
| **I** — Interface Segregation | `WebSocketDataSource` exposes only what the repository needs |
| **D** — Dependency Inversion | All dependencies injected via Hilt; data and domain layers never know about presentation |

---

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| UI | Jetpack Compose 100% |
| Architecture | MVVM + MVI/UDF + Clean Architecture |
| Navigation | Navigation Compose with `NavHost`, type-safe routes |
| Async | Kotlin Coroutines + `StateFlow` + `SharedFlow` |
| Networking | OkHttp WebSocket |
| Serialization | `kotlinx.serialization` |
| DI | Hilt |
| State | `SavedStateHandle` for symbol in details, `SharingStarted.WhileSubscribed(5000)` |
| Connectivity | Android `ConnectivityManager` `NetworkCallback` |
| Performance | `@Immutable`/`@Stable` on all models, Strong Skipping enabled, `animateItem()` in `LazyColumn` |

---

## 🌐 WebSocket Integration

- **URL:** `wss://ws.postman-echo.com/raw` (configured via `BuildConfig.BASE_URL`)
- **Flow:**
  1. Generator produces a random price tick for all 25 symbols every 2 seconds
  2. Prices are serialized as JSON and **sent** to the echo server
  3. Server **echoes** the same JSON back
  4. Repository receives the echo, computes trend (UP/DOWN/NEUTRAL), updates `StateFlow`
  5. UI recomposes with new prices

---

## 🔗 Deep Link

Open any symbol's detail screen directly:

```bash
# Via ADB
adb shell am start -a android.intent.action.VIEW -d "stocks://symbol/TSLA"

# Format
stocks://symbol/{SYMBOL}
```

The ViewModel auto-starts the WebSocket feed when the app is launched from a deep link.

---

## 📁 Project Structure

```
app/src/main/java/.../
├── data/
│   ├── generator/         PriceGenerator — fake tick producer
│   ├── network/           NetworkConnectivityObserver
│   ├── remote/            WebSocketDataSource interface + impl
│   │   └── model/         PriceUpdateDto (serializable DTO)
│   └── repository/        PriceRepositoryImpl (Singleton)
├── di/
│   ├── AppModule.kt       Coroutine dispatcher bindings
│   ├── NetworkModule.kt   OkHttpClient + WebSocket URL
│   └── RepositoryModule.kt
├── domain/
│   ├── model/             SymbolPrice, PriceTrend (domain entities)
│   ├── repository/        PriceRepository interface
│   └── usecase/           GetSortedFeedUseCase, GetSymbolUseCase,
│                          StartFeedUseCase, StopFeedUseCase,
│                          ObserveConnectionUseCase
├── presentation/
│   ├── components/        PriceRow, ConnectionIndicator, StableTopBar
│   ├── details/           SymbolDetailsScreen + ViewModel + Contract
│   ├── feed/              FeedScreen + FeedViewModel + FeedContract
│   ├── mapper/            SymbolPrice → SymbolPriceUiModel
│   └── navigation/        PriceTrackerNavHost, FeedRoute, DetailsRoute
└── ui/theme/              Light + Dark Material3 theme
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 26+
- JDK 17

### Run
```bash
git clone https://github.com/hikmat-gadirov/Real-Time_Price_Tracker_App.git
cd Real-Time_Price_Tracker_App
./gradlew installDebug
```

Or open the project in Android Studio and press **Run**.

---

## 🔧 Build Configuration

Environment is fully configured via `build.gradle.kts`:

```kotlin
buildConfigField("String", "BASE_URL", "\"wss://ws.postman-echo.com/raw\"")
buildConfigField("String", "DEEP_LINK_URI_PATTERN", "\"stocks://symbol\"")
manifestPlaceholders["deepLinkScheme"] = "stocks"
manifestPlaceholders["deepLinkHost"] = "symbol"
```

No hardcoded secrets or URLs anywhere in source code.

---

## 📋 Requirements Compliance

| Requirement | Status |
|---|---|
| 25 stock symbols | ✅ |
| WebSocket echo (`wss://ws.postman-echo.com/raw`) | ✅ |
| Updates every 2 seconds | ✅ |
| LazyColumn with symbol, price, ↑/↓ | ✅ |
| Sorted by highest price | ✅ |
| TopBar: connection indicator + Start/Stop | ✅ |
| Symbol details: title + price + description | ✅ |
| 100% Jetpack Compose | ✅ |
| MVVM/MVI architecture | ✅ |
| NavHost with 2 destinations | ✅ |
| Kotlin Flow for WebSocket stream | ✅ |
| Immutable UI state | ✅ |
| ViewModel + StateFlow + SavedStateHandle | ✅ |
| Single WebSocket (no duplicates) | ✅ |
| Price flash 1s green/red (Bonus) | ✅ |
| Deep link `stocks://symbol/{symbol}` (Bonus) | ✅ |
| Light & Dark theme (Bonus) | ✅ |
