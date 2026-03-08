# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
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

# Clean build artifacts
./gradlew clean
```

**Windows Note:** AAPT2 workers limited to 1 in gradle.properties (`org.gradle.workers.max=1`) to prevent InvalidPathException with resource compilation.

## Architecture Overview

Android base project template using **MVVM** with Kotlin. Two modules: `app` (main application) and `lib` (reusable library, namespace `com.dong.baselib`).

### Tech Stack
- **Namespace:** `com.b096.dramarush5`
- **Min SDK:** 26 (Android 8.0) / **Target SDK:** 36
- **Language:** Kotlin 2.1.0 with JVM target 17, KSP for annotation processing
- **DI:** Koin 4.1.1 (modules in `app/app/IModule.kt`)
- **Async:** Kotlin Coroutines + Flow with lifecycle-aware collection
- **View Binding:** Enabled throughout (View-based, not Compose)
- **Ads:** Azura Global ads module (`azmoduleads`) with AdMob + Facebook/Pangle/Mintegral mediation
- **Analytics:** Firebase BOM 33.13.0 (Analytics, Crashlytics, Remote Config, Messaging, Firestore)
- **Build:** AGP 8.8.2

### Build Flavors

| Flavor | App ID | Ad IDs | `build_debug` |
|--------|--------|--------|---------------|
| `dev` | `com.b096.dramarush5` | Google test IDs | `true` |
| `product` | `com.iptv.livetv.smartersplayerlite` | Production | `false` |

### Navigation Flow

**First launch:** `SplashActivity` → `NativeSplashActivity` → `Language1Activity` → `Language2Activity` → `LanguageOpenActivity` → `LanguageWaitingActivity` → `LangApplyActivity` → `OnboardingActivity` → `FeatureActivity` → `MainActivity`

**Return launch:** `SplashActivity` → `MainActivity` (gated by `PreferenceData.isFinishFirstFlow`)

`SplashActivity` runs in parallel: UMP consent + Firebase Remote Config fetch (30s timeout), then initializes ads before navigating.

`MainActivity` uses NavHostFragment; bottom nav visibility is gated per destination (currently only `homeFragment` shows it).

### Dependency Injection (Koin)

Modules in `app/app/IModule.kt`:
- `viewModelModule`: `OnboardingViewModel`
- `dataModule`: `SharedPreference` (singleton)
- `databaseModule`: empty (add Room here when needed)

### State & Preferences

**`SharedPreference`** (`app/app/SharePreference.kt`) — Property delegates (`boolean()`, `string()`, `int()`, `long()`, `float()`, `enum()`, `json()`) with LiveData and Flow observable support via `observable()`.

**`PreferenceData`** (`app/app/PreferenceData.kt`) — KoinComponent singleton exposing app-wide state: `isFinishFirstFlow`, `languageCode`, session counters. Use this for cross-screen state.

**`RemoteConfig`** (`app/app/RemoteConfig.kt`) — Wraps Firebase Remote Config; all values are persisted to `SharedPreference` for offline access. Access via the top-level `remoteConfig` property.

### Ads Architecture

All ad configs are fetched from Firebase Remote Config and cached in SharedPreference. Remote Config keys follow a naming convention:

| Prefix | Type |
|--------|------|
| `A101` | App Open |
| `N101`–`N112` | Native ads (splash, language, onboarding, feature, home, popup) |
| `I101`–`I103` | Interstitial (splash, in-app, paywall) |
| `B101` | Banner |
| `R101` | Rewarded |

Key ad managers in `ads/wrapper/`:
- `AdSplashManager` / `NativeSplashManager` — Splash screen ads
- `InterstitialAdManager` — Interstitial ads between screens; `loadInterAll()` called in `MainActivity.initialize()`
- `NativeAdPreloadManager` — Preloads native ads during splash for smooth transitions
- `BannerAdWrapper` / `BannerAdView` — Banner ads with auto-reload
- `RewardAdManager` — Rewarded ads

Ads are disabled when `!remoteConfig.adEnable || !isInternetAvailable() || !isAcceptUmp`.

### Base Classes

**`BaseActivity<VB>`** (lib) — Foundation with View Binding, back press handling, activity launch helpers (`launchActivity<T>()`, `launcherForResult<T>()`).

**`BaseAppActivity<VB>`** (app, extends BaseActivity) — Adds network connectivity monitoring with `DialogNoInternet`. Use `internetClick {}` / `internetSingleClick {}` / `runWithInternet {}` for network-gated actions.

**`BaseFragment<VB>`** (lib) — Fragment with View Binding.

**`BaseAppFragment<VB>`** (app, extends BaseFragment) — App-level fragment base.

### App Utilities (`app/app/Aso.kt`)

Holds app-level constants (`POLICY_LINK`, `TEAM_SERVICE`, `MAIL_SUPPORT`) and Context/Activity/View extension functions: `openUrl()`, `toastShort/Long/Top()`, `showTopSnackbar()`, `isInternetAvailable()`, `rateApp()`, `sendFeedBack()`, `shareApp()`, `shareValue()`.

### Analytics (`firebase/Analytics.kt`)

Thin wrapper around Firebase Analytics: `Analytics.track(event)` / `Analytics.track(event, bundle)`. Use this for all event logging rather than calling Firebase directly.

### Notification System

Two-tier strategy (release builds only, when `!BuildConfig.build_debug`):
1. **Foreground** — Triggered on first `MainActivity` resume via `HeadUpNotification`
2. **Background** — Triggered via `ProcessLifecycleOwner.onStop` when `isMainActivityActive`; cleared on `onStart`

Lock screen reminders scheduled via `ReminderUtils` after notification permission granted.

### Application Initialization

**`ProjectApplication`** (extends `AdsMultiDexApplication`):
1. `LocateManager.initDeviceLocate()`
2. `FirebaseApp.initializeApp()`
3. Koin startup with `dataModule`, `databaseModule`, `viewModelModule`
4. `registerLifecycleCallback()` — tracks foreground state + notification triggers
5. `HeadUpNotification.createHeadUpNotificationChannel()`
6. `initAds()` — AzAds with Adjust + Taichi configs

**Global state** (companion): `isAppForeground`, `isMainActivityActive`, `isFirstOpenMainActivity`

### lib Module Structure

```
lib/src/main/java/com/dong/baselib/
├── base/        # BaseActivity, BaseFragment, BaseAdapter, BaseDialog, BaseBottomSheet
├── extensions/  # Kotlin extensions (View, Animation, Click, Glide, Text, etc.)
├── api/         # HTTP client utilities
├── lifecycle/   # Coroutine lifecycle helpers (launchIO, etc.)
├── network/     # Network connectivity observer
├── canvas/      # Custom drawing views
├── file/        # File I/O, Bitmap, URI, clipboard utilities
├── utils/       # Color, Dimen, Size, TextStyle, ViewUtils, PermissionUtils
└── widget/      # Custom views (layouts, UiTextView, UiImageView, RatingBar, etc.)
```

### Common Development Workflows

**Adding a new screen:**
1. Create `FooViewModel` with StateFlow state, register in `IModule.kt` under `viewModelModule`
2. Create Activity extending `BaseAppActivity<VB>` or Fragment extending `BaseAppFragment<VB>`
3. Add Activity to AndroidManifest or Fragment to nav graph

**Adding Room database:**
1. Create entity in `data/local/entity/`
2. Create DAO and `AppDatabase` (singleton with KSP)
3. Register in `databaseModule` in `IModule.kt`
4. Use `Dispatchers.IO` for all DB operations

**Adding a new dependency:**
1. Add to `gradle/libs.versions.toml` under `[versions]` and `[libraries]`
2. Reference in `app/build.gradle.kts` or `lib/build.gradle.kts`

### Key Configuration Files

- `gradle/libs.versions.toml` — Centralized dependency versions
- `gradle.properties` — JVM args (`-Xmx4096m`), Windows AAPT2 workaround
- `app/proguard-rules.pro` — R8 rules for release (Gson, Firebase, GMS, Retrofit, Guava)
- `lib/proguard-rules.pro` — Library R8 rules

**ProGuard note:** Release builds enable `isMinifyEnabled` + `isShrinkResources`. Model classes used by Firestore, Gson, or Remote Config need keep rules.

### CI/CD (Jenkinsfile)

- Agent: `store-vohathi` (Windows)
- Branch `staging` → builds APK; branch `master` → builds AAB for Play Console
- Stages: Checkout → Credentials → Build → Publish (Triple Play) → Download APK → Upload to Nexus → Discord notification