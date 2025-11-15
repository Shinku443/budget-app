package com.projects.shinku443.budget_app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import java.time.YearMonth

@Composable
fun MonthSelector(currentMonth: YearMonth, onMonthSelected: (YearMonth) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val months = (0..11).map { YearMonth.now().minusMonths(it.toLong()) }

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(currentMonth.toString())
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            months.forEach { month ->
                DropdownMenuItem(
                    text = { Text(month.toString()) },
                    onClick = {
                        expanded = false
                        onMonthSelected(month)
                    }
                )
            }
        }
    }
}
