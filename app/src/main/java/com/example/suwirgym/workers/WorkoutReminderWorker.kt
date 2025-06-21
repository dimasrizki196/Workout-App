package com.example.suwirgym.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.suwirgym.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.concurrent.TimeUnit

class WorkoutReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    private val channelId = "workout_reminder_channel"

    override fun doWork(): Result {
        val context = applicationContext
        val title = "Ingat Workout Harian!"
        val message = "Yuk, lakukan workout hari ini untuk menjaga stamina 💪"

        sendReminderNotification(context, title, message)
        saveNotificationToFirestore(title, message)

        // Schedule ulang besok
        val hour = inputData.getInt("hour", 6)
        val minute = inputData.getInt("minute", 0)
        scheduleNextReminder(context, hour, minute)

        return Result.success()
    }

    private fun sendReminderNotification(context: Context, title: String, message: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pengingat Workout Harian",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .build()

        manager.notify((1000..9999).random(), notification)
    }

    private fun saveNotificationToFirestore(title: String, message: String) {
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val notif = hashMapOf(
            "title" to title,
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users").document(userId)
            .collection("notifications")
            .add(notif)
    }

    private fun scheduleNextReminder(context: Context, hour: Int, minute: Int) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, 1)
        }

        val delay = target.timeInMillis - now.timeInMillis

        val workRequest = OneTimeWorkRequestBuilder<WorkoutReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("hour" to hour, "minute" to minute))
            .addTag("daily_reminder_${hour}_${minute}")
            .addTag("daily_reminder")
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
