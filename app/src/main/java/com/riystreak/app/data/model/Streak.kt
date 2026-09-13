package com.riystreak.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "streaks")
data class Streak(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val startDate: LocalDate = LocalDate.now(),
    val currentStreakCount: Int = 0,
    val longestStreakCount: Int = 0,
    val lastCompletedDate: LocalDate? = null,
    val freezesAvailable: Int = 0, // Capped at 2
    val consecutiveDaysForFreeze: Int = 0, // Tracks 7-day milestone progress
    val reminderTimeOverride: LocalTime? = null,
    val isArchived: Boolean = false
) {
    fun isCompletedOn(date: LocalDate): Boolean {
        return lastCompletedDate == date
    }

    fun isCompletedToday(): Boolean {
        return isCompletedOn(LocalDate.now())
    }
}
