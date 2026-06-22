# Cyclewise — Manual Finance

[![CI / CD](https://github.com/justachillgirl/dvide/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/justachillgirl/dvide/actions/workflows/ci.yml)
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
| CI/CD | GitHub Actions (5-stage pipeline) |
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
# Local builds produce app-debug.apk; the CI pipeline renames it to cyclewise-debug.apk
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

Defined in [`.github/workflows/ci.yml`](.github/workflows/ci.yml).

### Pipeline diagram

```
push / PR to master
        │
   ①  Compile ──────────────────────────────────── fail fast
        │
   ②  Unit Tests ◄──────────────────────── parallel
   ③  Lint       ◄──────────────────────── parallel
        │
       both pass
        │
   ─── on push to master ─── ④ Debug APK ────── artifact (7 days)
        │
   ─── on tag v*.*.* ──────── ⑤ Release ─────── GitHub Release
                                  ├── cyclewise-release.apk
                                  └── cyclewise-release.aab
```

### Job summary

| # | Job | Trigger | Steps |
|---|-----|---------|-------|
| ① | Compile | PR + push | Checkout → JDK → wrapper validation → `compileDebugKotlin` |
| ② | Unit Tests | after ① | `testDebugUnitTest` → upload JUnit XML + HTML → annotate PR |
| ③ | Lint | after ① | `lintDebug` → upload HTML + XML |
| ④ | Debug APK | push to master | `assembleDebug` → rename → upload artefact |
| ⑤ | Release | tag `v*.*.*` | decode keystore → `bundleRelease` → `assembleRelease` → verify sig → changelog → GitHub Release |

### Release workflow (tag-triggered)

1. Push a version tag to master:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
   > The tag must match `v[0-9]+.[0-9]+.[0-9]+` exactly — only this pattern
   > triggers the release job.
2. The **Release** job automatically:
   - Extracts the version from the tag and computes a workflow-side
     `versionCode` as `major×10000 + minor×100 + patch` (`1.0.0` → `10000`),
     passing both as project properties *(see the versioning caveat above)*
   - Decodes the keystore from `RELEASE_KEYSTORE_B64`
   - Builds and signs the AAB (`bundleRelease`) and APK (`assembleRelease`)
   - Verifies the APK signature with `apksigner`
   - Generates a changelog from `git log` since the previous tag
   - Creates a GitHub Release with the APK + AAB attached
   - Shreds the keystore from the runner

### Required GitHub Secrets

Set these in **Settings → Secrets and variables → Actions**:

| Secret | Description |
|--------|-------------|
| `RELEASE_KEYSTORE_B64` | Base64-encoded `.jks` keystore file (`base64 -w0 release.jks`) |
| `KEY_ALIAS` | Key alias inside the keystore |
| `KEY_PASSWORD` | Private key password |
| `STORE_PASSWORD` | Keystore password |

---

## Package info

| Field | Value |
|---|---|
| Application ID | `com.knownassurajit.dvide_finance.app` (debug builds use `com.knownassurajit.dvide_finance.app.debug`) |
| Min SDK | 26 (Android 8.0 Oreo) |
| Target SDK | 35 (Android 15) |
| Compile SDK | 35 |
| Version name | `0.0.0.2` (four-part `major.minor.patch.build`) |
| Version code | `major×1_000_000 + minor×10_000 + patch×100 + build` |
| Build tools | AGP 8.7.3 / Kotlin 2.1.0 / KSP 2.1.0-1.0.29 |

> **Versioning:** `versionName` / `versionCode` are currently derived from
> hardcoded `major`/`minor`/`patch`/`build` constants in
> [`app/build.gradle.kts`](app/build.gradle.kts). The release job *extracts* a
> version from the git tag and passes `-PversionName` / `-PversionCode`, but the
> build script does not yet read those project properties — wire them in if you
> want tag-driven versioning to take effect.

---

## Contributing

1. Fork and create a branch: `git checkout -b feat/your-feature`
2. Write or update tests alongside your changes
3. Open a PR to `master` — the **① Compile → ② Tests + ③ Lint** pipeline runs automatically
4. Squash-merge once green

---

## Licence

[MIT](LICENSE)
