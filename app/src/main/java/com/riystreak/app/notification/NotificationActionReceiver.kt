package com.riystreak.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.riystreak.app.RiyStreakApplication
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MARK_COMPLETE = "com.riystreak.app.ACTION_MARK_COMPLETE"
        const val EXTRA_STREAK_ID = "extra_streak_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_MARK_COMPLETE) {
            val streakId = intent.getLongExtra(EXTRA_STREAK_ID, -1L)
            if (streakId != -1L) {
                val app = context.applicationContext as RiyStreakApplication
                CoroutineScope(Dispatchers.IO).launch {
                    app.streakRepository.markCompleted(streakId, LocalDate.now())
                }
            }
        }
    }
}
