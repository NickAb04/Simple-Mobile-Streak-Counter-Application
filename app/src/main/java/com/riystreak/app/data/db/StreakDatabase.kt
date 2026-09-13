package com.riystreak.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.riystreak.app.data.model.DayLog
import com.riystreak.app.data.model.Streak

@Database(
    entities = [Streak::class, DayLog::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class StreakDatabase : RoomDatabase() {
    abstract fun streakDao(): StreakDao
    abstract fun dayLogDao(): DayLogDao

    companion object {
        @Volatile
        private var INSTANCE: StreakDatabase? = null

        fun getInstance(context: Context): StreakDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StreakDatabase::class.java,
                    "riystreak_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
