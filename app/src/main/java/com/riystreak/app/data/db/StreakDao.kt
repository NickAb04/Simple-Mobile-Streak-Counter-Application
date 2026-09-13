package com.riystreak.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.riystreak.app.data.model.Streak
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {
    @Query("SELECT * FROM streaks WHERE isArchived = 0 ORDER BY id DESC")
    fun getAllActiveStreaksFlow(): Flow<List<Streak>>

    @Query("SELECT * FROM streaks WHERE isArchived = 0")
    suspend fun getAllActiveStreaks(): List<Streak>

    @Query("SELECT * FROM streaks WHERE isArchived = 1 ORDER BY id DESC")
    fun getArchivedStreaksFlow(): Flow<List<Streak>>

    @Query("SELECT * FROM streaks WHERE id = :id")
    fun getStreakByIdFlow(id: Long): Flow<Streak?>

    @Query("SELECT * FROM streaks WHERE id = :id")
    suspend fun getStreakById(id: Long): Streak?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreak(streak: Streak): Long

    @Update
    suspend fun updateStreak(streak: Streak)

    @Delete
    suspend fun deleteStreak(streak: Streak)
}
