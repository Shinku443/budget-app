package com.projects.shinku443.budget_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projects.shinku443.budget_app.model.Category
import com.projects.shinku443.budget_app.model.CategoryType
import com.projects.shinku443.budget_app.model.Transaction
import com.projects.shinku443.budget_app.repository.CategoryRepository
import com.projects.shinku443.budget_app.repository.TransactionRepository
import com.projects.shinku443.budget_app.sync.SyncService
import com.projects.shinku443.budget_app.util.TimeWrapper
import com.projects.shinku443.budget_app.util.YearMonth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BudgetViewModel(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val syncService: SyncService
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(TimeWrapper.currentYearMonth())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    val transactions: StateFlow<List<Transaction>> = transactionRepository.observeTransactions()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val categories: StateFlow<List<Category>> = categoryRepository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _monthlyBudgetGoal = MutableStateFlow(0.0f)
    val monthlyBudgetGoal: Float get() = _monthlyBudgetGoal.value

    // Derived flows
    val expense: StateFlow<Double> = transactions.map { list ->
        list.filter { it.type == CategoryType.EXPENSE }
            .sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    val monthlyExpenses: StateFlow<List<Transaction>> = transactions.map { list ->
        list.filter { it.type == CategoryType.EXPENSE }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val income: StateFlow<Double> = transactions.map { list ->
        list.filter { it.type == CategoryType.INCOME }
            .sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    val net: StateFlow<Double> = combine(income, expense) { inc, exp -> inc - exp }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    fun syncDataForMonth(month: YearMonth) {
        _currentMonth.value = month
        viewModelScope.launch {
            try {
                syncService.syncAll(month)
            } catch (e: Exception) {
                println("Failed to sync data for month: ${e.message}")
            }
        }
    }

    fun addTransaction(tx: Transaction) {
        viewModelScope.launch {
            try {
                transactionRepository.createTransaction(tx)
                syncService.syncAll(_currentMonth.value) // Re-sync after adding
            } catch (e: Exception) {
                println("Failed to add transaction: ${e.message}")
            }
        }
    }

    fun deleteTransaction(tx: Transaction) {
        viewModelScope.launch {
            try {
                transactionRepository.deleteTransaction(tx.id)
                syncService.syncAll(_currentMonth.value) // Re-sync after deleting
            } catch (e: Exception) {
                println("Failed to delete transaction: ${e.message}")
            }
        }
    }

    fun addCategory(name: String, type: CategoryType) {
        viewModelScope.launch {
            try {
                categoryRepository.createCategory(name, type, true)
                syncService.syncAll() // Re-sync categories
            } catch (e: Exception) {
                println("Failed to add category: ${e.message}")
            }
        }
    }
}
