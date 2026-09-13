package com.riystreak.app.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.riystreak.app.MainActivity
import com.riystreak.app.R
import com.riystreak.app.data.model.Streak
import java.time.LocalDate

object NotificationHelper {

    const val CHANNEL_ID = "riystreak_digested_reminders"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "RiyStreak Daily Reminders"
            val descriptionText = "Digested daily reminders for open habit streaks"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    fun sendDigestedReminder(context: Context, openStreaks: List<Streak>) {
        if (openStreaks.isEmpty()) return
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val count = openStreaks.size
        val streakTitles = openStreaks.joinToString(", ") { it.title }
        val titleText = if (count == 1) "1 habit open today!" else "$count habits open today!"
        val contentText = "Keep your streak alive: $streakTitles"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titleText)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Attach quick-action buttons for up to 3 open streaks
        openStreaks.take(3).forEachIndexed { index, streak ->
            val actionIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_MARK_COMPLETE
                putExtra(NotificationActionReceiver.EXTRA_STREAK_ID, streak.id)
            }
            val actionPendingIntent = PendingIntent.getBroadcast(
                context,
                streak.id.toInt() + 2000,
                actionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                android.R.drawable.ic_menu_add,
                "Done: ${streak.title}",
                actionPendingIntent
            )
        }

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }
}
