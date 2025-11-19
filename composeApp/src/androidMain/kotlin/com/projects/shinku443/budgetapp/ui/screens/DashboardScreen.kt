package com.projects.shinku443.budgetapp.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.projects.shinku443.budgetapp.sync.SyncStatus
import com.projects.shinku443.budgetapp.ui.components.CategoryPieChart
import com.projects.shinku443.budgetapp.ui.components.TransactionList
import com.projects.shinku443.budgetapp.util.YearMonth
import com.projects.shinku443.budgetapp.viewmodel.BudgetViewModel
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.Month
import java.time.format.TextStyle
import java.util.*


class DashboardScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalKoalaPlotApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: BudgetViewModel = koinViewModel()
        val transactions by viewModel.transactions.collectAsState()
        val currentMonth by viewModel.currentMonth.collectAsState()

        var isRefreshing by remember { mutableStateOf(false) }
        val pullToRefreshState = rememberPullToRefreshState()
        val coroutineScope = rememberCoroutineScope()

        var showPicker by remember { mutableStateOf(false) }
        val syncStatus by viewModel.syncStatus.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

        // Show sync status as snackbar
        LaunchedEffect(syncStatus) {
            when (syncStatus) {
                is SyncStatus.Syncing -> coroutineScope.launch {
                    snackbarHostState.showSnackbar("Syncing data…")
                }

                is SyncStatus.Success -> {
                    val ts = (syncStatus as SyncStatus.Success).timestamp
                    val time = java.time.Instant.ofEpochMilli(ts)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Last synced at $time")
                    }
                }

                is SyncStatus.Error -> {
                    val msg = (syncStatus as SyncStatus.Error).message
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Sync failed: $msg")
                    }
                }

                else -> {}
            }
        }

        // Trigger initial sync for the current month
        LaunchedEffect(Unit) {
            viewModel.syncDataForMonth(currentMonth)
        }

        val onRefresh: () -> Unit = {
            coroutineScope.launch {
                isRefreshing = true
                viewModel.syncDataForMonth(currentMonth)
                isRefreshing = false
            }
        }

        val pieChartData = remember(transactions) {
            transactions.groupBy { it.type.name }
                .mapValues { (_, txs) -> txs.sumOf { it.amount }.toFloat() }
        }


        Column(
            Modifier
                .fillMaxSize()
                .pullToRefresh(
                    state = pullToRefreshState,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh
                ),
        ) {
            // Month Selector Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { showPicker = true }) {
                    Text(
                        text = "${
                            Month.of(currentMonth.month).getDisplayName(TextStyle.FULL, Locale.getDefault())
                        } ${currentMonth.year}",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Total Expenses",
                textAlign = TextAlign.Center
            )

            if (pieChartData.isNotEmpty()) {
                CategoryPieChart(
                    data = pieChartData,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
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


        if (showPicker) {
            MonthYearPickerDialog(
                initialMonth = currentMonth,
                onDismiss = { showPicker = false },
                onConfirm = { selectedMonth ->
                    viewModel.syncDataForMonth(selectedMonth)
                    showPicker = false
                }
            )
        }
    }
}

@Composable
fun MonthYearPickerDialog(
    initialMonth: YearMonth,
    onDismiss: () -> Unit,
    onConfirm: (YearMonth) -> Unit
) {
    var selectedMonth by remember { mutableStateOf(initialMonth) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Month & Year") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Month Dropdown
                var monthExpanded by remember { mutableStateOf(false) }
                Box {
                    TextButton(onClick = { monthExpanded = true }) {
                        Text(Month.of(selectedMonth.month).getDisplayName(TextStyle.SHORT, Locale.getDefault()))
                    }
                    DropdownMenu(expanded = monthExpanded, onDismissRequest = { monthExpanded = false }) {
                        (1..12).forEach { monthVal ->
                            DropdownMenuItem(
                                text = { Text(Month.of(monthVal).getDisplayName(TextStyle.FULL, Locale.getDefault())) },
                                onClick = {
                                    selectedMonth = selectedMonth.copy(month = monthVal)
                                    monthExpanded = false
                                }
                            )
                        }
                    }
                }

                // Year TextField
                var yearText by remember(selectedMonth.year) { mutableStateOf(selectedMonth.year.toString()) }
                OutlinedTextField(
                    value = yearText,
                    onValueChange = {
                        yearText = it
                        if (it.length == 4) {
                            it.toIntOrNull()?.let { yearVal ->
                                selectedMonth = selectedMonth.copy(year = yearVal)
                            }
                        }
                    },
                    label = { Text("Year") },
                    modifier = Modifier.width(100.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedMonth) }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
