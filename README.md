# GiphyTask — Modern GIF Search Android Application

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg)
![Compose](https://img.shields.io/badge/Jetpack_Compose-Material3-blue.svg)
![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2B%20MVVM-green.svg)
![DI](https://img.shields.io/badge/DI-Hilt-gold.svg)
![Database](https://img.shields.io/badge/Database-Room-blue.svg)

An Android application built on the official [Giphy API](https://developers.giphy.com/docs/api/).
GiphyTask follows modern Android architecture guidance: Clean Architecture layering, a declarative
Jetpack Compose + Material 3 UI, Room for local storage, animated GIF playback and caching via Coil,
reactive state with Kotlin Flow, offset-based pagination with Paging 3, and unit tests written with
JUnit 4, MockK and Turbine.

---

## 🌟 Features

- **⚡ Debounced automatic search**: typing triggers a search 400 ms after you stop, and
  `flatMapLatest` cancels the in-flight request. The initial trending load and the "clear" action
  are not debounced, so the first screen paint is immediate.
- **🔥 Trending GIFs**: Giphy's trending feed is shown whenever the query is blank.
- **📜 Infinite scroll pagination**: Paging 3 with load-state footers for the append spinner and a
  retry button.
- **🎬 Animated GIFs**: Coil decodes animated GIF/WebP through the platform `ImageDecoder`
  (`ImageDecoderDecoder`, API 28+) and falls back to `GifDecoder` on API 24–27, with a 25 %-of-heap
  memory cache and a 100 MB disk cache.
- **📱 Adaptive grid**: `LazyVerticalGrid(GridCells.Adaptive(160.dp))` reflows across portrait,
  landscape and tablet widths.
- **💾 Room search history**: the 10 most recent searches are persisted
  (`AppDatabase`, `RecentSearchEntity`, `RecentSearchDao`). Entries are de-duplicated
  case-insensitively, older rows are trimmed automatically, and history can be cleared either
  entirely or one entry at a time.
- **🌐 Connectivity monitoring**: `ConnectivityManager` callbacks drive an offline banner in
  real time.
- **🎨 Material 3 theming**: light and dark schemes built from the Giphy brand palette, with
  shimmer skeleton loaders while the first page loads. Material You dynamic colour is available via
  `GiphyTaskTheme(dynamicColor = true)` but is **off by default** so the brand palette is preserved.
- **ℹ️ Detail screen**: full-size animated playback plus creator metadata, verified badge,
  dimensions, content rating, import/trending dates, native sharing and an external browser launch.

---

## 🏛️ Architecture

A three-tier **Clean Architecture** with **MVVM** in the presentation layer:

```
                          +-------------------------+
                          |   Presentation Layer    |
                          | (Compose UI, ViewModel) |
                          +-------------------------+
                                       |
                                       v
                          +-------------------------+
                          |      Domain Layer       |
                          | (UseCases, Models, Repo)|
                          +-------------------------+
                                       |
                                       v
                          +-------------------------+
                          |       Data Layer        |
                          | (Retrofit, Room, DTO)   |
                          +-------------------------+
```

### Layer responsibilities

- **Presentation (`feature/search`, `feature/details`)** — stateless composables driven by a single
  immutable `UiState`, ViewModels that consume use cases, and unidirectional data flow
  (`UiState` / `UiEvent`, plus `UiEffect` for one-shot navigation and intent launches on the
  detail screen).
- **Domain (`domain`)** — pure Kotlin: entities (`Gif`, `GifImages`, `RecentSearch`), repository
  contracts, and single-responsibility use cases.
- **Data (`data`)** — Retrofit + Kotlinx Serialization remote API, an offset-based `PagingSource`,
  Room persistence, and DTO → domain mappers that absorb Giphy's inconsistent payloads
  (missing image variants, blank usernames, `"0000-00-00"` placeholder dates).
- **Core (`core`)** — the `AppError` taxonomy and `Result` wrapper, coroutine dispatcher
  abstraction, network monitor, API-key interceptor, theme tokens, and reusable design-system
  components.

Errors from any source (`IOException`, `HttpException`, timeouts, HTTP 429 rate limits) are
normalised into `AppError` by `Throwable.toAppError()`, so the UI never handles transport types.

---

## 📁 Package structure

```
com.nursulton.giphytask
├── GiphyApplication.kt      # Hilt entry point + Coil ImageLoader configuration
├── MainActivity.kt
│
├── core
│   ├── common               # AppError, Result, DispatcherProvider
│   ├── designsystem
│   │   ├── components       # GifCard, SearchBar, ErrorCard, ShimmerGrid, EmptyState, OfflineBanner
│   │   └── theme            # Color, Theme, Type
│   └── network              # NetworkMonitor, ApiKeyInterceptor, ErrorMapper
│
├── data
│   ├── local                # AppDatabase, RecentSearchDao, RecentSearchEntity, LocalDataSource
│   ├── mapper               # GifMapper (DTO -> domain)
│   ├── remote
│   │   ├── api              # GiphyApi Retrofit service
│   │   ├── dto              # Kotlinx @Serializable DTOs
│   │   └── paging           # GiphyPagingSource (Paging 3, offset based)
│   └── repository           # GiphyRepositoryImpl, RecentSearchRepositoryImpl
│
├── domain
│   ├── model                # Gif, GifImages, RecentSearch
│   ├── repository           # GiphyRepository, RecentSearchRepository
│   └── usecase              # SearchGifs, GetGifDetails, Get/Save/Remove/ClearRecentSearches
│
├── feature
│   ├── search               # SearchUiState, SearchViewModel, SearchScreen
│   └── details              # DetailsUiState, DetailsViewModel, DetailsScreen
│
├── navigation               # GiphyNavHost, Screen routes
└── di                       # Hilt modules (Network, Repository, Database, Dispatcher)
```

---

## 🛠️ Tech stack

- **Language**: Kotlin 2.0.21 (JDK 17 target)
- **UI**: Jetpack Compose (BOM 2024.11.00) + Material 3
- **Local storage**: Room 2.6.1 (`room-runtime`, `room-ktx`, `room-compiler` via KSP)
- **Dependency injection**: Hilt 2.51.1
- **Networking**: Retrofit 2.11.0, OkHttp 4.12.0, Kotlinx Serialization JSON 1.7.3
- **Async**: Coroutines 1.9.0, Flow / StateFlow
- **Images**: Coil 2.7.0 (`coil-compose`, `coil-gif`)
- **Pagination**: Paging 3.3.4 (`paging-runtime`, `paging-compose`)
- **Navigation**: Navigation Compose 2.8.4
- **Logging**: Timber 5.0.1
- **Testing**: JUnit 4.13.2, MockK 1.13.13, Turbine 1.2.0, `kotlinx-coroutines-test` 1.9.0,
  Compose UI test (instrumented)

---

## 🔑 API key setup

1. Get a key from the [Giphy Developer Portal](https://developers.giphy.com/).
2. Add it to `local.properties` in the project root (this file is git-ignored):

```properties
GIPHY_API_KEY=your_giphy_api_key_here
```

> **Do not wrap the value in quotes.** `local.properties` is a Java properties file, so
> `GIPHY_API_KEY="abc"` would make the key literally `"abc"` and every request would fail with
> HTTP 401. The build script strips surrounding quotes defensively, but unquoted is correct.

Alternatively, export `GIPHY_API_KEY` as an environment variable (useful in CI). Resolution order
is `local.properties` → environment variable → a public Giphy beta key that is checked in as a
fallback so a fresh clone runs without setup.

> **Note:** that fallback key is committed in `app/build.gradle.kts` purely for convenience. It is
> rate-limited and shared, so replace it with your own key for anything beyond a quick trial. In a
> real product the key belongs in a secrets manager, and ideally behind your own backend rather
> than in the client at all.

---

## 🚀 Build & run

### Prerequisites

- Android Studio Ladybug (2024.2+) or newer
- JDK 17
- Android SDK Platform 35

### Command line

```bash
# Clone
git clone https://github.com/nursulton/GiphyTask.git
cd GiphyTask

# Unit tests (JVM, no device needed)
./gradlew test

# Instrumented UI tests (requires a connected device or emulator)
./gradlew connectedDebugAndroidTest

# Debug APK
./gradlew assembleDebug

# Release APK with R8 minification and resource shrinking
./gradlew assembleRelease
```

`assembleRelease` produces an **unsigned** APK — add a `signingConfig` before distributing it.

---

## 🧪 Tests

Unit tests (`app/src/test`):

| Test | Covers |
|---|---|
| `GifMapperTest` | DTO → domain mapping, blank/placeholder field fallbacks |
| `GiphyPagingSourceTest` | full page vs. short page (end of results), trending vs. search endpoint, error mapping |
| `GiphyRepositoryTest` | detail success path and `NotFound` when the API returns no data |
| `SearchGifsUseCaseTest` | blank query routes to trending, non-blank query is trimmed |
| `SearchViewModelTest` | query/mode transitions, history save / remove / clear, offline state |
| `DetailsViewModelTest` | load success, error surfacing, missing nav argument, share effect |

Instrumented tests (`app/src/androidTest`):

| Test | Covers |
|---|---|
| `GifCardTest` | card renders its accessibility label and forwards clicks |

---

## 📌 Known limitations / next steps

- **UI strings are hardcoded** in the composables rather than extracted to `strings.xml`. This is
  the first thing to change before localising the app.
- **No `MediatorRemoteKey` offline cache for GIFs** — only the search history is persisted, so the
  grid needs a connection. A Paging `RemoteMediator` backed by Room would fix this.
- **No signing config** for release builds.
- Static analysis (detekt/ktlint) is not wired into the Gradle build.
