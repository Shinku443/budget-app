package com.projects.shinku443.budgetapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.projects.shinku443.budgetapp.model.CategoryType
import com.projects.shinku443.budgetapp.model.Transaction
import com.projects.shinku443.budgetapp.util.YearMonth
import java.time.Month
import java.time.format.TextStyle
import java.util.*

@Composable
fun MonthlyComparison(
    currentMonthTransactions: List<Transaction>,
    previousMonthTransactions: List<Transaction>,
    currentMonth: YearMonth,
    modifier: Modifier = Modifier
) {
    val currentExpenses = currentMonthTransactions
        .filter { it.type == CategoryType.EXPENSE }
        .sumOf { it.amount }
    
    val previousExpenses = previousMonthTransactions
        .filter { it.type == CategoryType.EXPENSE }
        .sumOf { it.amount }
    
    val currentIncome = currentMonthTransactions
        .filter { it.type == CategoryType.INCOME }
        .sumOf { it.amount }
    
    val previousIncome = previousMonthTransactions
        .filter { it.type == CategoryType.INCOME }
        .sumOf { it.amount }
    
    val expenseChange = if (previousExpenses > 0) {
        ((currentExpenses - previousExpenses) / previousExpenses) * 100
    } else {
        0.0
    }
    
    val incomeChange = if (previousIncome > 0) {
        ((currentIncome - previousIncome) / previousIncome) * 100
    } else {
        0.0
    }
    
    val previousMonthName = if (currentMonth.month == 1) {
        Month.of(12).getDisplayName(TextStyle.FULL, Locale.getDefault())
    } else {
        Month.of(currentMonth.month - 1).getDisplayName(TextStyle.FULL, Locale.getDefault())
    }
    
    val currentMonthName = Month.of(currentMonth.month).getDisplayName(TextStyle.FULL, Locale.getDefault())
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Month-over-Month Comparison",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        // Expenses comparison
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Expenses",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "$currentMonthName ${currentMonth.year}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "$${String.format("%.2f", currentExpenses)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "$previousMonthName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "$${String.format("%.2f", previousExpenses)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                }
                if (previousExpenses > 0) {
                    val changeColor = if (expenseChange > 0) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                    Text(
                        "${if (expenseChange > 0) "+" else ""}${String.format("%.1f", expenseChange)}% change",
                        style = MaterialTheme.typography.bodyMedium,
                        color = changeColor
                    )
                }
            }
        }
        
        // Income comparison
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Income",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "$currentMonthName ${currentMonth.year}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "$${String.format("%.2f", currentIncome)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "$previousMonthName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "$${String.format("%.2f", previousIncome)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    }
                }
                if (previousIncome > 0) {
                    val changeColor = if (incomeChange > 0) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                    Text(
                        "${if (incomeChange > 0) "+" else ""}${String.format("%.1f", incomeChange)}% change",
                        style = MaterialTheme.typography.bodyMedium,
                        color = changeColor
                    )
                }
            }
        }
    }
}

