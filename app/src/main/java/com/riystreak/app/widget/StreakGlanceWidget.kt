package com.riystreak.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import com.riystreak.app.RiyStreakApplication
import java.time.LocalDate

class StreakGlanceWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent(context)
            }
        }
    }

    @Composable
    private fun WidgetContent(context: Context) {
        val prefs = androidx.glance.currentState<Preferences>()
        val streakIdKey = longPreferencesKey("streak_id")
        val streakId = prefs[streakIdKey] ?: -1L

        val app = context.applicationContext as RiyStreakApplication
        val streakDao = app.database.streakDao()

        val streak = if (streakId != -1L) {
            kotlinx.coroutines.runBlocking {
                val raw = streakDao.getStreakById(streakId)
                raw?.let { app.streakRepository.evaluateSingleStreak(it, LocalDate.now()) }
            }
        } else null

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp)
                .background(GlanceTheme.colors.surface),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (streak == null) {
                Text(
                    text = "Tap to configure widget",
                    style = TextStyle(fontSize = 14.sp)
                )
            } else {
                Text(
                    text = streak.title,
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🔥 ${streak.currentStreakCount}",
                        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        text = "❄️ ${streak.freezesAvailable}/2",
                        style = TextStyle(fontSize = 12.sp)
                    )
                }
                Spacer(modifier = GlanceModifier.height(8.dp))

                val isDone = streak.isCompletedToday()
                Text(
                    text = if (isDone) "Done Today!" else "Mark Done",
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                    modifier = GlanceModifier
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clickable(
                            if (!isDone) {
                                actionRunCallback<CompleteStreakActionCallback>(
                                    actionParametersOf(CompleteStreakActionCallback.PARAM_STREAK_ID to streak.id)
                                )
                            } else actionRunCallback<CompleteStreakActionCallback>()
                        )
                )
            }
        }
    }
}

class CompleteStreakActionCallback : ActionCallback {
    companion object {
        val PARAM_STREAK_ID = ActionParameters.Key<Long>("streak_id_action")
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val streakId = parameters[PARAM_STREAK_ID] ?: return
        val app = context.applicationContext as RiyStreakApplication
        app.streakRepository.markCompleted(streakId, LocalDate.now())
        StreakGlanceWidget().update(context, glanceId)
    }
}

class StreakWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StreakGlanceWidget()
}
