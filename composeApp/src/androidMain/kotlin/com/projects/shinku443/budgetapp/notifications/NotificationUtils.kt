package com.projects.shinku443.budgetapp.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

private const val DAILY_REMINDER_WORK = "daily_reminder"

/**
 * Schedules a daily reminder at the specified time (24h schedule).
 * Default: 9:00 AM local time.
 */
fun scheduleDailyReminder(
    context: Context,
    hourOfDay: Int = 9,
    minute: Int = 0
) {
    val initialDelayMs = calculateDelayUntilNext(hourOfDay, minute)

    val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        DAILY_REMINDER_WORK,
        ExistingPeriodicWorkPolicy.REPLACE,
        request
    )
}

fun scheduleTestReminder(context: Context) {
    val request = OneTimeWorkRequestBuilder<ReminderWorker>()
        .setInitialDelay(10, TimeUnit.SECONDS)
        .build()

    WorkManager.getInstance(context).enqueue(request)
}


fun cancelDailyReminder(context: Context) {
    WorkManager.getInstance(context).cancelUniqueWork(DAILY_REMINDER_WORK)
}

/**
 * Calculates delay from "now" until the next occurrence of [hourOfDay]:[minute] local time.
 * If the time today has already passed, schedules for tomorrow.
 */
fun calculateDelayUntilNext(hourOfDay: Int, minute: Int, debug: Boolean = false): Long {
    if (debug) return 10_000L // 10 seconds
    val zone = ZoneId.systemDefault()
    val now = ZonedDateTime.now(zone)

    val targetToday = LocalDate.now(zone).atTime(LocalTime.of(hourOfDay, minute)).atZone(zone)
    val target = if (now.isBefore(targetToday)) targetToday else {
        LocalDate.now(zone).plusDays(1).atTime(LocalTime.of(hourOfDay, minute)).atZone(zone)
    }

    return java.time.Duration.between(now, target).toMillis()
}
