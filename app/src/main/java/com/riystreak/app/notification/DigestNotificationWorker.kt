package com.riystreak.app.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.riystreak.app.RiyStreakApplication
import java.time.LocalDate

class DigestNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val app = applicationContext as RiyStreakApplication
        val repository = app.streakRepository
        val today = LocalDate.now()

        val activeStreaks = repository.getEvaluatedActiveStreaks(today)
        val openStreaks = activeStreaks.filter { !it.isCompletedOn(today) }

        if (openStreaks.isNotEmpty()) {
            NotificationHelper.sendDigestedReminder(applicationContext, openStreaks)
        }

        return Result.success()
    }
}
