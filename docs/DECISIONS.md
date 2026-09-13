# DECISIONS.md

## 2026-09-13 — App Name: RiyStreak
Confirmed app name: RiyStreak (`com.riystreak.app`), aligning with user dev branding.

## 2026-09-13 — Streak correctness computed from dates, not background timers
Android's Doze mode and OEM battery management make exact-time background execution
unreliable across devices. Core streak state must never depend on it.

## 2026-09-13 — Freeze cap set at 2 per streak, earned via 7-day milestones only
Mirrors Duolingo's own deliberate small-cap design: unlimited forgiveness stops
being a safety net and becomes a substitute for the daily habit.

## 2026-09-13 — No exact alarms; WorkManager only
Avoids fragile exact-alarm scheduling and extra Play Store policy scrutiny.
Notifications are explicitly best-effort, never a correctness dependency.

## 2026-09-13 — Multiple independent streaks, not a single focused habit
Confirmed: general habit-tracker scope. Data model already supported this without
changes — each Streak row was independent from the start.

## 2026-09-13 — Notifications digested across streaks, not sent per-streak
Avoids notification spam once a user has several active streaks.

## 2026-09-13 — Home-screen widget included in v1, per-streak instances
Jetpack Glance, one widget instance per streak via configuration activity.

## 2026-09-13 — Open source (MIT license), non-commercial, About screen added
Source published publicly on GitHub; About screen links out to it plus the portfolio
site. No ads, no in-app purchases anywhere in the app.

## 2026-09-13 — No donation/payment link inside the app; GitHub Sponsors externally instead
Google Play's tax-exempt-donation policy carve-out applies only to verified nonprofit
orgs. Support ask lives entirely on the GitHub repo page instead.
