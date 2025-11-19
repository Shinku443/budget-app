package com.projects.shinku443.budget_app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.projects.shinku443.budget_app.viewmodel.BudgetViewModel
import io.github.koalaplot.core.xygraph.DefaultPoint
import io.github.koalaplot.core.xygraph.XYGraph
import io.github.koalaplot.core.xygraph.autoScaleXRange
import io.github.koalaplot.core.xygraph.autoScaleYRange
import org.koin.androidx.compose.koinViewModel

class ReportsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: BudgetViewModel = koinViewModel()

        val income by viewModel.income.collectAsState()
        val expense by viewModel.expense.collectAsState()
        val net by viewModel.net.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Reports") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
        }
    }
}
