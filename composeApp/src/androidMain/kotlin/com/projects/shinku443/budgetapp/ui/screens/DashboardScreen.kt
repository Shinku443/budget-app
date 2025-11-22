package com.projects.shinku443.budgetapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.projects.shinku443.budgetapp.sync.SyncStatus
import com.projects.shinku443.budgetapp.ui.components.CategoryPieChart
import com.projects.shinku443.budgetapp.ui.components.MonthlyComparison
import com.projects.shinku443.budgetapp.ui.components.MonthlyTrendChart
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
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalKoalaPlotApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: BudgetViewModel = koinViewModel()
        val transactionViewModel: TransactionViewModel = koinViewModel()
        val transactions by transactionViewModel.transactionsFiltered.collectAsState()
        val allTransactions by transactionViewModel.transactions.collectAsState()
        val currentMonth by viewModel.currentMonth.collectAsState()
        val expense by viewModel.expense.collectAsState()

        // Local UI selection for month/year (stays in sync with viewModel.currentMonth)
        var selectedMonthLocal by remember { mutableStateOf(currentMonth) }
        LaunchedEffect(currentMonth) {
            selectedMonthLocal = currentMonth
        }

        // Filter transactions by the locally selected month for immediate UI updates
        val selectedMonthStr = selectedMonthLocal.toString()
        val currentMonthTransactions = remember(allTransactions, selectedMonthStr) {
            allTransactions.filter { it.date.startsWith(selectedMonthStr) }
        }

        val previousMonth = remember(selectedMonthLocal) {
            if (selectedMonthLocal.month == 1) {
                YearMonth(selectedMonthLocal.year - 1, 12)
            } else {
                YearMonth(selectedMonthLocal.year, selectedMonthLocal.month - 1)
            }
        }
        val previousMonthStr = previousMonth.toString()
        val previousMonthTransactions = remember(allTransactions, previousMonthStr) {
            allTransactions.filter { it.date.startsWith(previousMonthStr) }
        }

        var isRefreshing by remember { mutableStateOf(false) }
        val pullToRefreshState = rememberPullToRefreshState()
        val coroutineScope = rememberCoroutineScope()

        var showPicker by remember { mutableStateOf(false) }
        val syncStatus by viewModel.syncStatus.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        var showAddTransaction by remember { mutableStateOf(false) }
        var transactionToEdit by remember { mutableStateOf<com.projects.shinku443.budgetapp.model.Transaction?>(null) }

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
        val tabs = listOf("Transactions", "Breakdown", "Trends", "Budgets", "Comparison")
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                if (selectedTab == 0) { // Transactions tab
                    ExtendedFloatingActionButton(
                        onClick = { showAddTransaction = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                        text = { Text("Add") },
                        elevation = FloatingActionButtonDefaults.elevation(8.dp)
                    )
                }
            }
        ) { innerPadding ->
            Column(
                Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .pullToRefresh(
                        state = pullToRefreshState,
                        isRefreshing = isRefreshing,
                        onRefresh = onRefresh
                    ),
            ) {
                // Modern Month Selector Header (uses local selection)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .clickable { showPicker = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            onClick = {
                                // go to previous month (update local state and ask VM to sync)
                                val cm = selectedMonthLocal
                                val prev =
                                    if (cm.month == 1) YearMonth(cm.year - 1, 12) else YearMonth(cm.year, cm.month - 1)
                                selectedMonthLocal = prev
                                viewModel.syncDataForMonth(prev)
//                                transactionViewModel.transactions
//                                transactionViewModel.setFilterMonth(prev)

                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
                        }

                        Spacer(Modifier.width(6.dp))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = Month.of(selectedMonthLocal.month).getDisplayName(TextStyle.FULL, Locale.getDefault())
                                    .uppercase(Locale.getDefault()),
                                style = MaterialTheme.typography.headlineSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = selectedMonthLocal.year.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.width(6.dp))

                        IconButton(
                            onClick = {
                                // go to next month (update local state and ask VM to sync)
                                val cm = selectedMonthLocal
                                val next =
                                    if (cm.month == 12) YearMonth(cm.year + 1, 1) else YearMonth(cm.year, cm.month + 1)
                                selectedMonthLocal = next
                                viewModel.syncDataForMonth(next)
//                                transactionViewModel.setFilterMonth(next)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
                        }
                    }
                }


                        ScrollableTabRow(
                            selectedTabIndex = selectedTab,
                            edgePadding = 16.dp,
                            containerColor = MaterialTheme.colorScheme.surface,
                            indicator = { tabPositions ->
                                TabRowDefaults.Indicator(
                                    Modifier
                                        .tabIndicatorOffset(tabPositions[selectedTab])
                                        .padding(horizontal = 24.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    height = 3.dp
                                )
                            }
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    modifier = Modifier.widthIn(min = 72.dp)
                                ) {
                                    Text(
                                        text = title,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        softWrap = false,
                                        style = if (selectedTab == index) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                }
                            }
                        }

                Spacer(Modifier.height(10.dp))

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
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )

                        Spacer(Modifier.height(8.dp))

                        val displayedTransactions = transactions.filter { it.id !in pendingDeleteIds.value }

                        if (displayedTransactions.isEmpty()) {
                            Text(
                                "No transactions yet",
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(top = 24.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            TransactionList(
                                transactions = displayedTransactions,
                                onDelete = { tx ->
                                    // Direct delete from icon
                                    transactionViewModel.deleteTransaction(tx.id)
                                },
                                onEdit = { tx ->
                                    transactionToEdit = tx
                                    showAddTransaction = true
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
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (pieChartData.isNotEmpty()) {
                            CategoryPieChart(
                                data = pieChartData,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(top = 12.dp)
                            )
                        }
                    }

                    2 -> {
                        // Trends chart - show all transactions for selected month
                        MonthlyTrendChart(
                            transactions = currentMonthTransactions,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }

                    3 -> {
                        // Budget progress
                        val budgetGoal by viewModel.monthlyBudgetGoal.collectAsState()
                        val currentExpenses = expense
                        val progress = if (budgetGoal > 0) {
                            (currentExpenses / budgetGoal.toDouble()).coerceIn(0.0, 1.0)
                        } else {
                            0.0
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "Monthly Budget Progress",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )

                            if (budgetGoal > 0) {
                                LinearProgressIndicator(
                                    progress = progress.toFloat(),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Spent: $${String.format("%.2f", currentExpenses)}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        "Goal: $${String.format("%.2f", budgetGoal)}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }

                                val remaining = budgetGoal.toDouble() - currentExpenses
                                Text(
                                    if (remaining >= 0) {
                                        "Remaining: $${String.format("%.2f", remaining)}"
                                    } else {
                                        "Over budget by: $${String.format("%.2f", -remaining)}"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (remaining >= 0) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    }
                                )
                            } else {
                                Text(
                                    "No budget goal set. Set a goal in the Goals section.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                    }

                    4 -> {
                        // Monthly comparison
                        MonthlyComparison(
                            currentMonthTransactions = currentMonthTransactions,
                            previousMonthTransactions = previousMonthTransactions,
                            currentMonth = selectedMonthLocal,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }
            }

            if (showPicker) {
                MonthYearPickerDialog(
                    initialMonth = selectedMonthLocal,
                    onDismiss = { showPicker = false },
                    onConfirm = { selectedMonth ->
                        selectedMonthLocal = selectedMonth
                        viewModel.syncDataForMonth(selectedMonth)
//                        transactionViewModel.setFilterMonth(selectedMonth)
                        showPicker = false
                    }
                )
            }
        }

        if (showAddTransaction) {
            ModalBottomSheet(
                onDismissRequest = {
                    showAddTransaction = false
                    transactionToEdit = null
                }
            ) {
                AddTransactionScreen(
                    onDismiss = {
                        showAddTransaction = false
                        transactionToEdit = null
                    },
                    transactionToEdit = transactionToEdit
                ).Content()
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