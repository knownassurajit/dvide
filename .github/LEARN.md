# dv/de — Architecture & Development Knowledge Base

This document serves as an onboarding guide and architectural blueprint for **dv/de** (formerly Cyclewise). Read this before modifying the codebase or running build pipelines.

---

## 1. Core Concept & Branding
- **Name:** The application name is creatively styled as `"dv/de"` in user-facing components (headers, settings footer, etc.), but references to the app in class names and folder structures use `dvide`.
- **Application ID:** `com.dvide.app` (debug suffix `.debug`).
- **Directory Package:** `app/src/main/kotlin/com/dvide/dvide/` contains all Kotlin source files.
- **Workflow / Design:** Models salary-based budget waterfall matching a repeating cycle anchored to a pay day (Anchor Day).

---

## 2. Architecture & Data Flow

```
   ┌────────────────────────────────────────────────┐
   │               MainActivity                     │
   │       edge-to-edge · Hilt · DvideTheme         │
   └─────────────────────┬──────────────────────────┘
                         │
           ┌─────────────▼─────────────┐
           │        DvideNavHost       │
           │    NavHost & BottomSheet  │
           └─────────────┬─────────────┘
                         │ state flows
           ┌─────────────▼─────────────┐
           │        MainViewModel      │  ← @HiltViewModel
           │  settings + transactions  │
           │  → CycleEngine → Metrics  │
           └─────┬──────────┬──────────┘
                 │          │
     ┌───────────▼──┐ ┌─────▼──────────────┐
     │  Settings    │ │  Transaction       │
     │  Repository  │ │  Repository        │
     │  DataStore   │ │  Room DB / DAO     │
     └──────────────┘ └────────────────────┘
```

- **UDF Pattern:** UI elements only state-observe and dispatch actions to the `MainViewModel`.
- **DvideDatabase:** A local Room database (`dvide.db`) storing a single `Transaction` table.
- **SettingsRepository:** Encapsulates Jetpack DataStore Preferences for user income, anchor day, selected app color theme (seed hue), dark mode, dashboard variant, and user profile information (`userName`, `userEmail`).

---

## 3. UI Styling & Component Design
- **Color Palettes:** Generated dynamically based on the stored `seedHue` (OKLCH mapping to standard RGB space) in `Theme.kt`.
- **Custom Theme Tokens:** Augmented via `DvideExtraColors` provided by the custom `LocalDvideColors` composition local. Always access status, categories, and category soft colors via `MaterialTheme.dvideColors.categoryColor(cat)`.
- **Asymmetric Shapes:** Rounded corners are asymmetrical (e.g. `ShapeGaugeCard`) to convey directional tension, morphing to standard round cards if the user budget is tight (`metrics.tight == true`).

---

## 4. Key Workflows & Gestures

### Daily / Weekly Spend Toggle
- The daily/weekly view toggle is implemented as a **long click (tap-and-hold)** gesture on the main hero balance card across all three dashboard variants:
  - `EditorialDashboard.kt`
  - `GaugeDashboard.kt`
  - `CardsDashboard.kt`
- Long click calls `onViewChange(!viewIsWeekly)`. When weekly view is active, the UI:
  1. Multiplies the daily allowance `metrics.safeToSpend` by `7.0` to show the weekly limit.
  2. Updates UI labels (e.g. from `"SAFE / DAY"` to `"SAFE / WEEK"`).
  3. Passes `groupByWeek = true` to the `TransactionTimeline` component.

### Profile Setup Details
- Clicking on the user's name on the dashboard header redirects the user to the **Personal details** screen (`ProfileScreen.kt`).
- The settings screen starts directly with the Display options block, with its profile summary card removed. Clicking "Personal details" in Settings also redirects to the profile setup screen.
- Changes to Name and Email are immediately persisted back to preferences via `viewModel.updateProfile(name, email)`.

### Transaction Deletion
- Long-pressing a transaction row in `TransactionTimeline.kt` prompts the user with a confirmation alert dialog. Confirming deletion dispatches `deleteTransaction(tx)` to the database, automatically updating all computed metrics.

---

## 5. Development & Verification Guide

### Build Requirements
- **JDK:** Java 21 (Microsoft OpenJDK or compatible). The vendor constraint in `gradle/gradle-daemon-jvm.properties` is removed to enable local compatibility.
- **SDK Path:** Configured in `local.properties` (typically pointing to `~/Library/Android/sdk` on macOS).

### CLI Command Reference
Use the standard Gradle wrapper `gradlew` for local compilation:
- **Compile:** `./gradlew compileDebugKotlin`
- **Build debug APK:** `./gradlew assembleDebug`
- **Run unit tests:** `./gradlew :app:testDebugUnitTest`

### Emulator & Device Management via `android-cli`
If the custom `android-cli` plugin is active:
- List virtual devices: `android emulator list`
- Launch emulator: `android emulator start <AVD_NAME>`
- Install and run APK: `android run --apks=app/build/outputs/apk/debug/app-debug.apk`
- Retrieve layout hierarchy: `android layout -p`
- Capture screen: `android screen capture -o=output.png`

---

## 6. Guidelines for Maintenance
- **Kotlin Package Declaration:** Source Kotlin files must declare `package com.dvide.app` or sub-packages (e.g., `com.dvide.app.ui.navigation`), while folders reside under `com/dvide/dvide/`.
- **Do Not Use Plain Colors:** Always resolve colors through `MaterialTheme.colorScheme` or `MaterialTheme.dvideColors` to respect user color scheme changes.
- **Maintain Test Coverage:** Run `:app:testDebugUnitTest` after modifying `CycleEngine.kt` or `MainViewModel.kt` to ensure budget math is correct.
