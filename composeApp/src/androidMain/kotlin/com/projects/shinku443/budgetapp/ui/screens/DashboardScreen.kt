package com.projects.shinku443.budgetapp.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.projects.shinku443.budgetapp.viewmodel.TransactionViewModel
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.androidx.compose.koinViewModel
import java.time.Month
import java.time.format.TextStyle
import java.util.*


class DashboardScreen : Screen {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter") //TODO - look into this
    @Composable
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalKoalaPlotApi::class)
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: BudgetViewModel = koinViewModel()
        val transactionViewModel: TransactionViewModel = koinViewModel()
        val transactions by transactionViewModel.transactionsFiltered.collectAsState()
        val currentMonth by viewModel.currentMonth.collectAsState()

        var isRefreshing by remember { mutableStateOf(false) }
        val pullToRefreshState = rememberPullToRefreshState()
        val coroutineScope = rememberCoroutineScope()

        var showPicker by remember { mutableStateOf(false) }
        val syncStatus by viewModel.syncStatus.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        var showAddTransaction by remember { mutableStateOf(false) }

        // Track items that are pending deletion, to be removed from the UI temporarily
        val pendingDeleteIds = remember { mutableStateOf(setOf<String>()) }

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

        val pieChartData by remember(transactions) {
            mutableStateOf(
                transactions.groupBy { it.type.name }
                    .mapValues { (_, txs) -> txs.sumOf { it.amount }.toFloat() }
            )
        }

        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("Transactions", "Breakdown", "Trends", "Budgets")
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                if (selectedTab == 0) { // Transactions tab
                    ExtendedFloatingActionButton(
                        onClick = { showAddTransaction = true },
                        containerColor = MaterialTheme.colorScheme.primary,

//                        onClick = { navigator.push(AddTransactionScreen()) },
                        icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                        text = { Text("Add Transaction") }
                    )
                }
            }
        ) {
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

                // Tabs
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                when (selectedTab) {
                    0 -> {
                        // Search field
                        var query by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = query,
                            onValueChange = {
                                query = it
                                transactionViewModel.setQuery(it)
                            },
                            label = { Text("Search transactions") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )

                        Spacer(Modifier.height(8.dp))

                        val displayedTransactions = transactions.filter { it.id !in pendingDeleteIds.value }

                        if (displayedTransactions.isEmpty()) {
                            Text("No transactions yet", modifier = Modifier.align(Alignment.CenterHorizontally))
                        } else {
                            TransactionList(
                                transactions = displayedTransactions,
                                onDelete = { tx ->
                                    // Direct delete from icon
                                    transactionViewModel.deleteTransaction(tx.id)
                                },
                                onSwipeDelete = { tx ->
                                    coroutineScope.launch {
                                        // 1. Add to pending list to hide from UI
                                        pendingDeleteIds.value = pendingDeleteIds.value + tx.id

                                        // 2. Show snackbar with timeout
                                        val result = withTimeoutOrNull(5000) {
                                            snackbarHostState.showSnackbar(
                                                message = "Transaction deleted",
                                                actionLabel = "Undo",
                                                withDismissAction = true
                                            )
                                        }

                                        // 3. Handle snackbar result
                                        if (result == SnackbarResult.ActionPerformed) {
                                            // UNDO: Remove from pending list to show in UI again
                                            pendingDeleteIds.value -= tx.id
                                        } else {
                                            // TIMEOUT or DISMISS: Perform the actual deletion
                                            transactionViewModel.deleteTransaction(tx.id)
                                            // The item is already removed from the main list, so we just clean up our pending state
                                            pendingDeleteIds.value -= tx.id
                                        }
                                    }
                                }
                            )
                        }
                    }

                    1 -> {
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
                    }

                    2 -> {
                        // Placeholder for trends chart
                        Text("Trends chart goes here", modifier = Modifier.align(Alignment.CenterHorizontally))
                    }

                    3 -> {
                        // Placeholder for budgets progress
                        Text("Budget progress bars go here", modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
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

        if (showAddTransaction) {
            ModalBottomSheet(
                onDismissRequest = { showAddTransaction = false }
            ) {
                AddTransactionScreen(onDismiss = { showAddTransaction = false }).Content()
            }
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
