package com.projects.shinku443.budget_app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import co.touchlab.kermit.Logger
import com.projects.shinku443.budget_app.ui.components.TransactionList
import com.projects.shinku443.budget_app.util.YearMonth
import com.projects.shinku443.budget_app.viewmodel.BudgetViewModel
import io.github.koalaplot.core.pie.PieChart
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import kotlinx.coroutines.launch
import java.time.Month
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalKoalaPlotApi::class)
@Composable
fun DashboardScreen(
    viewModel: BudgetViewModel,
    onAddTransaction: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val monthlyExpenses by viewModel.monthlyExpenses.collectAsState()
    val currentMonth = viewModel.currentMonth

    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()

    // Month/year picker dialog state
    var showPicker by remember { mutableStateOf(false) }
    var selectedYearMonth by remember { mutableStateOf(currentMonth) }

    // Load current month when screen first appears
    LaunchedEffect(Unit) {
        viewModel.loadTransactions(currentMonth)
    }

    val onRefresh: () -> Unit = {
        Logger.e("Test")
        coroutineScope.launch {
            isRefreshing = true
            viewModel.loadTransactions(currentMonth)
            // wait until loadTransactions finishes
            isRefreshing = false
        }
    }

    Logger.e { "monthly expenses: $monthlyExpenses" }
    val pieChartData = remember(monthlyExpenses) {
        monthlyExpenses.groupBy { it.categoryType.name }
            .mapValues { (_, txs) -> txs.sumOf { it.amount }.toFloat() }
    }

    Scaffold(
        modifier = Modifier.pullToRefresh(
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = onRefresh
        ),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "${
                            Month.of(selectedYearMonth.month).getDisplayName(TextStyle.FULL, Locale.getDefault())
                        } ${selectedYearMonth.year}"
                    )
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { showPicker = true }) {
                        Icon(Icons.Filled.CalendarToday, contentDescription = "Pick Month")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTransaction,
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                text = { Text("Add Transaction") }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                   ,
                text = "Total Expenses",
                textAlign = TextAlign.Center
            )
            if (pieChartData.isNotEmpty()) {
                PieChart(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    values = pieChartData.values.toList(),
                    label = { index ->
                        Text(pieChartData.keys.elementAt(index).capitalize())
                    }
                )
            }
            if (transactions.isEmpty()) {
                Text("No transactions yet", modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                TransactionList(
                    transactions = transactions,
                    onDelete = { tx -> viewModel.deleteTransaction(tx) }
                )
            }
        }
    }

    // Month/year picker dialog
    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text("Select Month & Year") },
            text = {
                Column {
                    // Month dropdown
                    var expandedMonth by remember { mutableStateOf(false) }
                    TextButton(onClick = { expandedMonth = true }) {
                        Text(Month.of(selectedYearMonth.month).getDisplayName(TextStyle.FULL, Locale.getDefault()))
                    }
                    DropdownMenu(expanded = expandedMonth, onDismissRequest = { expandedMonth = false }) {
                        Month.values().forEach { m ->
                            DropdownMenuItem(
                                text = { Text(m.getDisplayName(TextStyle.FULL, Locale.getDefault())) },
                                onClick = {
                                    selectedYearMonth = YearMonth(selectedYearMonth.year, m.value)
                                    expandedMonth = false
                                }
                            )
                        }
                    }

                    // Year input
                    var yearText by remember { mutableStateOf(selectedYearMonth.year.toString()) }
                    OutlinedTextField(
                        value = yearText,
                        onValueChange = { yearText = it },
                        label = { Text("Year") }
                    )
                    selectedYearMonth = selectedYearMonth.copy(year = yearText.toIntOrNull() ?: selectedYearMonth.year)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.loadTransactions(selectedYearMonth)
                    showPicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
