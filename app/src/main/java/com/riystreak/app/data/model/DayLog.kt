package com.riystreak.app.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

enum class DayStatus {
    COMPLETED,
    FROZEN,
    MISSED
}

@Entity(
    tableName = "day_logs",
    foreignKeys = [
        ForeignKey(
            entity = Streak::class,
            parentColumns = ["id"],
            childColumns = ["streakId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("streakId"),
        Index(value = ["streakId", "date"], unique = true)
    ]
)
data class DayLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val streakId: Long,
    val date: LocalDate,
    val status: DayStatus
)
