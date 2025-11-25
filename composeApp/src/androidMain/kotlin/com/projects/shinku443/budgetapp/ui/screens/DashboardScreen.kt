package com.projects.shinku443.budgetapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import co.touchlab.kermit.Logger
import com.projects.shinku443.budgetapp.model.Transaction
import com.projects.shinku443.budgetapp.ui.components.*
import com.projects.shinku443.budgetapp.util.YearMonth
import com.projects.shinku443.budgetapp.viewmodel.BudgetViewModel
import com.projects.shinku443.budgetapp.viewmodel.TransactionViewModel
import com.projects.shinku443.budgetapp.viewmodel.UiState
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
        val transactionViewModel: TransactionViewModel = koinViewModel()

        val transactions by transactionViewModel.transactionsFiltered.collectAsState()
        val currentMonth by transactionViewModel.filterMonth.collectAsState()
        val expense by viewModel.expense.collectAsState()
        val uiState by viewModel.uiState.collectAsState()
        val isSyncing by viewModel.isSyncing.collectAsState() // Collect the new isSyncing state

        // New states to manage persistent banner visibility and content
        var isBannerActive by remember { mutableStateOf(false) } // Renamed from shouldShowPersistentBanner for clarity
        var lastKnownOfflineErrorState by remember { mutableStateOf<UiState?>(null) }

        LaunchedEffect(uiState, isSyncing) { // Observe both uiState and isSyncing
            Logger.d("DashboardScreen:: uiState changed to $uiState, isSyncing: $isSyncing")
            when (uiState) {
                is UiState.Offline, is UiState.Error, is UiState.Connecting -> {
                    isBannerActive = true
                    lastKnownOfflineErrorState = uiState
                }
                is UiState.Idle -> {
                    // Only hide banner if we are Idle AND no sync is in progress.
                    // This prevents the banner from disappearing if uiState briefly becomes Idle during a sync.
                    if (!isSyncing) {
                        isBannerActive = false
                        lastKnownOfflineErrorState = null
                    }
                    // If uiState is Idle but isSyncing is true, the banner should remain if it was active.
                    // This case is implicitly handled by not setting isBannerActive to false.
                }
            }
        }

        val previousMonth = remember(currentMonth) {
            if (currentMonth.month == 1) YearMonth(currentMonth.year - 1, 12)
            else YearMonth(currentMonth.year, currentMonth.month - 1)
        }
        val previousMonthTransactions by transactionViewModel.getTransactionsForMonth(previousMonth)
            .collectAsState(initial = emptyList())

        var isRefreshing by remember { mutableStateOf(false) }
        val pullToRefreshState = rememberPullToRefreshState()
        val coroutineScope = rememberCoroutineScope()
        var showPicker by remember { mutableStateOf(false) }
        val snackbarHostState = remember { SnackbarHostState() }
        var showAddTransaction by remember { mutableStateOf(false) }
        var transactionToEdit by remember { mutableStateOf<Transaction?>(null) }

        LaunchedEffect(currentMonth) {
            Logger.d("DashboardScreen:: Syncing data for month: $currentMonth")
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
            topBar = {
                if (isBannerActive) { // Use the new isBannerActive state
                    AnimatedVisibility(
                        visible = true, // Always true here, as the outer if handles visibility
                        enter = slideInVertically(initialOffsetY = { -it }),
                        exit = slideOutVertically(targetOffsetY = { -it })
                    ) {
                        // Determine banner content based on lastKnownOfflineErrorState
                        val (backgroundColor, text, textColor) = when (lastKnownOfflineErrorState) {
                            is UiState.Offline -> Triple(MaterialTheme.colorScheme.tertiaryContainer, "Offline mode: Data is saved locally.", MaterialTheme.colorScheme.onTertiaryContainer)
                            is UiState.Error -> Triple(MaterialTheme.colorScheme.errorContainer, "Connectivity Issues - Offline Mode", MaterialTheme.colorScheme.onErrorContainer)
                            is UiState.Connecting -> Triple(MaterialTheme.colorScheme.primaryContainer, "Connecting...", MaterialTheme.colorScheme.onPrimaryContainer)
                            else -> Triple(MaterialTheme.colorScheme.surface, "", MaterialTheme.colorScheme.onSurface) // Fallback, should not be reached if isBannerActive is true
                        }
                        SyncStatusBanner(
                            text = text,
                            backgroundColor = backgroundColor,
                            textColor = textColor
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                if (selectedTab == 0) {
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
                if (isSyncing) { // Use the new isSyncing state for the progress indicator
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

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
                                val prev = if (currentMonth.month == 1) YearMonth(currentMonth.year - 1, 12) else YearMonth(currentMonth.year, currentMonth.month - 1)
                                transactionViewModel.setFilterMonth(prev)
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
                                text = Month.of(currentMonth.month).getDisplayName(TextStyle.FULL, Locale.getDefault())
                                    .uppercase(Locale.getDefault()),
                                style = MaterialTheme.typography.headlineSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = currentMonth.year.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.width(6.dp))

                        IconButton(
                            onClick = {
                                val next = if (currentMonth.month == 12) YearMonth(currentMonth.year + 1, 1) else YearMonth(currentMonth.year, currentMonth.month + 1)
                                transactionViewModel.setFilterMonth(next)
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

                        if (transactions.isEmpty()) {
                            Text(
                                "No transactions yet",
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(top = 24.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            TransactionList(
                                transactions = transactions,
                                onEditItem = { tx ->
                                    transactionToEdit = tx
                                    showAddTransaction = true
                                },
                                onDeleteItem = { tx ->
                                    coroutineScope.launch {
                                        transactionViewModel.stageTransactionForDeletion(tx.id)
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Transaction deleted",
                                            actionLabel = "Undo",
                                            withDismissAction = true
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            transactionViewModel.unstageTransactionForDeletion(tx.id)
                                        } else {
                                            transactionViewModel.deleteTransaction(tx.id)
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
                        MonthlyTrendChart(
                            transactions = transactions,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                    3 -> {
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
                        MonthlyComparison(
                            currentMonthTransactions = transactions,
                            previousMonthTransactions = previousMonthTransactions,
                            currentMonth = currentMonth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }
            }

            if (showPicker) {
                MonthYearPickerDialog(
                    initialMonth = currentMonth,
                    onDismiss = { showPicker = false },
                    onConfirm = { selectedMonth ->
                        transactionViewModel.setFilterMonth(selectedMonth)
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