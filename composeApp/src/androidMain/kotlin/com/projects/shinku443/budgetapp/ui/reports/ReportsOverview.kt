package com.projects.shinku443.budgetapp.ui.reports

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import co.touchlab.kermit.Logger
import com.projects.shinku443.budgetapp.network.AiSuggestionService
import com.projects.shinku443.budgetapp.network.AiSuggestionServiceImpl
import com.projects.shinku443.budgetapp.viewmodel.BudgetViewModel
import com.projects.shinku443.budgetapp.viewmodel.TransactionViewModel
import io.github.koalaplot.core.line.LinePlot2
import io.github.koalaplot.core.xygraph.*
import io.ktor.client.HttpClient
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReportsOverview(modifier: Modifier = Modifier) {
    val viewModel: BudgetViewModel = koinViewModel()
    val transactionViewModel: TransactionViewModel = koinViewModel()

    val income by viewModel.income.collectAsState()
    val expense by viewModel.expense.collectAsState()
    val net by viewModel.net.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    LaunchedEffect(transactions) {
        transactionViewModel.loadBudgetTips(transactions)
    }

    Text("Income vs Expenses", style = MaterialTheme.typography.titleMedium)

    // Simple line chart (replace with real monthly data later)
//                val entries = listOf(
//                    LineChartEntry(1f, income.toFloat()),
//                    LineChartEntry(2f, expense.toFloat()),
//                    LineChartEntry(3f, net.toFloat())
//                )
    val data = buildList {
        for (i in 1..10) {
            add(DefaultPoint(i.toFloat(), i * i.toFloat()))
        }
    }

    AiSuggestionsView()

//    XYGraph(
//        xAxisModel = rememberFloatLinearAxisModel(data.autoScaleXRange()),
//        yAxisModel = rememberFloatLinearAxisModel(data.autoScaleYRange()),
//        xAxisLabels = { value -> Text(value.toString()) }, // Composable for labels
//        yAxisLabels = { value -> Text(value.toString()) },
//        xAxisTitle = { Text("X Axis Title") },
//        yAxisTitle = { Text("Y Axis Title") },
//    ) {
//        LinePlot2(
//            data = data
//        )
//    }
//                XYGraph(
//                    rememberLinearAxisModel(data.autoScaleXRange()),
//                    rememberLinearAxisModel(data.autoScaleYRange())
//                ) {
//                    LinePlot(
//                        data,
//                        lineStyle = LineStyle(SolidColor(Color.Blue))
//                    )
//                }
//                LineChart(
//                    data = listOf(entries),
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(200.dp)
//                )

    Text("Income: $income", style = MaterialTheme.typography.bodyMedium)
    Text("Expenses: $expense", style = MaterialTheme.typography.bodyMedium)
    Text("Net: $net", style = MaterialTheme.typography.bodyMedium)
}


@Composable
fun AiSuggestionsView(suggestions: List<String>) {
    Column {
        Text("AI Suggestions", style = MaterialTheme.typography.titleMedium)
        suggestions.forEach { tip ->
            AssistChip(
                onClick = { /* maybe expand */ },
                label = { Text(tip) }
            )
        }
    }
}
@Composable
fun AiSuggestionsView() {
    val transactionViewModel: TransactionViewModel = koinViewModel()
    val suggestions by transactionViewModel.budgetTips.collectAsState()
    Logger.d("suggestions:: $suggestions")
    if (suggestions.isNotEmpty()) {
        Column {
            Text("AI Suggestions", style = MaterialTheme.typography.titleMedium)
            suggestions.forEach { tip ->
                AssistChip(
                    onClick = { /* maybe expand */ },
                    label = { Text(tip) }
                )
            }
        }
    }
}
