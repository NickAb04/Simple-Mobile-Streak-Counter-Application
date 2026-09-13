package com.riystreak.app.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AddEditStreak : Screen("add_edit_streak?streakId={streakId}") {
        fun createRoute(streakId: Long? = null): String {
            return if (streakId != null) "add_edit_streak?streakId=$streakId" else "add_edit_streak"
        }
    }
    object StreakDetail : Screen("streak_detail/{streakId}") {
        fun createRoute(streakId: Long): String = "streak_detail/$streakId"
    }
    object Settings : Screen("settings")
    object Onboarding : Screen("onboarding")
    object About : Screen("about")
}
