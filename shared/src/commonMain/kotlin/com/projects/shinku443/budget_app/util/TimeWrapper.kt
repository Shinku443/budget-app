package com.projects.shinku443.budget_app.util

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

val today: LocalDate = Clock.System.now()
    .toLocalDateTime(TimeZone.currentSystemDefault())
    .date

// You can derive month/year like this:
val currentMonth = today.month
val currentYear = today.year
