# REQUIREMENTS.md

## Functional Requirements
1. **Multiple Independent Streaks**: Users can create, edit, view, archive, and delete independent habit streaks.
2. **Date-Based Streak Counter**: "I did it" increments streak count. Resets or consumes freezes based on actual days elapsed.
3. **Streak Freeze Forgiveness**:
   - 1 freeze consumed per missed day automatically.
   - Earned automatically: +1 freeze per 7 consecutive completions.
   - Cap: max 2 banked freezes per streak.
4. **Local Data Persistence**: 100% on-device local database (Room) & settings (DataStore).
5. **Digested Notifications**: Best-effort daily digested reminder for incomplete habits, missed-day notices, and freeze-exhausted warnings via WorkManager.
6. **Jetpack Glance Widget**: Per-streak home screen widget instance configured via picker.
7. **UI Screens**: Home, Add/Edit, Streak Detail (with calendar/history), Settings, Onboarding, About.
