# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Clean build artifacts
./gradlew clean

# Build debug APK (dev flavor with test ad IDs)
./gradlew assembleDevDebug

# Build release APK (product flavor with production ad IDs)
./gradlew assembleProductRelease

# Install debug build to connected device (requires adb)
./gradlew installDevDebug

# Run all unit tests
./gradlew test

# Run specific test class
./gradlew test --tests="com.b096.dramarush5.ExampleUnitTest"

# Run instrumented tests on connected device
./gradlew connectedAndroidTest
```

**Windows Note:** AAPT2 workers limited to 1 in gradle.properties (`org.gradle.workers.max=1`) to prevent InvalidPathException with resource compilation.

## Architecture Overview

Android IPTV streaming application using **MVVM** with Kotlin. Two modules: `app` (main application) and `base` (reusable library, namespace `com.dong.baselib`).

### Tech Stack
- **Namespace:** `com.b096.dramarush5`
- **Min SDK:** 26 (Android 8.0) / **Target SDK:** 36
- **Language:** Kotlin 2.1.0 with JVM target 17, KSP for annotation processing
- **DI:** Koin 4.1.1 (declarative modules in `app/app/IModule.kt`)
- **Database:** Room 2.6.1 with destructive fallback migration, DB name `iptv_database`
- **Async:** Kotlin Coroutines + Flow with lifecycle-aware collection
- **Media:** Media3 ExoPlayer 1.9.0 with HLS support, PiP, resume playback
- **View Binding:** Enabled throughout (View-based, not Compose)
- **Ads:** Azura Global ads module (`azmoduleads`) with AdMob + Facebook/Pangle/Mintegral mediation
- **Analytics:** Firebase BOM 33.13.0 (Analytics, Crashlytics, Remote Config, Messaging, Firestore)
- **Build:** AGP 8.8.2, Triple Play 3.13.0 (Play Console publishing)

### Module Structure

**`app` module** — Main application (IPTV player)
- Activities, Fragments, ViewModels under `com.b096.dramarush5`
- Two flavors: `dev` (test ad IDs, `build_debug=true`) and `product` (production)
- Ads wrappers in `ads/` (banner, interstitial, native, reward, splash)

**`base` module** — Reusable base classes and utilities (namespace `com.dong.baselib`)
```
base/src/main/java/com/dong/baselib/
├── base/        # BaseActivity, BaseFragment, BaseAdapter, BaseDialog, BaseBottomSheet
├── extensions/  # Kotlin extensions (View, String, etc.)
├── api/         # HTTP client utilities
├── lifecycle/   # Coroutine lifecycle helpers (launchIO, etc.)
├── network/     # Network utilities
├── canvas/      # Custom drawing views (CanvasDrawingView, AnimatedCanvasView)
├── file/        # File I/O utilities
├── utils/       # Generic utilities
└── widget/      # Custom views (flexbox, layout, navigation, popup)
```

### Data Layer

```
app/src/main/java/com/b096/dramarush5/data/
├── local/
│   ├── AppDatabase.kt       # Room DB v2, singleton, destructive fallback
│   ├── entity/
│   │   ├── PlaylistEntity   # source type: URL(0), FILE(1), GALLERY(2)
│   │   ├── ChannelEntity    # FK to Playlist (CASCADE), indexed on playlist_id/is_favorite/group_name
│   │   └── SourceType       # Enum: URL, FILE, GALLERY
│   └── dao/
│       ├── ChannelDao       # Insert REPLACE, Flow + suspend queries
│       └── PlaylistDao      # Flow + suspend queries
└── repository/
    └── AppRepository        # Single source of truth, exposes Flow-based reactive queries
```

**SharedPreference** (`app/app/SharePreference.kt`) — Property delegates (`boolean()`, `string()`, `int()`, `long()`, `float()`, `enum()`, `json()`) with LiveData and Flow observable support.

### Dependency Injection (Koin)

Modules defined in `app/app/IModule.kt`:

```kotlin
viewModelModule: OnboardingViewModel, GuideViewModel, HomeViewModel,
                 PlayViewModel, AddItemViewModel, ChannelViewModel

dataModule:      SharedPreference (singleOf)

databaseModule:  AppDatabase (singleton), ChannelDao, PlaylistDao, AppRepository
```

ViewModels injected in Activities/Fragments via `by viewModel()` delegation.

### Navigation Structure

**Entry:** `SplashActivity` → `NativeSplashActivity` → `MainActivity`

**MainActivity** — NavHostFragment with 4 bottom tabs (defined in `main_nav_graph.xml`):
- HomeFragment, ChannelFragment, FavoriteFragment, HistoryFragment
- All under `ui/main/home/`

**Standalone Activities:**
- `PlayChannelActivity` / `ChannelHorActivity` — Portrait/landscape video players with PiP
- `ChannelActivity` — Channel browser/selector
- `AddItemActivity` — Playlist import (3 fragments via `add_nav_graph.xml`: URL, File, Gallery)
- `SettingActivity`, `HowToActivity`, `IAPActivity`

**PlayerManager** (`ui/main/play/PlayerManager.kt`) — Centralized ExoPlayer instance, HLS streaming, resume playback, shared between portrait/landscape players.

### Build Flavors

| Flavor | App ID | Ad IDs | `build_debug` |
|--------|--------|--------|---------------|
| `dev` | `com.b096.dramarush5` | Google test IDs | `true` |
| `product` | `com.iptv.livetv.smartersplayerlite` | Production | `false` |

Build variant: `[flavor][BuildType]` → `assembleDevDebug`, `assembleProductRelease`

### Application Initialization

**ProjectApplication** (`app/app/ProjectApplication.kt`, extends `AdsMultiDexApplication`):

1. `LocateManager.initDeviceLocate()` — Device location setup
2. `FirebaseApp.initializeApp()` — Firebase services
3. Koin startup — `modules(dataModule, databaseModule, viewModelModule)`
4. `registerLifecycleCallback()` — Activity + ProcessLifecycleOwner tracking
5. `HeadUpNotification.createHeadUpNotificationChannel()` — Notification channels
6. `initAds()` — AzAds with Adjust, AppsFlyer, Taichi configs

**Global state** (Companion object): `isAppForeground`, `isMainActivityActive`, `isFirstOpenMainActivity`

### Notification System

Two-tier strategy (release builds only, when `!BuildConfig.build_debug`):
1. **Foreground** — Triggers on first MainActivity open via `HeadUpNotification.onShowHomeOpen()`
2. **Background** — Triggers on `ProcessLifecycleOwner.onStop`, cleared on `onStart`

### Common Development Workflows

**Adding a new screen:**
1. Create `FooViewModel` with StateFlow state
2. Register in `IModule.kt` under `viewModelModule`
3. Create Activity or Fragment to observe StateFlow
4. Add to AndroidManifest (Activity) or nav graph (Fragment)

**Modifying database schema:**
1. Edit entity in `data/local/entity/`
2. Update corresponding DAO methods
3. Increment database version in `AppDatabase` — uses destructive fallback

**Adding a new dependency:**
1. Add to `gradle/libs.versions.toml` under `[versions]` and `[libraries]`
2. Reference in `app/build.gradle.kts` or `base/build.gradle.kts`

### Key Configuration Files

- `gradle/libs.versions.toml` — Centralized dependency versions
- `gradle.properties` — JVM args (`-Xmx4096m`), Windows AAPT2 workaround, AndroidX config
- `app/proguard-rules.pro` — R8 rules for release (Gson, Firebase, GMS, Retrofit, Guava)
- `base/proguard-rules.pro` — Library R8 rules (Gson, Kotlin Metadata, Guava)

**ProGuard note:** Release builds enable `isMinifyEnabled` + `isShrinkResources`. Model classes used by Firestore, Gson, or Remote Config need keep rules. Current rules reference stale package `com.as069.iptv.model` — should be updated to `com.b096.dramarush5.model` if Firestore models exist.

### CI/CD (Jenkinsfile)

- Agent: `store-vohathi` (Windows)
- Branch `staging` → builds APK; branch `master` → builds AAB for Play Console
- Stages: Checkout → Credentials → Build → Publish (Triple Play) → Download APK → Upload to Nexus → Discord notification
- Artifacts stored at Nexus (`nexus.azuraglobal.vn`)

### Code Style

- Kotlin official style (`kotlin.code.style=official`)
- View Binding throughout (no `findViewById`)
- `lifecycleScope.launch()` in Activities, `viewModelScope.launch()` in ViewModels
- `Dispatchers.IO` for Room/file operations

### Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| AAPT2 InvalidPathException on Windows | Already set: `org.gradle.workers.max=1` |
| Room migration errors | Check entity annotations, increment DB version (destructive fallback) |
| Koin injection fails | Verify in `IModule.kt`, ensure Koin started in `ProjectApplication.onCreate()` |
| ProGuard strips model fields | Add `-keep` rule for model classes used with Gson/Firestore/Remote Config |
