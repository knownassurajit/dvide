# dvide (Cyclewise)

Kotlin 2.1 · Compose · Material 3 Expressive · MVVM/UDF · Hilt · Room. Salary-cycle finance tracker.

Package: `com.knownassurajit.dvide_finance.app`. Keep the income → set-aside → spendable → spent → daily-balance waterfall consistent across dashboards.

ECC is installed for Cursor. Prefer `kotlin-patterns`, `android-clean-architecture`, `material-design`. Review with `ecc-kotlin-reviewer`.

Verify: `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`

## Cursor Cloud specific instructions

- The Gradle daemon requires Java 17 (`gradle/gradle-daemon-jvm.properties`). `JAVA_HOME` is `/usr/lib/jvm/java-17-openjdk-amd64`. Java 21 is also on the image and is not the daemon JVM.
- The Android SDK lives at `/opt/android-sdk` (platform 36, build-tools 35.0.0). Environment install writes `local.properties` with `sdk.dir=/opt/android-sdk`. `adb` and `sdkmanager` are on `PATH`.
- Use the verify command above. It matches CI. `./gradlew test` also runs `testReleaseUnitTest`, which fails the Robolectric Compose tests because `androidx.compose.ui:ui-test-manifest` is `debugImplementation` only.
- No emulator starts on boot. `CycleEngineTest` covers the income → set-aside → spendable → spent → daily-balance waterfall. `E2ELocalUiTest` covers onboarding, profile save, currency selection, and the empty dashboard.
