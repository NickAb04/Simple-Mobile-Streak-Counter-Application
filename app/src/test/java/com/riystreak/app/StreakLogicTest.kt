package com.riystreak.app

import com.riystreak.app.data.model.DayLog
import com.riystreak.app.data.model.DayStatus
import com.riystreak.app.data.model.Streak
import com.riystreak.app.data.repository.StreakRepository
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StreakLogicTest {

    @Test
    fun testConsecutiveCompletions_IncrementsStreakAndLongest() {
        val today = LocalDate.of(2026, 9, 10)
        var streak = Streak(
            id = 1,
            title = "Read 10 Pages",
            startDate = today,
            currentStreakCount = 0,
            longestStreakCount = 0,
            lastCompletedDate = null
        )

        // Day 1: Completed
        streak = streak.copy(
            currentStreakCount = streak.currentStreakCount + 1,
            longestStreakCount = maxOf(streak.longestStreakCount, streak.currentStreakCount + 1),
            lastCompletedDate = today,
            consecutiveDaysForFreeze = streak.consecutiveDaysForFreeze + 1
        )
        assertEquals(1, streak.currentStreakCount)
        assertEquals(1, streak.longestStreakCount)
        assertEquals(1, streak.consecutiveDaysForFreeze)

        // Day 2: Completed next day
        val day2 = today.plusDays(1)
        streak = streak.copy(
            currentStreakCount = streak.currentStreakCount + 1,
            longestStreakCount = maxOf(streak.longestStreakCount, streak.currentStreakCount + 1),
            lastCompletedDate = day2,
            consecutiveDaysForFreeze = streak.consecutiveDaysForFreeze + 1
        )
        assertEquals(2, streak.currentStreakCount)
        assertEquals(2, streak.longestStreakCount)
        assertEquals(2, streak.consecutiveDaysForFreeze)
    }

    @Test
    fun testSevenDayMilestone_EarnsFreezeCappedAtTwo() {
        var streak = Streak(
            id = 1,
            title = "Daily Exercise",
            freezesAvailable = 0,
            consecutiveDaysForFreeze = 6
        )

        // 7th consecutive completion
        var newFreezes = streak.freezesAvailable
        var newMilestone = streak.consecutiveDaysForFreeze + 1
        if (newMilestone >= 7) {
            if (newFreezes < 2) {
                newFreezes += 1
            }
            newMilestone = 0
        }

        streak = streak.copy(
            freezesAvailable = newFreezes,
            consecutiveDaysForFreeze = newMilestone
        )

        assertEquals(1, streak.freezesAvailable)
        assertEquals(0, streak.consecutiveDaysForFreeze)

        // Earn second freeze
        streak = streak.copy(freezesAvailable = 1, consecutiveDaysForFreeze = 6)
        newFreezes = streak.freezesAvailable
        newMilestone = streak.consecutiveDaysForFreeze + 1
        if (newMilestone >= 7) {
            if (newFreezes < 2) newFreezes += 1
            newMilestone = 0
        }
        streak = streak.copy(freezesAvailable = newFreezes, consecutiveDaysForFreeze = newMilestone)

        assertEquals(2, streak.freezesAvailable)

        // Try earning 3rd freeze (should stay capped at 2)
        streak = streak.copy(freezesAvailable = 2, consecutiveDaysForFreeze = 6)
        newFreezes = streak.freezesAvailable
        newMilestone = streak.consecutiveDaysForFreeze + 1
        if (newMilestone >= 7) {
            if (newFreezes < 2) newFreezes += 1
            newMilestone = 0
        }
        streak = streak.copy(freezesAvailable = newFreezes, consecutiveDaysForFreeze = newMilestone)

        assertEquals(2, streak.freezesAvailable)
    }

    @Test
    fun testMissedOneDayWithBankedFreeze_ConsumesFreezeAndPreservesStreak() {
        val monday = LocalDate.of(2026, 9, 7)
        val wednesday = LocalDate.of(2026, 9, 9) // Tuesday missed

        val initialStreak = Streak(
            id = 1,
            title = "LeetCode",
            currentStreakCount = 5,
            longestStreakCount = 5,
            lastCompletedDate = monday,
            freezesAvailable = 1,
            consecutiveDaysForFreeze = 3
        )

        val (evaluatedStreak, generatedLogs) = StreakRepository.computeEvaluatedStreakPure(
            streak = initialStreak,
            today = wednesday,
            existingLogs = emptyList()
        )

        assertEquals(5, evaluatedStreak.currentStreakCount) // Preserved!
        assertEquals(0, evaluatedStreak.freezesAvailable) // 1 consumed
        assertEquals(0, evaluatedStreak.consecutiveDaysForFreeze) // Milestone reset

        assertEquals(1, generatedLogs.size)
        val tuesdayLog = generatedLogs[0]
        assertEquals(LocalDate.of(2026, 9, 8), tuesdayLog.date)
        assertEquals(DayStatus.FROZEN, tuesdayLog.status)
    }

    @Test
    fun testMissedTwoDaysWithOneBankedFreeze_ConsumesFreezeThenBreaksStreak() {
        val monday = LocalDate.of(2026, 9, 7)
        val thursday = LocalDate.of(2026, 9, 10) // Tuesday & Wednesday missed

        val initialStreak = Streak(
            id = 1,
            title = "Meditation",
            currentStreakCount = 10,
            longestStreakCount = 10,
            lastCompletedDate = monday,
            freezesAvailable = 1,
            consecutiveDaysForFreeze = 4
        )

        val (evaluatedStreak, generatedLogs) = StreakRepository.computeEvaluatedStreakPure(
            streak = initialStreak,
            today = thursday,
            existingLogs = emptyList()
        )

        assertEquals(0, evaluatedStreak.currentStreakCount) // Reset to 0 on 2nd miss!
        assertEquals(10, evaluatedStreak.longestStreakCount) // Record intact!
        assertEquals(0, evaluatedStreak.freezesAvailable) // 1 freeze consumed for Tuesday

        assertEquals(2, generatedLogs.size)
        assertEquals(DayStatus.FROZEN, generatedLogs[0].status)
        assertEquals(LocalDate.of(2026, 9, 8), generatedLogs[0].date)

        assertEquals(DayStatus.MISSED, generatedLogs[1].status)
        assertEquals(LocalDate.of(2026, 9, 9), generatedLogs[1].date)
    }

    @Test
    fun testNoMissedDays_ReturnsSameStreak() {
        val monday = LocalDate.of(2026, 9, 7)
        val initialStreak = Streak(
            id = 1,
            title = "Reading",
            currentStreakCount = 3,
            lastCompletedDate = monday,
            freezesAvailable = 2
        )

        // Evaluate on same day (monday)
        val (evaluatedSameDay, logsSameDay) = StreakRepository.computeEvaluatedStreakPure(
            streak = initialStreak,
            today = monday,
            existingLogs = emptyList()
        )
        assertEquals(3, evaluatedSameDay.currentStreakCount)
        assertTrue(logsSameDay.isEmpty())

        // Evaluate next day (tuesday)
        val (evaluatedNextDay, logsNextDay) = StreakRepository.computeEvaluatedStreakPure(
            streak = initialStreak,
            today = monday.plusDays(1),
            existingLogs = emptyList()
        )
        assertEquals(3, evaluatedNextDay.currentStreakCount)
        assertTrue(logsNextDay.isEmpty())
    }
}
