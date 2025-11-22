package com.projects.shinku443.budgetapp.notifications

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import com.projects.shinku443.budgetapp.R
import androidx.work.WorkerParameters

class ReminderWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override suspend fun doWork(): Result {
        Log.d("ReminderWorker", "Daily reminder fired")
        val notification = NotificationCompat.Builder(applicationContext, "budget_channel")
            .setSmallIcon(R.drawable.ic_notification) // ensure this exists
            .setContentTitle("Budget Reminder")
            .setContentText("Don’t forget to log today’s transactions!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(1, notification)
        return Result.success()
    }
}
