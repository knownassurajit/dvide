# Cyclewise Knowledge Base (LEARN.md)

Welcome to the Cyclewise project. This document serves as the living documentation and design guide for the application. Any agent or developer working on the project must read and maintain this file.

---

## 1. Project Overview & Business Logic

Cyclewise is a salary-cycle personal finance tracker for Android. Unlike typical calendar-month trackers, Cyclewise models spendable money around a **repeating monthly window anchored to a payday**.

### The Budget Waterfall
All incoming salary flows through a single calculation engine (`CycleEngine`):
```
Income
  − Set aside  (Savings · Investment · Security)
  = Spendable
  − Spent      (Essentials · Lifestyle)
  = Balance   →  safe-to-spend per day remaining
```

Three dashboard layouts render this flow: Editorial (Big-number daily allowance), Gauge (Ring runway), and Cards (stacked hero + bucket stats).

---

## 2. Technical Stack & Architecture

### Dependency Layers
The application uses a unidirectional data flow (UDF) MVVM pattern:

```
[Room Database / DataStore] ──> [Repositories] ──> [ViewModel] ──> [Compose UI]
```

- **Core Framework:** Kotlin 2.1 / Jetpack Compose with Material Design 3 Expressive.
- **Dependency Injection:** Hilt 2.52 (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`).
- **Database:** Room 2.6 (SQLite) for storing transaction models.
- **Preferences:** Jetpack DataStore Preferences for app state (selected theme seed, payday anchor, target numbers).
- **Navigation:** Navigation Compose 2.8.
- **Calculations:** Pure Kotlin ports of budget maths (`CycleEngine.kt`) combining settings data and room transactions into `Metrics`.

### Packaging Structure
> [!IMPORTANT]
> The Android application ID is `com.dvide.app` (or `com.dvide.app.debug` for local debug builds). However, the source code resides under package folders `com/dvide/cyclewise/` for historical continuity. Do not align directory structure to package declarations; follow the established path.

---

## 3. Design System & Styling Rules

### Color Seed System
The app uses four HSL/OKLCH color themes selectable via a slider in Settings:
- **Violet:** Accent primary dark `#D4AAFF` / light `#6B3FA8`
- **Indigo:** Accent primary dark `#B8B4FF` / light `#5A48B8`
- **Forest:** Accent primary dark `#72DEBF` / light `#00715F`
- **Clay:** Accent primary dark `#FFB77A` / light `#8A4A00`

State indicators (e.g. over-spent terracotta) are fixed to maintain warning clarity.

### Organic Shapes
Asymmetrical expressiveness is built using Custom RoundedCornerShapes:
- `ShapeGaugeCard`: `RoundedCornerShape(topStart=40, topEnd=40, bottomEnd=40, bottomStart=16)`
- `ShapeGaugeCardSharp` (used when budget stress morphs): `RoundedCornerShape(16)`

---

## 4. Maintenance Guidelines

To ensure this knowledge remains accurate as the codebase evolves, developers and agents must adhere to the following rules:

1. **Iterative Updates:** Every time a database schema changes, a new dependency is added, or the budget algorithm is refactored, the corresponding section of this file must be updated.
2. **Design Tokens:** If new typography scales, shape tokens, or color seed schemes are introduced, document them in Section 3.
3. **No Dead Documentation:** Verify that package name references, paths, and build commands match the latest Gradle/manifest configuration.
