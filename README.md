# Cyclewise — Manual Finance

[![CI / CD](https://github.com/knownassurajit/dvide/actions/workflows/ci-cd.yml/badge.svg?branch=master)](https://github.com/knownassurajit/dvide/actions/workflows/ci-cd.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
![Min SDK](https://img.shields.io/badge/minSdk-26-green)
![Target SDK](https://img.shields.io/badge/targetSdk-35-blue)

> A salary-cycle personal finance tracker for Android — built with Kotlin, Jetpack Compose, and Material Design 3 Expressive.

---

## Overview

Cyclewise models money the way salaried workers actually earn and spend: a repeating monthly window anchored to your pay day. Every pound flows through a single **waterfall**:

```
Income
  − Set aside  (Savings · Investment · Security)
  = Spendable
  − Spent      (Essentials · Lifestyle)
  = Balance   →  safe-to-spend per day remaining
```

Three dashboard layouts surface this engine — pick the one that clicks.

---

## Screenshots

| Editorial | Gauge | Cards |
|-----------|-------|-------|
| Big-number daily allowance | Ring gauge with runway fraction | Stacked hero + bucket stats |

> Design reference: `resources/claude design/` — web prototype in React / JSX

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.1 |
| UI | Jetpack Compose + Material 3 Expressive |
| Architecture | MVVM · StateFlow · Unidirectional data flow |
| DI | Hilt 2.52 |
| Database | Room 2.6 (SQLite) |
| Preferences | DataStore Preferences |
| Navigation | Navigation Compose 2.8 |
| Build | Gradle 8.10.2 · KSP · version catalog |
| CI/CD | GitHub Actions, containerized (develop → master pipeline) |
| Min SDK | API 26 (Android 8.0) |
| Target SDK | API 35 (Android 15) |

---

## Project Structure

> **Note on packaging:** the Kotlin package is `com.knownassurajit.dvide_finance.app`, but the source
> directories remain `com/dvide/cyclewise/` — Kotlin does not require the folder
> path to mirror the package declaration, so the historical folder name was kept.

```
app/
├── build.gradle.kts                ← module config, signing, version formula
├── proguard-rules.pro
├── schemas/                        ← Room exported schemas (tracked by git)
└── src/
    ├── main/
    │   ├── AndroidManifest.xml
    │   ├── kotlin/com/dvide/cyclewise/        (package = com.knownassurajit.dvide_finance.app)
    │   │   ├── CyclewiseApp.kt          ← @HiltAndroidApp
    │   │   ├── MainActivity.kt          ← Edge-to-edge entry point
    │   │   │
    │   │   ├── data/
    │   │   │   ├── local/
    │   │   │   │   ├── CyclewiseDatabase.kt   ← Room database
    │   │   │   │   ├── TransactionDao.kt
    │   │   │   │   └── Converters.kt          ← LocalDate ↔ String
    │   │   │   ├── model/
    │   │   │   │   ├── Transaction.kt         ← Room entity
    │   │   │   │   └── Category.kt            ← Enum + OKLCH colour helpers
    │   │   │   └── repository/
    │   │   │       ├── TransactionRepository.kt
    │   │   │       └── SettingsRepository.kt  ← DataStore wrapper
    │   │   │
    │   │   ├── di/
    │   │   │   ├── AppModule.kt               ← DataStore singleton
    │   │   │   └── DatabaseModule.kt          ← Room singleton
    │   │   │
    │   │   ├── domain/
    │   │   │   ├── engine/
    │   │   │   │   ├── CycleEngine.kt         ← Pure computation (port of engine.jsx)
    │   │   │   │   └── TransactionGroup.kt
    │   │   │   └── model/
    │   │   │       ├── Cycle.kt
    │   │   │       ├── DashboardVariant.kt
    │   │   │       ├── Metrics.kt
    │   │   │       ├── PastCycle.kt
    │   │   │       └── SeedPreset.kt
    │   │   │
    │   │   ├── ui/
    │   │   │   ├── MainViewModel.kt           ← Hilt ViewModel; settings + txns → Metrics
    │   │   │   ├── components/                ← Shared composables
    │   │   │   │   ├── AllocationBar.kt
    │   │   │   │   ├── CategoryChip.kt
    │   │   │   │   ├── CycleProgressBar.kt
    │   │   │   │   ├── DashHeader.kt
    │   │   │   │   ├── HueSlider.kt
    │   │   │   │   ├── Icons.kt
    │   │   │   │   ├── Keypad.kt
    │   │   │   │   ├── RingGauge.kt
    │   │   │   │   └── TransactionTimeline.kt
    │   │   │   ├── dashboard/
    │   │   │   │   ├── DashboardScreen.kt
    │   │   │   │   └── variants/
    │   │   │   │       ├── EditorialDashboard.kt
    │   │   │   │       ├── GaugeDashboard.kt
    │   │   │   │       └── CardsDashboard.kt
    │   │   │   ├── entry/
    │   │   │   │   └── AddTransactionSheet.kt
    │   │   │   ├── settings/
    │   │   │   │   └── SettingsScreen.kt
    │   │   │   ├── cycle/
    │   │   │   │   └── CycleDetailScreen.kt
    │   │   │   ├── navigation/
    │   │   │   │   └── CyclewiseNavHost.kt
    │   │   │   └── theme/
    │   │   │       ├── Color.kt               ← 4 OKLCH seed palettes
    │   │   │       ├── Type.kt                ← M3E type scale
    │   │   │       ├── Shape.kt               ← Asymmetric expressive shapes
    │   │   │       └── Theme.kt               ← CyclewiseTheme composable
    │   │   │
    │   │   └── util/
    │   │       └── FormatMoney.kt
    │   │
    │   └── res/
    │       ├── drawable/              ← ic_launcher_background / _foreground
    │       ├── font/                  ← Drop RobotoFlex-VariableFont.ttf here
    │       ├── mipmap-anydpi-v26/     ← ic_launcher / ic_launcher_round
    │       └── values/                ← strings.xml · themes.xml
    │
    ├── test/kotlin/com/dvide/cyclewise/
    │   └── CycleEngineTest.kt         ← JUnit unit tests
    └── androidTest/kotlin/com/dvide/cyclewise/
        └── ExampleInstrumentedTest.kt ← Instrumented test
```

---

## Architecture

```
┌────────────────────────────────────────────────┐
│               MainActivity                     │
│    edge-to-edge · Hilt · CyclewiseTheme        │
└─────────────────────┬──────────────────────────┘
                      │
        ┌─────────────▼─────────────┐
        │      CyclewiseNavHost     │
        │  NavHost · FAB · Sheet    │
        └──────┬────────────────────┘
               │ collectAsStateWithLifecycle
        ┌──────▼────────────────────┐
        │     MainViewModel         │  ← @HiltViewModel
        │  settings + transactions  │
        │  → CycleEngine → Metrics  │
        └─────┬──────────┬──────────┘
              │          │
  ┌───────────▼──┐ ┌─────▼──────────────┐
  │  Settings    │ │  Transaction       │
  │  Repository  │ │  Repository        │
  │  DataStore   │ │  Room / DAO        │
  └──────────────┘ └────────────────────┘
```

**Data flow:** `Room + DataStore → combine() → CycleEngine.computeMetrics() → StateFlow → Compose UI`

---

## Design System

### Colour tokens

Four OKLCH-derived M3 palettes selectable via the hue slider in Settings:

| Seed | Hue | Primary dark | Primary light |
|------|-----|-------------|---------------|
| Violet | 300 | `#D4AAFF` | `#6B3FA8` |
| Indigo | 265 | `#B8B4FF` | `#5A48B8` |
| Forest | 152 | `#72DEBF` | `#00715F` |
| Clay   |  38 | `#FFB77A` | `#8A4A00` |

The **status** role (terracotta / hue 47) signals over-spent state regardless of which seed is active.

### M3 Expressive shapes

```kotlin
// Organic asymmetric gauge card — directional tension in the corner
ShapeGaugeCard = RoundedCornerShape(topStart=40, topEnd=40, bottomEnd=40, bottomStart=16)

// Stress-morph: smooth → squared when balance is tight
ShapeGaugeCardSharp = RoundedCornerShape(16)
```

### Category colour system

| Category | Kind | Hue | Colour |
|----------|------|-----|--------|
| Savings | aside | 168 | cyan-green |
| Investment | aside | 262 | indigo-blue |
| Security | aside | 28 | amber-orange |
| Essentials | expense | *seed* | matches app accent |
| Lifestyle | expense | 75 | yellow-green |
| *Custom* | expense | `hash(name) % 360` | deterministic |

---

## Building

### Prerequisites

- Android Studio Iguana (2023.2.1) or newer
- JDK 17
- Android SDK platform 35

### First-time setup

1. **Gradle Wrapper JAR:** The wrapper JAR (`gradle/wrapper/gradle-wrapper.jar`) must be present. If it is missing, download or generate it (e.g., from Gradle's repository).
2. **Permissions:** Mark the wrapper as executable:
   ```bash
   chmod +x gradlew
   ```
3. **Android SDK Location:** Create a `local.properties` file in the project root pointing to your Android SDK:
   ```properties
   sdk.dir=/path/to/your/sdk
   # On macOS: /Users/<username>/Library/Android/sdk
   ```

### Build & install debug

```bash
./gradlew :app:assembleDebug
# Local builds produce app-debug.apk; the CI pipeline renames it to dvide-debug.apk
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Run unit tests

```bash
./gradlew :app:testDebugUnitTest
# HTML report: app/build/reports/tests/testDebugUnitTest/index.html
```

### Run lint

```bash
./gradlew :app:lintDebug
# HTML report: app/build/reports/lint-results-debug.html
```

---

## CI / CD Pipeline

Defined in a single consolidated workflow:
[`.github/workflows/ci-cd.yml`](.github/workflows/ci-cd.yml).

### Branch model

```
feature/* ──PR──► develop ──PR──► master
                    │                │
              debug pre-release  stable release
              (GitHub + APK)     (GitHub Release + Play Console)
```

- **`develop`** is the integration branch. Every push runs the full test
  suite and cuts a debug APK pre-release for testers.
- **`master`** is the production branch. Every push runs tests again, then
  builds, signs, and ships a stable release — to GitHub Releases and,
  when configured, the Play Console internal track.
- Feature work branches off `develop` and merges back via PR; PRs into
  `master` get an automated summary comment before merge.

### Pipeline diagram

```
push to develop/master, or PR into master
        │
   Test & Lint ──────────────────────────────── containerized, fail fast
   (compile → testDebugUnitTest → lintDebug)
        │
        ├── push to develop ──► Debug Pre-release ──► GitHub pre-release (APK)
        │
        ├── PR into master ───► PR Summary ──────────► step summary + sticky PR comment
        │
        └── push to master ───► Stable Release ──────► GitHub Release + Play Console
                                    ├── dvide-release.apk
                                    └── dvide-release.aab
```

### Job summary

| Job | Trigger | Steps |
|-----|---------|-------|
| `test` | push to `develop`/`master`, all PRs into `master` | `compileDebugKotlin` → `testDebugUnitTest` → `lintDebug`, all inside `eclipse-temurin:17-jdk-jammy` |
| `debug-release` | push to `develop` (needs `test`) | `assembleDebug` → rename → changelog → GitHub pre-release |
| `pr-summary` | PR into `master` | re-runs tests + lint → step summary + sticky PR comment with a changelog preview since the last stable release |
| `stable-release` | push to `master` (needs `test`) | dedicated `release/dvide/$version` branch → signed `assembleRelease`/`bundleRelease` → GitHub Release → Play Console internal track (if configured) |

All jobs run in a `eclipse-temurin:17-jdk-jammy` container; each job installs
`unzip`/`curl`/`git` via `apt-get` before setting up the Android SDK, since the
base image ships neither.

### Release workflow (push-to-master)

`stable-release` is the **only** release path — the previous tag-triggered
release job was merged into this one so a single commit can never produce two
GitHub Releases. Pushing to `master`:
1. Computes the version from the `major`/`minor`/`patch`/`build` constants in
   [`app/build.gradle.kts`](app/build.gradle.kts) and passes them as
   `-PversionName`/`-PversionCode` project properties.
2. Creates (or reuses) a dedicated `release/dvide/$version` branch for
   rollback tracking.
3. Decodes the keystore from `RELEASE_KEYSTORE_B64` and builds
   `assembleRelease`/`bundleRelease` — signed via the Gradle
   `signingConfigs["release"]` block (see Signing below); this is now the
   **only** signing mechanism (the old `-Pandroid.injected.signing.*`
   property approach was removed to avoid two competing signing paths).
4. Generates a changelog from `git log` since the previous tag.
5. Publishes a GitHub Release with the APK + AAB attached.
6. Uploads the AAB to the Play Console internal track, only when
   `PLAY_CONSOLE_JSON` is set.
7. Shreds the keystore from the runner.

### Required GitHub Secrets

Set these in **Settings → Secrets and variables → Actions**:

| Secret | Description |
|--------|-------------|
| `RELEASE_KEYSTORE_B64` | Base64-encoded `.jks` keystore file (`base64 -w0 release.jks`) |
| `STORE_FILE` | *(injected by CI, not a secret)* path to the decoded keystore |
| `STORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias inside the keystore |
| `KEY_PASSWORD` | Private key password |
| `PLAY_CONSOLE_JSON` | *(optional)* Play Console service-account JSON — Play upload is skipped when unset |

`STORE_FILE`/`STORE_PASSWORD`/`KEY_ALIAS`/`KEY_PASSWORD` are read directly by
`signingConfigs["release"]` in `app/build.gradle.kts` via `System.getenv(...)`,
mirroring the pattern used by the other `knownassurajit` Android apps
(clndr, void). If the keystore file isn't present at build time, the
`release` build type simply skips the signing config rather than failing.

---

## Package info

| Field | Value |
|---|---|
| Application ID | `com.knownassurajit.dvide_finance.app` (debug builds use `com.knownassurajit.dvide_finance.app.debug`) |
| Min SDK | 26 (Android 8.0 Oreo) |
| Target SDK | 35 (Android 15) |
| Compile SDK | 35 |
| Version name | `0.0.0.1` (four-part `major.minor.patch.build`), overridable via `-PversionName` |
| Version code | `major×1_000_000 + minor×10_000 + patch×100 + build`, overridable via `-PversionCode` |
| Build tools | AGP 8.7.3 / Kotlin 2.1.0 / KSP 2.1.0-1.0.29 |

`versionName`/`versionCode` in `app/build.gradle.kts` now read the
`-PversionName`/`-PversionCode` project properties CI passes in, falling back
to the deterministic `major.minor.patch.build` formula when they aren't
supplied (e.g. for local builds).

---

## Contributing

1. Branch off `develop`: `git checkout -b feat/your-feature develop`
2. Write or update tests alongside your changes
3. Open a PR to `develop` — the **Test & Lint** job runs automatically
4. Squash-merge once green; pushes to `develop` cut a debug pre-release
5. Periodically, `develop` is merged into `master` via a PR — the **PR Summary**
   job posts test/lint results and a changelog preview, and the push to
   `master` afterwards triggers the signed **Stable Release**

---

## Licence

[MIT](LICENSE)
