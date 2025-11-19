package com.projects.shinku443.budget_app.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.Instant


/**
 * Wrapper for getting our MonthYear (for tracking date of budgeting)
 */
object TimeWrapper {
    @OptIn(ExperimentalTime::class)
    fun today(): LocalDate = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date

    fun currentYearMonth(): YearMonth {
        val date = today()
        return YearMonth(year = date.year, month = date.month.number)
    }

    @OptIn(ExperimentalTime::class)
    fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()


    fun formatMonthYear(monthYear: YearMonth): String {
        // Example: "2025-11"
        return "${monthYear.year}-${monthYear.month.toString().padStart(2, '0')}"
    }

}

/**
 * Simple multiplatform-safe representation of a year + month.
 */
data class YearMonth(val year: Int, val month: Int) {
    val currentYear = year
        get() = field

    val currentMonth = month
        get() = field

    override fun toString(): String {
        // Pad month with leading zero if needed
        val monthStr = month.toString().padStart(2, '0')
        return "$year-$monthStr"
    }

    companion object {
        fun parse(input: String): YearMonth {
            // Expect format "YYYY-MM"
            val parts = input.split("-")
            require(parts.size == 2) { "Invalid YearMonth format: $input" }
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            return YearMonth(year, month)
        }
    }
}
