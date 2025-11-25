package com.projects.shinku443.budgetapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.projects.shinku443.budgetapp.connectivity.ConnectivityMonitor
import com.projects.shinku443.budgetapp.model.BudgetGoal
import com.projects.shinku443.budgetapp.model.CategoryType
import com.projects.shinku443.budgetapp.model.Transaction
import com.projects.shinku443.budgetapp.repository.BudgetRepository
import com.projects.shinku443.budgetapp.repository.TransactionRepository
import com.projects.shinku443.budgetapp.sync.SyncService
import com.projects.shinku443.budgetapp.util.TimeWrapper
import com.projects.shinku443.budgetapp.util.YearMonth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class UiState {
    object Idle : UiState()
    // object Syncing : UiState() // Removed, as sync status is now separate
    object Offline : UiState()
    data class Error(val message: String) : UiState()
    object Connecting : UiState()
}

class BudgetViewModel(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val syncService: SyncService,
    private val connectivityMonitor: ConnectivityMonitor
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(TimeWrapper.currentYearMonth())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    // The ViewModel now gets its UI state (for banner) directly from the SyncService.
    val uiState: StateFlow<UiState> = syncService.overallState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Idle)

    // New flow to indicate if a sync is visually in progress (for progress bar)
    val isSyncing: StateFlow<Boolean> = syncService.isSyncInProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        startPeriodicSync()
    }

    val monthlyBudgetGoal: StateFlow<Float> = currentMonth
        .flatMapLatest { month ->
            budgetRepository.observeBudgetGoal(month)
        }
        .map { goal -> goal?.amount ?: 0.0f }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0f)

    val transactions: StateFlow<List<Transaction>> = currentMonth
        .flatMapLatest { month ->
            transactionRepository.observeTransactionsForMonth(month)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expense: StateFlow<Double> = transactions.map { list ->
        list.filter { it.type == CategoryType.EXPENSE }
            .sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val income: StateFlow<Double> = transactions.map { list ->
        list.filter { it.type == CategoryType.INCOME }
            .sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val net: StateFlow<Double> = combine(income, expense) { inc, exp -> inc - exp }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun syncDataForMonth(month: YearMonth) {
        Logger.d("BudgetViewModel:: Triggering sync for month")
        _currentMonth.value = month
        // The call is now non-blocking. The UI will update via the uiState flow.
        syncService.syncAll(month)
    }

    private fun startPeriodicSync() {
        viewModelScope.launch {
            while (true) {
                delay(10_000) // Wait for 10 seconds for testing
                Logger.d("BudgetViewModel:: Starting periodic sync.")
                syncService.syncAll(currentMonth.value)
            }
        }
    }

    fun setMonthlyBudgetGoal(goal: Float, yearMonth: YearMonth? = null) {
        viewModelScope.launch {
            val targetMonth = yearMonth ?: currentMonth.value
            val budgetGoal = BudgetGoal(
                id = "${targetMonth.year}-${targetMonth.month}",
                yearMonth = targetMonth,
                amount = goal,
                updatedAt = TimeWrapper.currentTimeMillis()
            )
            budgetRepository.setBudgetGoal(budgetGoal)
        }
    }

    fun getBudgetGoalForMonth(yearMonth: YearMonth): StateFlow<Float> {
        return budgetRepository.observeBudgetGoal(yearMonth)
            .map { goal -> goal?.amount ?: 0.0f }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0f)
    }

    fun monthlyProgress(month: YearMonth): Flow<Pair<Double, Double>> {
        return transactionRepository.observeTransactionsForMonth(month).map { txs ->
            val expenses = txs.filter { it.type == CategoryType.EXPENSE }
                .sumOf { it.amount }
            val budget = 2000.0 // TODO: configurable per user
            expenses to budget
        }
    }
}
