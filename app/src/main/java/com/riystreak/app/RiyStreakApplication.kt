package com.riystreak.app

import android.app.Application
import com.riystreak.app.data.db.StreakDatabase
import com.riystreak.app.data.repository.AppSettingsRepository
import com.riystreak.app.data.repository.StreakRepository

class RiyStreakApplication : Application() {

    lateinit var database: StreakDatabase
        private set

    lateinit var streakRepository: StreakRepository
        private set

    lateinit var appSettingsRepository: AppSettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = StreakDatabase.getInstance(this)
        streakRepository = StreakRepository(database.streakDao(), database.dayLogDao())
        appSettingsRepository = AppSettingsRepository(this)
    }
}
