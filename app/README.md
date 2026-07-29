# 📱 Dvide App Module (`dvide/app`)

The `app` module provides the user interface, manual cycles calculator, budget tracker, and Compose viewmodels for the Dvide application.

---

## 🏗️ Architecture & Component Layout

```text
app/
├── src/main/
│   ├── java/com/knownassurajit/dvide/
│   │   ├── MainActivity.kt        # Entry Jetpack Compose Activity
│   │   ├── DvideApplication.kt    # Application class & DI setup
│   │   ├── ui/                    # Screens (Dashboard, Cycles, Budget)
│   │   └── viewmodel/             # StateFlow ViewModels
│   └── res/                       # UI assets, themes, & strings
└── build.gradle.kts               # Module build configuration
```

---

## 🛠️ Verification

```bash
./gradlew :app:testDebugUnitTest
```
