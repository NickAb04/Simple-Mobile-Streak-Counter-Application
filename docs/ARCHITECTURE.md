# ARCHITECTURE.md

## Core principle
Streak state, per streak, is a pure function of (today's local date, that streak's
lastCompletedDate, its freezesAvailable) — recomputed on demand, never trusted to a
background timer. See the brainstorm doc §6 for full reasoning.

## Layers
UI (Compose, multi-screen via Navigation Compose)
  → ViewModel (StateFlow)
    → Repository  ←── also read by the Glance widget (shared source of truth)
      → Room (Streak, DayLog tables)
      → DataStore (AppSettings)

## Notification layer
WorkManager schedules a single digested check across all streaks. Never gates core
functionality; never one notification per streak.

## Widget
Jetpack Glance, one instance per streak, chosen via a configuration activity at
add-time. Reads/writes through the same Repository as the main app.
