package com.projects.shinku443.budget_app.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.projects.shinku443.budget_app.util.YearMonth

@Composable
fun MonthSelector(
    currentMonth: YearMonth,
    onMonthSelected: (YearMonth) -> Unit
) {
    Row(modifier = Modifier.padding(8.dp)) {
        Button(onClick = {
            val prevMonth = if (currentMonth.month == 1) {
                YearMonth(currentMonth.year - 1, 12)
            } else {
                YearMonth(currentMonth.year, currentMonth.month - 1)
            }
            onMonthSelected(prevMonth)
        }) {
            Text("<")
        }

        Text(
            text = prettyFormat(currentMonth),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Button(onClick = {
            val nextMonth = if (currentMonth.month == 12) {
                YearMonth(currentMonth.year + 1, 1)
            } else {
                YearMonth(currentMonth.year, currentMonth.month + 1)
            }
            onMonthSelected(nextMonth)
        }) {
            Text(">")
        }
    }
}

private fun prettyFormat(yearMonth: YearMonth): String {
    val monthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    val monthName = monthNames[yearMonth.month - 1]
    return "$monthName ${yearMonth.year}"
}
