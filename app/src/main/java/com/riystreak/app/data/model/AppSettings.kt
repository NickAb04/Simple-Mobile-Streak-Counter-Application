package com.riystreak.app.data.model

import java.time.LocalTime

data class AppSettings(
    val notificationsEnabled: Boolean = true,
    val defaultReminderTime: LocalTime = LocalTime.of(20, 0), // 8:00 PM default
    val hasCompletedOnboarding: Boolean = false,
    val appLockEnabled: Boolean = false
)
