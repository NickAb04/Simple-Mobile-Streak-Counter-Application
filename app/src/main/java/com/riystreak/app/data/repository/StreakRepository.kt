package com.riystreak.app.data.repository

import com.riystreak.app.data.db.DayLogDao
import com.riystreak.app.data.db.StreakDao
import com.riystreak.app.data.model.DayLog
import com.riystreak.app.data.model.DayStatus
import com.riystreak.app.data.model.Streak
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StreakRepository(
    private val streakDao: StreakDao,
    private val dayLogDao: DayLogDao
) {
    val activeStreaksFlow: Flow<List<Streak>> = streakDao.getAllActiveStreaksFlow().map { streaks ->
        evaluateAndSortStreaks(streaks, LocalDate.now())
    }

    val archivedStreaksFlow: Flow<List<Streak>> = streakDao.getArchivedStreaksFlow()

    fun getStreakByIdFlow(id: Long): Flow<Streak?> {
        return streakDao.getStreakByIdFlow(id).map { streak ->
            streak?.let { evaluateSingleStreak(it, LocalDate.now()) }
        }
    }

    suspend fun getStreakById(id: Long): Streak? {
        val streak = streakDao.getStreakById(id) ?: return null
        return evaluateSingleStreak(streak, LocalDate.now())
    }

    suspend fun getEvaluatedActiveStreaks(today: LocalDate = LocalDate.now()): List<Streak> {
        val raw = streakDao.getAllActiveStreaks()
        return evaluateAndSortStreaks(raw, today)
    }

    suspend fun insertStreak(streak: Streak): Long {
        return streakDao.insertStreak(streak)
    }

    suspend fun updateStreak(streak: Streak) {
        streakDao.updateStreak(streak)
    }

    suspend fun archiveStreak(streakId: Long, isArchived: Boolean) {
        val streak = streakDao.getStreakById(streakId) ?: return
        streakDao.updateStreak(streak.copy(isArchived = isArchived))
    }

    suspend fun deleteStreak(streakId: Long) {
        val streak = streakDao.getStreakById(streakId) ?: return
        streakDao.deleteStreak(streak)
    }

    suspend fun markCompleted(streakId: Long, completionDate: LocalDate = LocalDate.now()): Streak? {
        val rawStreak = streakDao.getStreakById(streakId) ?: return null
        val evaluatedStreak = evaluateSingleStreak(rawStreak, completionDate)

        if (evaluatedStreak.lastCompletedDate == completionDate) {
            return evaluatedStreak
        }

        val newStreakCount = evaluatedStreak.currentStreakCount + 1
        val newLongestCount = maxOf(evaluatedStreak.longestStreakCount, newStreakCount)
        var newFreezes = evaluatedStreak.freezesAvailable
        var newMilestone = evaluatedStreak.consecutiveDaysForFreeze + 1

        if (newMilestone >= 7) {
            if (newFreezes < 2) {
                newFreezes += 1
            }
            newMilestone = 0
        }

        val updatedStreak = evaluatedStreak.copy(
            currentStreakCount = newStreakCount,
            longestStreakCount = newLongestCount,
            lastCompletedDate = completionDate,
            freezesAvailable = newFreezes,
            consecutiveDaysForFreeze = newMilestone
        )

        streakDao.updateStreak(updatedStreak)

        val dayLog = DayLog(
            streakId = streakId,
            date = completionDate,
            status = DayStatus.COMPLETED
        )
        dayLogDao.insertOrUpdateLog(dayLog)

        return updatedStreak
    }

    fun getLogsForStreakFlow(streakId: Long): Flow<List<DayLog>> {
        return dayLogDao.getLogsForStreakFlow(streakId)
    }

    suspend fun getLogsForStreak(streakId: Long): List<DayLog> {
        return dayLogDao.getLogsForStreak(streakId)
    }

    suspend fun evaluateSingleStreak(streak: Streak, today: LocalDate = LocalDate.now()): Streak {
        val lastDate = streak.lastCompletedDate ?: return streak

        if (lastDate == today || lastDate == today.minusDays(1)) {
            return streak
        }

        val daysMissed = ChronoUnit.DAYS.between(lastDate, today).toInt() - 1
        if (daysMissed <= 0) return streak

        var currentStreakCount = streak.currentStreakCount
        var freezesLeft = streak.freezesAvailable
        var milestoneProgress = streak.consecutiveDaysForFreeze
        val logsToInsert = mutableListOf<DayLog>()
        var stateChanged = false

        for (i in 1..daysMissed) {
            val missedDate = lastDate.plusDays(i.toLong())
            val existingLog = dayLogDao.getLogForStreakAndDate(streak.id, missedDate)
            if (existingLog != null) continue

            stateChanged = true
            if (freezesLeft > 0) {
                freezesLeft -= 1
                milestoneProgress = 0
                logsToInsert.add(DayLog(streakId = streak.id, date = missedDate, status = DayStatus.FROZEN))
            } else {
                currentStreakCount = 0
                milestoneProgress = 0
                logsToInsert.add(DayLog(streakId = streak.id, date = missedDate, status = DayStatus.MISSED))
            }
        }

        if (logsToInsert.isNotEmpty()) {
            dayLogDao.insertOrUpdateLogs(logsToInsert)
        }

        val updatedStreak = streak.copy(
            currentStreakCount = currentStreakCount,
            freezesAvailable = freezesLeft,
            consecutiveDaysForFreeze = milestoneProgress
        )

        if (stateChanged) {
            streakDao.updateStreak(updatedStreak)
        }

        return updatedStreak
    }

    private suspend fun evaluateAndSortStreaks(streaks: List<Streak>, today: LocalDate): List<Streak> {
        val evaluated = streaks.map { evaluateSingleStreak(it, today) }
        return evaluated.sortedWith(
            compareBy<Streak> { it.isCompletedOn(today) }
                .thenBy { it.freezesAvailable }
                .thenByDescending { it.currentStreakCount }
        )
    }

    companion object {
        fun computeEvaluatedStreakPure(
            streak: Streak,
            today: LocalDate,
            existingLogs: List<DayLog>
        ): Pair<Streak, List<DayLog>> {
            val lastDate = streak.lastCompletedDate ?: return Pair(streak, emptyList())
            if (lastDate == today || lastDate == today.minusDays(1)) {
                return Pair(streak, emptyList())
            }

            val daysMissed = ChronoUnit.DAYS.between(lastDate, today).toInt() - 1
            if (daysMissed <= 0) return Pair(streak, emptyList())

            var currentStreakCount = streak.currentStreakCount
            var freezesLeft = streak.freezesAvailable
            var milestoneProgress = streak.consecutiveDaysForFreeze
            val newLogs = mutableListOf<DayLog>()
            val loggedDates = existingLogs.map { it.date }.toSet()

            for (i in 1..daysMissed) {
                val missedDate = lastDate.plusDays(i.toLong())
                if (loggedDates.contains(missedDate)) continue

                if (freezesLeft > 0) {
                    freezesLeft -= 1
                    milestoneProgress = 0
                    newLogs.add(DayLog(streakId = streak.id, date = missedDate, status = DayStatus.FROZEN))
                } else {
                    currentStreakCount = 0
                    milestoneProgress = 0
                    newLogs.add(DayLog(streakId = streak.id, date = missedDate, status = DayStatus.MISSED))
                }
            }

            val updatedStreak = streak.copy(
                currentStreakCount = currentStreakCount,
                freezesAvailable = freezesLeft,
                consecutiveDaysForFreeze = milestoneProgress
            )

            return Pair(updatedStreak, newLogs)
        }
    }
}
