package com.example.suwirgym.util

import android.content.Context
import androidx.work.*
import com.example.suwirgym.workers.WorkoutReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

fun scheduleDailyWorkoutReminder(context: Context) {
    val times = listOf(
        6 to 0,   // 06:00
        10 to 0,  // 10:00
        15 to 0,  // 15:00
        20 to 0   // 20:00
    )

    val now = Calendar.getInstance()

    times.forEach { (hour, minute) ->
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }

        val delay = target.timeInMillis - now.timeInMillis

        val request = OneTimeWorkRequestBuilder<WorkoutReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("hour" to hour, "minute" to minute))
            .addTag("daily_reminder_${hour}_${minute}")
            .addTag("daily_reminder")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "reminder_${hour}_${minute}",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
