package com.riystreak.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.riystreak.app.data.model.DayLog
import com.riystreak.app.data.model.DayStatus
import com.riystreak.app.ui.theme.FlameOrange
import com.riystreak.app.ui.theme.FreezeCyan
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun StreakCalendarGrid(
    currentMonth: YearMonth,
    dayLogs: List<DayLog>,
    modifier: Modifier = Modifier
) {
    val logMap = dayLogs.associateBy { it.date }
    val firstDayOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Day of week headers
            val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Days Grid
            val totalSlots = startDayOfWeek + daysInMonth
            val rows = (totalSlots + 6) / 7

            for (row in 0 until rows) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (col in 0 until 7) {
                        val dayNumber = row * 7 + col - startDayOfWeek + 1
                        if (dayNumber in 1..daysInMonth) {
                            val date = currentMonth.atDay(dayNumber)
                            val dayLog = logMap[date]
                            CalendarDayCell(
                                dayNumber = dayNumber,
                                dayStatus = dayLog?.status,
                                isToday = date == LocalDate.now(),
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    dayNumber: Int,
    dayStatus: DayStatus?,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(2.dp)
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(
                when {
                    dayStatus == DayStatus.COMPLETED -> FlameOrange.copy(alpha = 0.2f)
                    dayStatus == DayStatus.FROZEN -> FreezeCyan.copy(alpha = 0.2f)
                    dayStatus == DayStatus.MISSED -> Color.Red.copy(alpha = 0.15f)
                    isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else -> Color.Transparent
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$dayNumber",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = when (dayStatus) {
                    DayStatus.COMPLETED -> FlameOrange
                    DayStatus.FROZEN -> FreezeCyan
                    DayStatus.MISSED -> Color.Red
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = if (isToday) 1f else 0.7f)
                }
            )

            if (dayStatus != null) {
                Icon(
                    imageVector = when (dayStatus) {
                        DayStatus.COMPLETED -> Icons.Default.LocalFireDepartment
                        DayStatus.FROZEN -> Icons.Default.AcUnit
                        DayStatus.MISSED -> Icons.Default.Close
                    },
                    contentDescription = dayStatus.name,
                    tint = when (dayStatus) {
                        DayStatus.COMPLETED -> FlameOrange
                        DayStatus.FROZEN -> FreezeCyan
                        DayStatus.MISSED -> Color.Red
                    },
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}
