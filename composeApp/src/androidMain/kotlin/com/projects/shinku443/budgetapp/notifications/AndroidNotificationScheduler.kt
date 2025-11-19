package com.projects.shinku443.budgetapp.notifications

import android.content.Context

class AndroidNotificationScheduler(private val context: Context) : NotificationScheduler {
    override fun scheduleDailyReminder() {
        scheduleDailyReminder(context) // your WorkManager helper
    }
    override fun cancelDailyReminder() {
        cancelDailyReminder(context)
    }
}
