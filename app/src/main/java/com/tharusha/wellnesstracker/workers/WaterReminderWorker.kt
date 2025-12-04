package com.tharusha.wellnesstracker.workers

import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.tharusha.wellnesstracker.R

class WaterReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        showNotification()
        return Result.success()
    }

    private fun showNotification() {
        // Create notification channel first (required for Android 8.0+)
        createNotificationChannel()

        // Build the notification - using Android default icon
        val notification = NotificationCompat.Builder(applicationContext, "water_reminder_channel")
            .setContentTitle("💧 Time to Hydrate!")
            .setContentText("Don't forget to drink water and stay hydrated!")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Using Android default icon
            .setAutoCancel(true)
            .build()

        // Check if notifications are enabled before showing
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        if (notificationManager.areNotificationsEnabled()) {
            notificationManager.notify(1, notification)
        }
        // If notifications are disabled, we don't show anything (silent fail)
    }

    private fun createNotificationChannel() {
        // Create notification channel for Android 8.0 (API 26) and higher
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "water_reminder_channel",
                "Water Reminders",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to drink water and stay hydrated"
            }

            val notificationManager = applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}