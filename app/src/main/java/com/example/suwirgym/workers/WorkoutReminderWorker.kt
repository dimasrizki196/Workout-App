package com.example.suwirgym.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.suwirgym.R

class WorkoutReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    private val channelId = "workout_reminder_channel"

    override fun doWork(): Result {
        sendReminderNotification()
        return Result.success()
    }

    private fun sendReminderNotification() {
        val context = applicationContext
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pengingat Workout Harian",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Ingat Workout Harian!")
            .setContentText("Yuk, lakukan workout hari ini untuk menjaga stamina 💪")
            .setSmallIcon(android.R.drawable.stat_notify_more) // or R.drawable.ic_notification
            .setAutoCancel(true)
            .build()

        manager.notify(1001, notification)
    }
}
