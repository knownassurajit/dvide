# PROJECT_CONTEXT.md — DVIDE (Cyclewise)

Latest verified technical state for coding agents. Read this first, then validate against source before
relying on it. Keep it factual: no transcripts, no secrets.

## A. Project Overview

- **Purpose:** offline, manual salary-cycle finance tracker. Income → set-aside → spendable → spent → daily balance
  ("waterfall"), computed per pay cycle.
- **Stack:** Kotlin 2.1.21, Jetpack Compose (BOM 2025.05.01), Material 3 1.4.0, Hilt 2.56, Room 2.7.1 (schema v4),
  DataStore Preferences, Navigation Compose 2.9. AGP 8.9.2, Gradle 8.11.1, compile/target SDK 36, min SDK 26, JVM 17.
- **Package / applicationId:** `com.knownassurajit.dvide_finance.app` (debug suffix `.debug`).
- **Privacy posture (constraint):** `PRIVACY_POLICY.md` promises 100% on-device storage, no network, **no `INTERNET`
  permission** in release (only `src/debug/AndroidManifest.xml` adds it for adb-reverse logging). Any feature that
  needs network (Google sign-in, cloud backup) contradicts that policy and needs an explicit product decision.
- **Verify command (from `AGENTS.md`):** `./gradlew assembleDebug test lintDebug`

## B. Architecture and Code Organization

Single Gradle module `:app`; MVVM/UDF with one shared ViewModel.

| Concern | Location (under `app/src/main/kotlin/com/knownassurajit/dvide_finance/app/`) |
|---|---|
| Entry | `MainActivity.kt` (edge-to-edge, `DvideTheme` → `DvideNavHost`), `DvideApp.kt` (`@HiltAndroidApp`) |
| State holder | `ui/MainViewModel.kt` — exposes `settings`, `cycles`, `transactions`, `currentCycle`, `metrics`, `pastCycles` as `StateFlow` |
| Navigation | `ui/navigation/DvideNavHost.kt` — routes `home`, `settings`, `profile`, `archive`, `cycle/{cycleId}`; FAB + bottom sheets live here |
| Domain | `domain/engine/CycleEngine.kt` (pure, unit-tested), `domain/model/*` (`Cycle`, `Metrics`, `PastCycle`, enums) |
| Data | `data/local/*` (Room `DvideDatabase`, DAOs, `Converters`), `data/model/*` (`ManualCycle`, `Transaction`, `Category`), `data/repository/*` |
| DI | `di/AppModule.kt` (DataStore `cyclewise_settings`), `di/DatabaseModule.kt` (Room + migrations 1→4) |
| UI | `ui/dashboard/*` (5 variants), `ui/cycle/*`, `ui/entry/AddTransactionSheet.kt`, `ui/settings/*`, `ui/onboarding/*`, `ui/components/*` |
| Widget | `widget/DvideWidgetProvider.kt` (RemoteViews balance widget) |

`README.md` under `app/` is stale (describes a different package layout) — do not trust it.

## C. Theme and Design System

- `ui/theme/Theme.kt` `DvideTheme(seedHue, darkTheme, dynamicColor, …)` builds a Material 3 `ColorScheme` from a hue via
  `colorSchemeForHue` (`Color.kt`, HSL-based) and provides `LocalDvideColors` (category colors) and `LocalCurrencyFormatter`.
- **Appearance settings:** `darkTheme` (default true), **Chameleon mode** = the `dynamicColor` preference. When **off** the
  hue is fixed at 300; when **on** the user's `seedHue` drives the whole palette. It is *not* Android Material You.
- Typography `ui/theme/Type.kt` (user-selectable font family, `AppFont`), shapes `Shape.kt`, motion `Motion.kt`,
  spacing `Dimens.kt`, insets helpers `Insets.kt` (`dvideBottomBarPadding`, `BottomClearance`).
- Window background in `res/values/themes.xml` is the fixed brand-dark `splash_background` (#100C14) by design.
- Rule: use `MaterialTheme.colorScheme` / `MaterialTheme.dvideColors`; avoid raw `Color(0x…)` for chrome.

## D. Data Models and Persistence

- Room (`dvide.db`, v4, exported schemas in `app/schemas/`): `cycles` (`ManualCycle`: id, month, year, startDate, endDate,
  income) and `transactions` (`Transaction`: id, date, category key, kind `expense|aside|income`, amount, note, color ARGB
  Int?, cycleId?). Categories are *not* a table: built-in keys in `Category` plus free-text custom keys on the transaction.
- DataStore (`cyclewise_settings`): see `SettingsRepository` keys (theme, hue, dashboard variant, profile, currency, region,
  week start, number format, decimals, payday, font, profile picture URI, `seeded`).
- `fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)` is configured — a downgrade wipes data. Do not ship a
  lower DB version than a previously released one.
- `app/schemas/` also contains stale schema folders for old package names (`com.dvide.app…`); harmless.

## E. Feature Implementation Status

"Verified" = covered by an automated check that was actually run (see H). No emulator/device was available in the
authoring environment, so nothing below was run on a device; "device-unverified" means exactly that.

| Feature | Current state | Key files | Verification | Outstanding |
|---|---|---|---|---|
| Light / Dark / Chameleon theme | Unchanged behaviour. System-bar style now follows the **in-app** theme (`enableEdgeToEdge` + `SystemBarStyle.auto { darkTheme }` in `MainActivity`); the icon `SideEffect` was removed from `DvideTheme`. | `ui/theme/Theme.kt`, `Color.kt`, `MainActivity.kt` | Compiles; no raw chrome colors found by grep. Bar icon contrast **device-unverified**. | Window background is always the brand-dark splash colour, so a light-theme user sees dark → light at launch (inherent; the window exists before settings load). |
| Cold-start flash | Root cause fixed: `MainViewModel.settings` used `AppSettings()` defaults (dark, `seeded=false`) and Room flows started empty, so the first frame showed dark theme → onboarding → "Start your first cycle". `AppSettings.isLoaded` + nullable Room snapshots feed `MainViewModel.isReady`; `MainActivity.holdFirstFrameUntilReady()` cancels pre-draw until ready (2 s cap). | `MainActivity.kt`, `ui/MainViewModel.kt`, `SettingsRepository.kt` | `PersistenceIntegrationTest` proves "ready ⇒ derived state populated" and "not ready before data". Visual absence of flicker **device-unverified**. | Onboarding → dashboard hand-off after `completeOnboarding` not re-examined. |
| Navigation safety | `navigateTo`/`goBack` only act when the current entry is RESUMED and use `launchSingleTop`, so double taps cannot stack/pop screens. | `ui/navigation/DvideNavHost.kt` | Compiles; **not** unit-tested. | – |
| Swipe actions | Configurable per direction (Settings → Gestures). Defaults: swipe left = Edit, swipe right = Delete (previously both directions only opened the delete dialog). Delete always confirms; income/top-up rows cannot be edited; RTL-aware; TalkBack custom actions; long-press menu and tap-to-expand preserved. Rows are keyed by entry id and read the latest entry (fixed stale-closure bug that could target the wrong entry after a delete). | `domain/model/SwipeAction.kt`, `ui/components/TransactionTimeline.kt`, `SettingsRepository.kt`, `ui/settings/SettingsExtras.kt`, `LocalSwipeConfig` in `Theme.kt` | `SwipeConfigTest`, `InteractionUiTest` (real swipe/long-press gestures under Robolectric), `PersistenceIntegrationTest` (persistence). Feel/thresholds on a real device **unverified**. | – |
| Notes / categories / colors | Note limit 32 → 200, custom category 24 → 40 (`EntryLimits`, enforced at input, in the ViewModel and in backup validation; Room columns are unbounded TEXT so **no migration**). Multi-line note field with counter; full note shown when a row is expanded. Custom color picker (H/S/L sliders + hex) saves to existing `Transaction.color`; commit button text uses black/white by WCAG contrast (≥ 4.5:1 guaranteed). Swatches wrap (no hidden "Custom") and are accessible radio options. | `domain/model/EntryLimits.kt`, `ui/entry/AddTransactionSheet.kt`, `ui/components/ColorPickerDialog.kt`, `ui/theme/ColorUtils.kt` | `EntryLimitsTest`, `ColorUtilsTest`, `InteractionUiTest` | Custom colors are absolute (not theme-adapted); the picker warns when contrast with the surface is < 3:1. |
| Account / profile | Honest **local-only** profile: screen states it is not signed in/verified. Name/email length-limited, email format validated. Photo flow: system photo picker → decode (sub-sampled, EXIF-rotated) → pan/zoom crop → square 512 px JPEG in `filesDir/profile`; only the **file name** is stored. Avatar decodes off the main thread at display size with an LRU cache. Unreferenced avatar files are pruned on save and at startup. | `ui/settings/ProfileScreen.kt`, `ProfileCropDialog.kt`, `data/profile/ProfileImages.kt`, `util/CropMath.kt`, `ui/components/ProfileAvatar.kt` | `CropMathTest`; profile validation in `InteractionUiTest`. Picker, decode, crop gestures and file writes **device-unverified**. | Legacy `content://` refs from older versions are tried once and fall back to initials if unreadable. |
| Google sign-in | **Not implemented — deliberate.** See F and I. | – | – | Needs a product/privacy decision. |
| Backup & restore | Local, offline, versioned JSON ("dvide-backup" v1) via the system file picker (no permission). Replace-all restore in one Room transaction after a confirmation dialog; preferences applied afterwards; bad file ⇒ nothing changes. | `data/backup/*`, `CycleDao`, `SettingsRepository.exportForBackup/applyBackup`, Settings → Data | `BackupCodecTest` (22), `PersistenceIntegrationTest` (real file round trip, replace, ID-collision, bad/missing file). SAF picker UI **device-unverified**. | No merge mode; profile photo not included; no encryption (file is plain JSON the user chooses where to keep). |
| Developer credits | Footer in Settings: tagline, version (`BuildConfig.VERSION_NAME`), "Designed and built by Surajit Das", link to the GitHub repo. Details taken from `PRIVACY_POLICY.md`; nothing invented. | `ui/settings/SettingsExtras.kt` | `InteractionUiTest` (visible). Link launch **device-unverified**. | – |
| Pay Cycle | `Cycle.nextSalaryDate = end + 1 day`; total days, days left (counts today, min 1), cycle ended state and next salary shown in `CycleProgressBar` (Editorial, Waterfall) and `CycleDetailScreen`; "1 day"/"Cycle ended" wording fixed. Day counts follow the calendar (midnight timer + refresh on app start). | `domain/model/Cycle.kt`, `Metrics.kt`, `ui/MainViewModel.kt`, `CycleProgressBar.kt`, `CycleDetailScreen.kt` | `PayCycleTest` (19, incl. leap year, month/year ends, first/last/after-end; mutation-checked) | – |
| Insets | Settings now uses one Scaffold inset policy (horizontal + bottom, top bar owns top) replacing a fixed 80 dp spacer. | `ui/settings/SettingsScreen.kt` | Compiles; device-unverified. | Other screens reviewed and left as they were (already consistent: Profile/CycleDetail use the same pattern; sheets use `safeContent` bottom). |

## F. Important Engineering Decisions

- **Pay cycle convention (do not change silently):** start/end inclusive; `totalDays = days between + 1`; `remaining`
  counts today and is clamped to ≥ 1 because it divides the daily allowance (`safeToSpend`); after the end date
  `ended = true` and `remaining` stays 1; **next salary = end + 1 day**. All math is `LocalDate` based (no clocks/zones).
- **Startup gate instead of a splash dependency:** hold the first draw with an `OnPreDrawListener` (platform API,
  no new library) until `MainViewModel.isReady`. Rejected: `runBlocking` DataStore read on the main thread
  (blocks startup) and masking with animations. Safety cap: 2 s.
- **Not-loaded vs empty:** never infer "empty" from an initial `emptyList()`; use nullable snapshots / `isLoaded`.
- **Swipe never mutates data itself:** `confirmValueChange` always returns false; Edit opens the editor, Delete opens
  the existing confirmation dialog.
- **Entries outlive deleted cycles** (existing product decision: "Entries stay in the ledger"). Therefore
  `Transaction.cycleId` may dangle; backup validation accepts that, and restore raises SQLite's `cycles`
  AUTOINCREMENT counter past every referenced id so a new cycle can't adopt old entries.
- **Backup is local-only and replace-only.** Strict for user data (one bad record rejects the file — no partial
  restore), lenient for preferences (unknown values skipped with a warning, device value kept). No tokens, no photo,
  no `seeded` flag. Rejected: merge/dedupe (no stable natural key for entries), cloud sync (no backend, contradicts policy).
- **Profile photos are copied, never referenced by URI** (picker URIs are transient and `takePersistableUriPermission`
  cannot succeed for `GetContent`). Only a bare file name is stored; refs containing `/`, `\` or `..` are treated as legacy URIs.
- **No Google sign-in (needs your decision):** it requires the `INTERNET` permission and a Google Cloud OAuth client,
  but `PRIVACY_POLICY.md` promises 100 % offline operation, no network permission and no cloud. Shipping it would
  make the policy and the Play Data-safety form false. Nothing in the app currently needs an online identity.
- **One new dependency: `androidx.exifinterface:exifinterface` 1.4.2** (version catalog). The app now reads EXIF
  orientation from arbitrary user-picked photos; lint flagged `android.media.ExifInterface` and the AndroidX copy carries
  parser hardening that older platform versions (min SDK 26) lack. Everything else uses platform/AndroidX APIs already
  present (photo picker, `org.json`, Room `withTransaction`, `core-ktx`). Backup tests run under the existing
  Robolectric so they exercise Android's real `org.json`.
- **Release-variant unit tests are disabled** (`androidComponents.beforeVariants … enableUnitTest = false` in
  `app/build.gradle.kts`). `ui-test-manifest` is `debugImplementation` by design, so Compose-rule tests cannot launch in
  the release variant (this already broke the 4 pre-existing `E2ELocalUiTest` tests under plain `./gradlew test`);
  CI only runs `testDebugUnitTest`. The AGENTS.md command `./gradlew assembleDebug test lintDebug` now passes.
- **Readable-on-color uses pure black/white**, not a soft near-black: only the best-of-{white, black} choice guarantees
  ≥ 4.5:1 for every possible background (a near-black floor is ~4.15:1).

## G. Known Bugs and Outstanding Work

- Google sign-in not implemented (see F/I) — product decision required.
- `android:allowBackup="true"` with no `dataExtractionRules`/`fullBackupContent`: Android Auto Backup may copy the DB,
  DataStore and avatar to the user's Google account, which sits awkwardly with the "no cloud" wording. Needs a decision
  (disable, or exclude via rules). Not changed.
- Transaction list is not lazy: `TransactionTimeline` composes every row inside a `verticalScroll` Column. Fine for
  hundreds of rows; revisit (LazyColumn with sticky headers) if cycles grow large.
- `Settings → Brand colour` hex field re-normalises its text whenever the hue changes (hex→hue is lossy). Pre-existing.
- `Category.oklchToColor` is an HSL approximation (commented in code); category colours are therefore approximate.
- `app/README.md` is stale; `app/schemas/` holds old-package schema folders; `AddTransactionSheet.kt:99` uses a deprecated
  `Locale(String,String)` constructor (pre-existing warning).
- Reduced motion: animations are Compose springs/tweens, which follow the system animator-duration scale; no
  in-app toggle and not runtime-verified.
- Device/emulator verification of every UI flow above is outstanding.

## H. Build, Test, and Verification Status

Environment notes (sandbox, not project config):
- JDK **17** is required by `gradle/gradle-daemon-jvm.properties` (`toolchainVersion=17`); Android SDK platform 36 +
  build-tools 36 needed, `sdk.dir` goes in git-ignored `local.properties`.
- The sandbox proxy rate-limits Maven Central (HTTP 429). Retry-friendly flags that worked:
  `./gradlew --max-workers=2 -Dorg.gradle.internal.repository.max.tentatives=12 -Dorg.gradle.internal.repository.initial.backoff=3000 <tasks>`.

Commands: `./gradlew assembleDebug test lintDebug` (AGENTS.md). Focused: `./gradlew :app:testDebugUnitTest --tests '*PayCycleTest*'`.

**Last full run — 2026-11-01, branch `claude/great-gates-vveyzf`:**
`./gradlew --continue assembleDebug assembleRelease test lintDebug` → **BUILD SUCCESSFUL**.

| Check | Result |
|---|---|
| Unit tests (debug variant) | **122 run, 0 failed** — BackupCodecTest 22, InteractionUiTest 18, PayCycleTest 19, CycleEngineTest 19*, PersistenceIntegrationTest 11, ColorUtilsTest 8, CropMathTest 7, EntryLimitsTest 7, SwipeConfigTest 7, E2ELocalUiTest 4* (*pre-existing) |
| `lintDebug` | **0 errors, 131 warnings** (UnusedResources 59, GradleDependency 45, ModifierParameter 10, HardcodedText 8 in the widget layout, UnusedAttribute 3, AGP version 3, SmallSp 2, others 1 each). None come from code or dependencies added in this work. |
| `assembleRelease` (R8 + `lintVitalRelease`) | Succeeded; unsigned APK ≈ 2.9 MB. Signing/Play upload not exercised (needs CI secrets). |
| Instrumented tests (`connectedAndroidTest`) | **Not run** — no device/emulator in the authoring sandbox. |
| On-device behaviour (flicker, picker, crop gestures, SAF dialogs, system-bar icons, link launch) | **Unverified** |

Baseline before any change (commit `849dc5d`): `assembleDebug` OK; `testDebugUnitTest` 22/23 — the one failure
(`E2ELocalUiTest.testOnboardingWizardFlow`) was Robolectric failing to download its `android-all` jar (Maven Central
HTTP 429 in the sandbox) and passes once the artifact is cached; lint was not reached, so the pre-change lint warning
count is unknown.

Meaningful regression coverage: Pay Cycle boundaries (mutation-checked: breaking `end + 1` fails 9 tests), swipe/long-press
gestures on the real timeline, startup readiness ordering, backup round trip through real files incl. ID-collision case,
backup rejection paths, entry limits/color flow, profile validation.


## I. External Integrations and Configuration

- None active. Release builds declare no `INTERNET` permission; `src/debug/AndroidManifest.xml` adds it (debug only).
- Release signing/Play upload is driven by CI (`.github/workflows/ci-cd.yml`, `.github/scripts/resolve-release-secrets.sh`);
  secrets are environment variables (`STORE_FILE`, `STORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`), never in the repo.
- **If Google sign-in is approved later**, the minimum is: (1) update `PRIVACY_POLICY.md` and the Play Data-safety form;
  (2) add `INTERNET` to the release manifest; (3) Google Cloud project → OAuth **Web** client ID (for
  `GetGoogleIdOption.setServerClientId`) plus an **Android** OAuth client with the package name and the SHA-1 of the debug,
  release and Play-App-Signing keys; (4) dependencies `androidx.credentials:credentials`,
  `credentials-play-services-auth`, `com.google.android.libraries.identity.googleid:googleid`; (5) supply the client ID
  via `local.properties`/CI secret → `BuildConfig`, never committed; (6) keep only name/email/photo from the verified
  credential, never persist ID tokens, never include account data in backups; (7) label signed-in vs local profile in
  `ProfileScreen`.

## J. Recent Changes (this work)

Startup/navigation (`MainActivity`, `MainViewModel`, `SettingsRepository`, `DvideNavHost`); system bars (`MainActivity`,
`Theme.kt`); Pay Cycle (`Cycle`, `Metrics`, dashboards, `CycleDetailScreen`, widget text); configurable swipe
(`SwipeAction`, `TransactionTimeline`, Settings); entry editor (`EntryLimits`, `AddTransactionSheet`, `ColorPickerDialog`,
`ColorUtils`); profile (`ProfileScreen`, `ProfileCropDialog`, `ProfileImages`, `ProfileAvatar`, `CropMath`);
backup/restore (`data/backup/*`, `CycleDao.insertAll/deleteAll`); Settings (gestures, data, credits, insets).
New tests: `PayCycleTest`, `SwipeConfigTest`, `EntryLimitsTest`, `ColorUtilsTest`, `CropMathTest`, `BackupCodecTest`,
`PersistenceIntegrationTest`, `InteractionUiTest`.
Defects found and fixed along the way: stale swipe-state closure (wrong entry could be targeted), hidden "Custom color"
swatch on narrow screens, 40-char category wrapping/clipping the commit button, "1 days left" grammar, `ended` cycles
still saying "1 days left".

## K. Instructions for Future Agents

1. Read this file first, then `git status` before editing.
2. Validate any assumption you will rely on against the source (paths in B/E).
3. Search only for what the task needs; follow the existing MVVM/UDF + Hilt + Compose structure.
4. Keep the pay-cycle convention (F), the privacy posture (A) and backup compatibility (D/F).
5. Make focused changes; add tests next to the behaviour (pure logic → plain JUnit; Compose/IO → Robolectric as in
   `InteractionUiTest`/`PersistenceIntegrationTest`).
6. Run `./gradlew assembleDebug test lintDebug`; report real outcomes only.
7. Update this file (status table, H results with date, new decisions) before finishing.
