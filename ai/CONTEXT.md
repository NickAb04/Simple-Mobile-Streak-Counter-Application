# CONTEXT.md

## Where things stand
- Scaffold: Complete (Gradle 8.13, target SDK 36, Kotlin 2.0+).
- Core Data & Business Logic: Complete (`StreakRepository`, Room DB, `StreakLogicTest` passing).
- Notification System: Complete (WorkManager digested notifications + quick action broadcast receiver).
- UI Screens: Complete (HomeScreen, StreakDetailScreen with history grid, AddEditStreakScreen, SettingsScreen, OnboardingScreen, AboutScreen).
- Widget: Complete (Jetpack Glance per-streak widget + `WidgetConfigActivity`).
- Build Status: Verified via `testDebugUnitTest` and `assembleDebug` (`BUILD SUCCESSFUL`).
