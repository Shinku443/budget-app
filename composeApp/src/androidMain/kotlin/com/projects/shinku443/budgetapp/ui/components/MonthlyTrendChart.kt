package com.projects.shinku443.budgetapp.ui.components

import androidx.compose.runtime.Composable
import com.projects.shinku443.budgetapp.model.Transaction
import io.github.koalaplot.core.xygraph.XYGraph
//
//@Composable
//fun MonthlyTrendChart(transactions: List<Transaction>) {
//    val dailyTotals = transactions
//        .groupBy { it.date.dayOfMonth }
//        .mapValues { (_, txs) -> txs.sumOf { it.amount }.toFloat() }
//
//    XYGraph(
//        data = dailyTotals.values.toList(),
//        labels = dailyTotals.keys.map { it.toString() }
//    )
//}
