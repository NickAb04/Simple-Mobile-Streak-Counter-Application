package com.riystreak.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.riystreak.app.data.model.DayLog
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface DayLogDao {
    @Query("SELECT * FROM day_logs WHERE streakId = :streakId ORDER BY date DESC")
    fun getLogsForStreakFlow(streakId: Long): Flow<List<DayLog>>

    @Query("SELECT * FROM day_logs WHERE streakId = :streakId ORDER BY date DESC")
    suspend fun getLogsForStreak(streakId: Long): List<DayLog>

    @Query("SELECT * FROM day_logs WHERE streakId = :streakId AND date = :date LIMIT 1")
    suspend fun getLogForStreakAndDate(streakId: Long, date: LocalDate): DayLog?

    @Query("SELECT * FROM day_logs WHERE streakId = :streakId AND date >= :startDate AND date <= :endDate")
    suspend fun getLogsForStreakInRange(streakId: Long, startDate: LocalDate, endDate: LocalDate): List<DayLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(dayLog: DayLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLogs(dayLogs: List<DayLog>)
}
