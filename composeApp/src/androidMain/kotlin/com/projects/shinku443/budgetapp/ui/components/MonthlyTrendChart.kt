package com.projects.shinku443.budgetapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.projects.shinku443.budgetapp.model.Transaction
import com.projects.shinku443.budgetapp.model.CategoryType
import io.github.koalaplot.core.line.LinePlot2
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.*

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun MonthlyTrendChart(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    if (transactions.isEmpty()) {
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("No transaction data available", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    // Group transactions by day and calculate daily totals
    val expenseData = transactions
        .filter { it.type == CategoryType.EXPENSE }
        .groupBy { transaction ->
            try {
                val dateParts = transaction.date.split("-")
                if (dateParts.size >= 3) {
                    dateParts[2].toIntOrNull() ?: 0
                } else {
                    0
                }
            } catch (e: Exception) {
                0
            }
        }
        .filterKeys { it > 0 }
        .mapValues { (_, txs) -> txs.sumOf { it.amount }.toDouble() }
        .toSortedMap()

    val incomeData = transactions
        .filter { it.type == CategoryType.INCOME }
        .groupBy { transaction ->
            try {
                val dateParts = transaction.date.split("-")
                if (dateParts.size >= 3) {
                    dateParts[2].toIntOrNull() ?: 0
                } else {
                    0
                }
            } catch (e: Exception) {
                0
            }
        }
        .filterKeys { it > 0 }
        .mapValues { (_, txs) -> txs.sumOf { it.amount }.toDouble() }
        .toSortedMap()

    // Create series for the chart
    val allDays = (expenseData.keys + incomeData.keys).sorted()
    
    if (allDays.isEmpty()) {
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("No valid date data", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Daily Spending Trend",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Create data points for expenses
        val expensePoints = remember(allDays, expenseData) {
            allDays.map { day ->
                DefaultPoint(day.toFloat(), (expenseData[day] ?: 0.0).toFloat())
            }
        }
        
        // Create data points for income
        val incomePoints = remember(allDays, incomeData) {
            allDays.map { day ->
                DefaultPoint(day.toFloat(), (incomeData[day] ?: 0.0).toFloat())
            }
        }
        
        // Combine all points for auto-scaling
        val allPoints = remember(expensePoints, incomePoints) {
            expensePoints + incomePoints
        }

        if (allPoints.isNotEmpty()) {
            XYGraph(
                modifier = Modifier
                    .height(300.dp)
                    .fillMaxWidth(),
                xAxisModel = rememberFloatLinearAxisModel(allPoints.autoScaleXRange()),
                yAxisModel = rememberFloatLinearAxisModel(allPoints.autoScaleYRange()),
                xAxisLabels = { value -> Text("Day ${value.toInt()}") },
                yAxisLabels = { value -> Text("$${String.format("%.0f", value)}") },
                xAxisTitle = { Text("Day of Month") },
                yAxisTitle = { Text("Amount ($)") },
            ) {
                // Plot expenses line
                LinePlot2(data = expensePoints)
                // Plot income line
                LinePlot2(data = incomePoints)
            }
        } else {
            Text("No data to display", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
