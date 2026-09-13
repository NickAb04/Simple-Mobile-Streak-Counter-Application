# AGENTS.md

## Stack
Kotlin + Jetpack Compose (native Android), Navigation Compose for the multi-screen
structure. MVVM + Repository pattern over Room. DataStore for settings. WorkManager
for reminders — NOT exact alarms. Jetpack Glance for the per-streak home widget.

## Conventions
- Streak state (current count, frozen/missed days) is ALWAYS recomputed per-streak
  from stored LocalDates on app open / button tap / widget tap — never assume a
  background job has already updated it. See docs/ARCHITECTURE.md.
- Freeze consumption logic lives in ONE place (the Repository), shared by the app UI
  AND the widget — never duplicated.
- Notifications are DIGESTED across streaks, never one-per-streak (§3.5) — avoid
  reintroducing per-streak notification spam.
- All dates are java.time.LocalDate, compared against the device's current local date.
- Background work (WorkManager) is ONLY for notifications, which are best-effort.
  Never make notification delivery a dependency for correctness of streak data.

## Before making changes
- Check docs/DECISIONS.md before reversing a past architectural choice.
- Check ai/CONTEXT.md for current in-progress work.

## After making changes
- Append an entry to ai/PATCHLOG.md.
- Log non-trivial decisions in ai/DISCUSSION.md or docs/DECISIONS.md.

## Never
- Never rely on an exact-time background job for core streak correctness.
- Never request SCHEDULE_EXACT_ALARM without discussing in ai/DISCUSSION.md first.
- Never silently expand the freeze cap (2) or earning rate (7-day milestone) without
  logging why in docs/DECISIONS.md — deliberate design principles, not placeholders.
- Never let the widget read a separately cached state from the main app.
- Never add a payment, tip, or donation link/button inside the app's own UI (About
  screen included) — Google Play policy risk (§11). Any support link belongs on the
  external GitHub repo/README only, never in the APK.
