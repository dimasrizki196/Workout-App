package com.example.suwirgym.util

import android.content.Context
import androidx.work.*
import com.example.suwirgym.workers.WorkoutReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

fun scheduleDailyWorkoutReminder(context: Context) {
    val workManager = WorkManager.getInstance(context)

    val workRequest = PeriodicWorkRequestBuilder<WorkoutReminderWorker>(
        24, TimeUnit.HOURS
    )
        .setInitialDelay(1, TimeUnit.SECONDS)
        .addTag("daily_workout_reminder")
        .build()

    workManager.enqueueUniquePeriodicWork(
        "daily_workout_reminder",
        ExistingPeriodicWorkPolicy.KEEP,
        workRequest
    )
}

fun cancelDailyWorkoutReminder(context: Context) {
    WorkManager.getInstance(context).cancelUniqueWork("daily_workout_reminder")
}

private fun calculateInitialDelay(): Long {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 17)  // Set ke jam 7 pagi
        set(Calendar.MINUTE, 17)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (before(now)) {
            add(Calendar.DAY_OF_YEAR, 1) // Jika sudah lewat jam 7 hari ini, target pindah ke besok
        }
    }
    return target.timeInMillis - now.timeInMillis
}
