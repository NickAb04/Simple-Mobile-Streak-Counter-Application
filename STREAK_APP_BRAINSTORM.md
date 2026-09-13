# Streak Tracker — Full Brainstorm & Build Plan (Native Android)

*A native Android habit tracker built entirely around a Duolingo-style streak counter with a small, deliberate forgiveness mechanic. Save this file — hand it to your coding agent alongside the AI context files in §13.*

**Status: Architecture, mechanics, and scope finalized.** Only app name/branding remains open (§16) — everything else in this document is ready to build against.

---

## 1. Product Summary

A native Android **general habit tracker**: you can create multiple independent streaks (e.g. "Read 10 pages," "No smoking," "LeetCode daily"), each tracked separately with its own count and its own bank of streak freezes. Tap "I did it" on a streak once you've done it that day; miss a day and a stored freeze silently covers it, mirroring exactly the mechanic that's kept you coming back to Duolingo. No lessons, no gems economy, no social features — just that one loop, isolated and pointed at whatever habits you choose.

---

## 2. Design Principles Borrowed From Duolingo's Research

Drawn from how Duolingo's system actually works and *why*, adapted to a simpler app with no currency/economy:

- **The reset rule is date-based, not effort-based.** A day counts if you tapped the button that day; streak resets to zero on a missed, unprotected day.
- **Freezes are proactive, not retroactive.** A freeze must already be banked *before* a day is missed — never buyable after the fact. This is a hard rule, not a UX nicety.
- **Freezes apply silently.** No "use your freeze?" dialog. You open the app after a missed day, see it was covered, move on. Requiring a decision at the moment of failure defeats the purpose.
- **The freeze cap is small and deliberate.** Unlimited forgiveness stops being a safety net and starts being a substitute for the habit itself.
- **Each freeze covers exactly one missed day.** Two consecutive missed days need two banked freezes.

---

## 3. Core Feature Breakdown

### 3.1 Creating a streak
Each streak is fully independent — its own count, its own freeze bank, its own history:
- **Title** (required) — e.g. "Read 10 pages," "No smoking," "LeetCode daily."
- **Description** (optional) — freeform notes on what "done" means, so future-you doesn't relitigate the definition every day.
- **Start date** — defaults to today.
- **Reminder time** (optional per-streak override) — falls back to the global default if not set (§7).

### 3.2 The "I did it" button and daily reset
- The button is the **only** way any individual streak increments.
- **Reset logic is date-based, not timer-based** — a deliberate architecture choice explained fully in §6. Whenever the app is opened (or a button tapped), it compares today's local date against that streak's last-completed date and computes the correct state on the spot, rather than depending on a background job firing at exactly midnight.
- Once tapped for the current local day, a streak's button shows "done for today" and goes inert until the next local day.
- Tapping "I did it" on a day that was previously freeze-covered resumes normal incrementing from that day forward.

### 3.3 Streak freezes — confirmed mechanic
- **Earned automatically at streak milestones** — +1 freeze every 7 consecutive days maintained, per streak. No purchasing, no manual acquisition, no shared economy across streaks — each streak earns and spends its own freezes independently.
- **Capped at 2 banked freezes per streak at a time**, deliberately mirroring Duolingo's own reasoning (§2).
- **Consumption:** on app open, if days were missed since a streak's last completion, one freeze is consumed per missed day, oldest first, up to however many are banked for that streak. If missed days exceed available freezes, that streak resets to zero — no partial credit. Other streaks are entirely unaffected.
- **Longest streak** is tracked per streak, never resets — a permanent personal record shown alongside the current count.

### 3.4 Local data storage
All data for all streaks lives on-device — no account, no login, no server, no network calls. No backend to build or pay for, and "this app can't leak your data because it never leaves your phone" is true almost for free.

### 3.5 Notifications — designed for multiple streaks
With several streaks running at once, one notification *per streak* would quickly become spammy. Notifications are **digested**, not per-streak:
1. **Daily reminder** — one notification listing every streak not yet completed today (e.g. "3 habits still open today: Read, Exercise, No smoking"), sent only if at least one is incomplete.
2. **Missed-day notice** — one digest covering every streak that had a day covered by a freeze (or broken, if none were available) since last opened.
3. **Freezes-exhausted warning** — escalated notice for any streak whose freeze bank is now empty and isn't done today, since its next miss breaks it outright.

### 3.6 Home-screen widget (confirmed for v1)
- Built with **Jetpack Glance** (§5).
- **Per-streak widget instances**, using Android's standard widget configuration-activity pattern: adding the widget to the home screen prompts a picker asking which streak this instance tracks. Want three streaks visible at a glance? Place three widget instances.
- Each instance shows the streak's title, current count, and a one-tap complete button — no need to open the app at all.
- Recommended v1 scope over a single combined "all streaks" widget: the per-instance pattern is the standard, well-supported Android approach and meaningfully simpler to build correctly; a combined multi-streak list widget is a reasonable v2 enhancement once the core app is solid.

---

## 4. Data Model

The schema already scales to multiple simultaneous streaks with no changes needed — each `Streak` row is fully independent by design:

```
Streak
├─ id
├─ title
├─ description (nullable)
├─ createdDate (LocalDate)
├─ currentStreakCount (Int)
├─ longestStreakCount (Int)
├─ lastCompletedDate (LocalDate, nullable)
├─ freezesAvailable (Int, capped at 2)
├─ reminderTimeOverride (LocalTime, nullable — falls back to global setting)
├─ isArchived (Boolean)

DayLog                                  -- powers the per-streak history/calendar view (§8)
├─ id
├─ streakId (fk)
├─ date (LocalDate)
├─ status (enum: completed | frozen | missed)

AppSettings (DataStore, not Room — simple key-value)
├─ notificationsEnabled (Boolean)
├─ defaultReminderTime (LocalTime)
```

`DayLog` is what makes the calendar/history view and "why did my streak reset" transparency possible for each streak independently.

---

## 5. Recommended Tech Stack

| Layer | Recommendation | Why |
|---|---|---|
| Language | **Kotlin** | The standard, Google-endorsed language for native Android — matches your "native" requirement directly. |
| UI toolkit | **Jetpack Compose** | Google's current recommended declarative UI toolkit. |
| Navigation | **Navigation Compose** | Needed now that the app is multi-screen by design: a home list of streaks → per-streak detail → create/edit → settings. |
| Architecture | **MVVM** (ViewModel + StateFlow) with a **Repository** layer wrapping Room | Keeps streak-calculation logic (§6) testable and separate from UI code, and scales cleanly to a list-of-streaks home screen. |
| Local database | **Room** | Google's recommended SQLite abstraction; satisfies "all data saved locally" with zero network dependency. |
| Settings storage | **Jetpack DataStore** | For the small global settings in §4. |
| Widget | **Jetpack Glance** | Confirmed for v1 (§3.6) — the current Compose-based way to build home-screen widgets, including per-instance configuration. |
| Background scheduling | **WorkManager** for digested reminders; deliberately **avoiding exact alarms** — see §6/§9 | Reduces battery-optimization fragility and Play Store scrutiny of sensitive alarm permissions. |
| Notifications | **NotificationCompat** + Notification Channels | Standard Android notification API. |
| Date/time handling | **`java.time.LocalDate`/`LocalTime`** | Avoids timezone/instant footguns — streak logic reasons in local calendar dates, never raw timestamps (§6). |
| Dependency injection | **Hilt** (optional) | Genuinely optional at this app's size, but a reasonable habit to build given the app now has real multi-screen structure. |
| Build | **Gradle**, target SDK **API 36 (Android 16)** | Google Play requires all apps to meet the latest target API level, currently API 36 as of Aug 31, 2026 — an ongoing, roughly-annual maintenance requirement. |

**Net cost: $0** to build, plus the one-time $25 Play Store fee (§10).

---

## 6. Architecture Principle — Streak Correctness Must Not Depend on Background Timing

The single most important technical decision in the app, worth calling out on its own.

**The temptation:** schedule a background job to fire at exactly local midnight and update streak state. **The problem:** Android's Doze mode, battery optimization, and especially aggressive OEM background-killing (Xiaomi/MIUI, Huawei, OnePlus/OxygenOS, and to a lesser extent Samsung all have reputations for killing background work more aggressively than stock Android) mean a background job is not guaranteed to fire at exactly midnight, or at all, on every device. If your *core streak count* depends on that job running reliably, you'll eventually see incorrectly reset streaks on some phone model you've never tested on — a fatal flaw for an app whose entire value proposition is trustworthiness of the count. This matters even more now with multiple streaks running simultaneously, since a single missed background run could silently corrupt several counts at once.

**The fix:**
- Streak state (current count, frozen days, whether today is done) is a **pure function of stored dates per streak**, recomputed every time the app is opened or a button tapped: compare each streak's `lastCompletedDate` to today's actual local date, resolve freezes/reset accordingly, right then.
- Background scheduling (WorkManager) is used **only** for the digested notification layer (§3.5) — inherently best-effort. A reminder arriving a few minutes late, or occasionally not at all on one device, is a minor UX miss, not a broken core feature.
- The app doesn't need Android's sensitive **exact alarm** permission (`SCHEDULE_EXACT_ALARM`) at all — WorkManager's flexible scheduling is sufficient for "remind me sometime this evening," simplifying the code and avoiding extra Play Store policy scrutiny.
- The widget (§3.6) follows the same rule: it reads the same recomputed-on-access state, never a separately cached "midnight snapshot."

---

## 7. Notification System Design

| Notification | Trigger condition | Timing |
|---|---|---|
| Daily reminder (digest) | ≥1 streak not yet completed today | Evening, default configurable in Settings (proposed default: 8:00 PM local) |
| Missed-day notice (digest) | ≥1 streak had a freeze consumed since last check | Next app-relevant check (app open, or next scheduled reminder) |
| Freezes-exhausted warning (digest) | ≥1 streak's freeze bank is empty AND it isn't done today | Same evening reminder slot, escalated copy |

**Recommended addition:** attach quick-action buttons directly to the daily digest notification for streaks nearing their reminder deadline (`NotificationCompat.Action`, one action per open streak, up to Android's practical action-button limit) — lets several habits get marked done without opening the app at all.

Requires runtime `POST_NOTIFICATIONS` permission on Android 13+ (API 33+); the app should degrade gracefully (still fully functional, just silent) if denied.

---

## 8. Recommended Additions (flagged — not in your original ask)

- **Longest-streak record** (§3.3, §4) — near-zero cost, meaningfully softens the sting of a broken streak.
- **Per-streak history/calendar view** — a month grid of completed/frozen/missed days using `DayLog`. Mirrors Duolingo's own calendar-with-snowflake-icons and gives the freeze mechanic visible transparency.
- **Home screen sort order** — streaks not yet completed today (especially ones with low/no freezes left) sorted to the top, ahead of already-done streaks — keeps the most actionable items visible without scrolling.
- **First-run onboarding** — a short explainer of how freezes work, since it's a system the user needs to trust and understand up front.
- **Battery-optimization guidance screen** — a short in-app prompt (with a deep link to the relevant system settings) explaining that some phones may delay reminder notifications unless the app is excluded from battery optimization.
- **Combined multi-streak list widget** — a v2 idea once per-instance widgets (§3.6) are solid: one larger widget showing all streaks with individual complete buttons, for users who don't want to place several separate widget instances.

---

## 9. Android-Specific Technical Considerations

- **Doze mode / OEM battery management** — covered in §6; reminders are best-effort by design, core streak logic is not affected.
- **Notification permission (Android 13+)** — requested at runtime; app remains functional without it.
- **No exact alarms needed** — deliberately avoided per §6, simplifying both the code and Play Store policy exposure.
- **Timezone edge case (disclosed, not solved):** the app always uses the device's current local timezone at the moment it checks the date. A user crossing timezones at exactly the wrong moment around midnight could, in rare cases, see an odd result. Accepted as a known limitation rather than over-engineered around, for a personal-use app.
- **Minimum/target SDK** — target API 36 (Android 16) per current Play Store policy (§5); a reasonable minimum SDK (e.g. API 26+, when Notification Channels were introduced) keeps notification code simple without excluding many real devices.
- **Widget refresh behavior** — Glance widgets should read from the same Repository as the app, not a stale cached copy, so a streak marked done from the widget and one marked done from inside the app never disagree.

---

## 10. Publishing to Google Play — Feasibility, Process, and Realistic Timeline

**The app itself is simple to build. The publishing *process* has genuine friction worth planning for now.**

### Account setup
- **Personal account** (not Organization) is the right choice — Organization accounts skip the closed-testing requirement below but require a business D-U-N-S number, impractical for an individual student.
- One-time **$25 USD registration fee**.
- Google is currently rolling out **mandatory identity verification** for all developer accounts through 2026–2027 — expect to submit an ID document and wait roughly 2–5 business days before you can do anything else.

### Closed testing requirement (the part most first-timers don't expect)
- Personal accounts created after November 2023 must run a **closed test with at least 12 testers, opted in continuously for 14 days**, before applying for production release.
- You need to actually **recruit 12 people** willing to install the test build and keep it for two weeks. Realistic sources: classmates/friends, university CS/tech Discord or WhatsApp groups, or beta-tester exchange communities — line this up *before* the app is finished, since it's the real bottleneck.
- After the 14 days, submit a short production-access questionnaire; Google reviews within roughly 1–3 business days.

### Realistic total timeline
Identity verification (~5 business days) → recruit testers & run closed test (14 days, can overlap with late development) → production review (1–3 days). **Budget roughly 3–4 weeks from "code-complete" to "live,"** independent of development time. Start account creation and tester recruitment *before* the app is finished.

### Ongoing maintenance obligation
Google periodically raises the required **target API level** (currently API 36 as of Aug 2026) — expect to bump `targetSdkVersion` roughly annually to stay compliant.

---

## 11. Open Source, About Page, Legal & Security Considerations

### Open source + non-commercial framing — confirmed, and a good call
Publishing the source publicly on GitHub and framing this as a non-commercial learning project has no downside for Play Store approval — plenty of published apps are open source, and "non-commercial" here just means no ads and no in-app purchases, which naturally falls out of not building any. Practical notes:
- **Pick an actual license** for the repo — **MIT** is the recommended default: simple, permissive, the most common choice for exactly this kind of portfolio project, and doesn't obligate anyone who forks it to also open-source their changes (unlike GPL).
- **Never commit signing secrets.** Once the repo is public, this matters for real — the release keystore/signing key must never be committed. Using **Google Play App Signing** (already recommended in §11 Security below) helps here: Google manages the actual distribution key, so only the smaller upload key needs protecting on your end, and it still must never end up in the public repo, `.gitignore`'d from day one.
- **Nice cross-project link:** the About screen can link out to your portfolio website (from the earlier brainstorm) and vice versa — this app is a natural second project entry there.

### About page content (new screen, added to §12)
- Short blurb: built as a learning project to understand native Android development and habit-formation mechanics.
- Link to the GitHub repository (source code).
- Link to your portfolio website, if you want the cross-reference.
- Attribution/inspiration note: mechanic inspired by Duolingo's streak system, independently built — pairs naturally with the trademark-distance guidance already in this section.

### Donations — a real policy finding, not just a formality
This needed checking rather than assuming, and what I found changes the recommended approach: **Google Play's policy carve-out for "tax-exempt donations" (the exception that lets an app link to an external payment method instead of Google Play's own billing) applies only to verified tax-exempt nonprofit organizations — not to individual developers accepting personal support.** This isn't a theoretical reading of the policy; it's been enforced directly against open-source Android apps. StreetComplete, an open-source Android app, was required to remove its in-app links to Patreon, Liberapay, and GitHub Sponsors specifically because those routed money to individuals, not a registered charity. A 2026 case against AnkiDroid shows Google enforcing this narrowly even against apps that *do* have some nonprofit status, if it isn't the specific charitable kind Google recognizes.

**What this means for you:** putting a "Donate to support me" button with a PayPal/Ko-fi/Buy Me a Coffee/GitHub Sponsors link **inside the app's own UI** is a genuine policy risk — not a gray area you're likely to get away with, based on the precedent above, and a Payments-policy violation can jeopardize the whole developer account, not just this app.

**The clean, low-risk alternative — keep the ask outside the app entirely:**
- The in-app About screen stays limited to the content above (bio, GitHub link, inspiration note) — **no payment or donation link inside the app's UI at all.**
- Set up **GitHub Sponsors** on the repository itself (fits perfectly since the code is already going there, and GitHub takes no platform cut beyond standard payment processing) — or Ko-fi/Buy Me a Coffee if you'd rather a simpler single-page "tip jar" not tied to a Sponsors application.
- Anyone who follows the About screen's GitHub link and wants to support you finds the Sponsor/donate option **on GitHub's website**, entirely outside the app binary Google reviews — this is standard, low-risk practice among open-source Android developers for exactly this reason.
- One honest caveat: personal donations received this way are generally your personal income under Malaysian tax rules depending on amount/frequency — worth keeping basic records; this isn't tax advice, just a flag to look into if amounts become non-trivial.

### Legal
- **Privacy policy — required for every app on Google Play**, even one collecting nothing. Draft starting point:

  > *This app does not collect, store, or transmit any personal data to any server. All streak data you enter is stored exclusively on your device using local storage. The app does not require an account, does not use analytics or advertising SDKs, and does not share any information with third parties.*

  Host as a simple static page (a single GitHub Pages markdown file works) and link it in the Play Console listing.
- **Data Safety form** — required for every app; here a straightforward "does not collect or share user data" declaration.
- **Content rating (IARC questionnaire)** — quick, free; expect "Everyone" for a plain utility app.
- **Naming/trademark** — since this app is explicitly inspired by Duolingo's mechanics, avoid any name, icon, or branding that could be confused with Duolingo itself (no owl mascot, no "Duo" in the name). The mechanic is a well-understood, unpatentable pattern; the branding is not yours to borrow.
- **Terms of service** — not strictly required, but a one-line disclaimer that notification timing can vary by device/manufacturer (§6/§9) manages a real expectation cheaply.

### Security
- **No accounts, no credentials, no server** — eliminates most of the usual mobile-app attack surface by construction.
- **Android's app sandboxing** protects the local Room database by default; no additional encryption needed given the low sensitivity of streak-title/count data.
- **`android:allowBackup`** — recommend leaving enabled so a user's earned streak history across all their habits survives a device change via Android's standard encrypted backup.
- **Optional privacy touch:** an app-level PIN/biometric lock, in case someone tracks a personal habit they'd rather keep private — flagged as optional.
- **Code shrinking/obfuscation (R8)** for the release build — standard, effectively free via Gradle.
- **Google Play App Signing** — use Google's recommended managed signing rather than self-managing the release key.

---

## 12. UI / Screen Map

Now explicit given the confirmed multi-streak scope:

1. **Home** — list of streak cards (title, current count, freeze indicator, one-tap complete), sorted per §8, "+ New Streak" entry point.
2. **Create/Edit Streak** — title, description, start date, optional reminder-time override.
3. **Streak Detail** — full history/calendar (§8), longest-streak record, freeze bank, edit/archive/delete.
4. **Settings** — global default reminder time, notification toggle, battery-optimization guidance (§8), privacy policy link.
5. **Onboarding** (first run only) — short explainer of the freeze mechanic.
6. **Widget configuration** (system-provided flow) — picks which streak a given widget instance tracks (§3.6).
7. **About** — learning-project blurb, GitHub repo link, portfolio site link, Duolingo-inspiration note. **No payment/donation link lives here** — see §11.

---

## 13. AI Context Management Files

### `AGENTS.md`
```markdown
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
```

### `docs/ARCHITECTURE.md`
```markdown
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
```

### `docs/REQUIREMENTS.md`
This document's §3–9 and §12 in full — the in-scope/out-of-scope source of truth.

### `docs/DESIGN.md`
```markdown
# DESIGN.md
(To be filled in once visual direction is decided — out of scope for this brainstorm,
which focused on mechanics/architecture/publishing feasibility.)
```

### `docs/DECISIONS.md`
```markdown
# DECISIONS.md

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
Jetpack Glance, one widget instance per streak via configuration activity, rather
than a single combined multi-streak widget (deferred to v2, §8).

## 2026-09-13 — Open source (MIT license), non-commercial, About screen added
Source published publicly on GitHub; About screen links out to it plus the portfolio
site. No ads, no in-app purchases anywhere in the app.

## 2026-09-13 — No donation/payment link inside the app; GitHub Sponsors externally instead
Google Play's tax-exempt-donation policy carve-out applies only to verified nonprofit
orgs, not individual developers (confirmed via StreetComplete/AnkiDroid enforcement
precedent). An in-app "donate" button is a real Payments-policy risk. Support ask
lives entirely on the GitHub repo page instead, outside the reviewed app binary.
```

### `ai/CONTEXT.md`
```markdown
# CONTEXT.md (last updated: ...)

## Where things stand
- Data layer (Room + Repository): ...
- Streak calculation logic: ...
- Notification digesting: ...
- Home/Detail/Create UI: ...
- Widget: ...

## Active blockers / open questions
- App name/branding (see brainstorm §16)

## Publishing status
- Play Console account: not started / verification pending / verified
- Closed testing: not started / in progress (day X of 14) / complete
```

### `ai/PATCHLOG.md`
```markdown
# PATCHLOG.md

## 2026-XX-XX
- Added: freeze-consumption logic in StreakRepository, shared by app + widget
- Files touched: data/StreakRepository.kt, data/Streak.kt, widget/StreakWidget.kt
```

### `ai/DISCUSSION.md`
```markdown
# DISCUSSION.md

## Per-streak widget instances vs. one combined widget
Chose per-instance (§3.6) for v1 as the standard, simpler-to-build-correctly Android
pattern. Combined widget logged as a v2 idea, not forgotten, in docs/DECISIONS.md.
```

---

## 14. Phased Build Roadmap

1. **Scaffold** — Android Studio project, Kotlin + Compose + Navigation Compose, Room + DataStore wired up, GitHub repo, seed `docs/`/`ai/` files.
2. **Core data layer** — `Streak`/`DayLog` entities, Repository with the per-streak date-based state-computation logic (§6) built and unit-tested *before* any UI.
3. **Home + Create/Edit + Detail screens** — the multi-streak list, per-streak detail with history/calendar, longest-streak record.
4. **Notification layer** — WorkManager-scheduled digested reminders, permission handling, the three digest types (§7).
5. **Home-screen widget** — Glance widget with per-streak configuration activity, sharing the app's Repository (§3.6/§9).
6. **Settings screen** — reminder time, notification toggle, battery-optimization guidance.
7. **Polish** — onboarding explainer, optional app lock.
8. **Start Play Console account + identity verification in parallel with step 7** — takes days, shouldn't block on the app being finished.
9. **Recruit closed-test testers** (§10) — start as early as realistically possible; the 14-day clock is the true bottleneck.
10. **Closed testing period** (14 days) → production questionnaire → **ship.**

---

## 15. Cost Summary

| Item | Cost |
|---|---|
| Android Studio, Kotlin, Jetpack libraries (incl. Glance) | $0 |
| Local-only architecture (no backend/server) | $0 |
| Google Play Developer registration | $25 one-time |
| Everything else (hosting a static privacy-policy page, etc.) | $0 |

**Total: $25, once, ever.**

---

## 16. Confirmed Decisions Log

| # | Decision | Answer |
|---|---|---|
| 1 | Platform | Native Android (Kotlin + Jetpack Compose), not cross-platform |
| 2 | Distribution | Google Play Store, Android only, Personal developer account |
| 3 | Data storage | 100% local (Room + DataStore), no backend, no accounts |
| 4 | Streak reset logic | Date-based computation on app open, not background-timer-based (§6) |
| 5 | Freeze behavior | Proactive-only, silent auto-application, one freeze per missed day |
| 6 | Streak scope | Multiple independent streaks — general habit tracker, not a single focused habit |
| 7 | Freeze earning | Automatic, 7-day milestone, capped at 2 per streak — no manual/purchase system |
| 8 | Home-screen widget | Included in v1, per-streak configurable instances (Jetpack Glance) |
| 9 | Notification design | Digested across streaks, never one notification per streak |
| 10 | Open source | Public GitHub repo, MIT license, non-commercial (no ads, no IAP) |
| 11 | About page | Included (§12) — learning-project bio, GitHub + portfolio links, no payment link |
| 12 | Support/donations | GitHub Sponsors (or Ko-fi) on the GitHub repo page only — never inside the app itself (§11 policy finding) |

## Still Open

- **App name/branding** — the only thing left. Candidates so far:
  - **RiyStreak** (your suggestion) — checked, no existing app collision found; ties cleanly to your "RiyRiy" dev brand, which has real portfolio-consistency value.
  - **Streakly** — clean, describes the function directly, easy to search for.
  - **Frostreak** (Frost + Streak) — plays specifically on the freeze mechanic, which is the app's actual differentiator, not just the counting.
  - **StreakVault** — leans into the "banked freezes" framing already used throughout this document.
  No rush to lock this in, but it's needed before Play Console setup — keep the trademark note in §11 in mind either way (nothing that could be confused with Duolingo itself).
