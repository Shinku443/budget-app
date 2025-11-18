package com.projects.shinku443.budget_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projects.shinku443.budget_app.model.Category
import com.projects.shinku443.budget_app.model.CategoryType
import com.projects.shinku443.budget_app.model.Transaction
import com.projects.shinku443.budget_app.repository.BudgetRepository
import com.projects.shinku443.budget_app.util.TimeWrapper
import com.projects.shinku443.budget_app.util.YearMonth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BudgetViewModel(
    private val repository: BudgetRepository
) : ViewModel() {

    // Observe directly from repository (DB-backed flows)
    val transactions: StateFlow<List<Transaction>> = repository.observeTransactions()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val categories: StateFlow<List<Category>> = repository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _currentMonth = MutableStateFlow(TimeWrapper.currentYearMonth())
    val currentMonth: YearMonth get() = _currentMonth.value

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

    val categoryTotals: StateFlow<Map<Category, Double>> =
        combine(categories, transactions) { cats, txs ->
            cats.associateWith { cat ->
                txs.filter { it.categoryId == cat.id }
                    .sumOf { it.amount }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())

    // Refresh from API → DB
    fun refreshTransactions(monthYear: YearMonth) {
        viewModelScope.launch {
            try {
                repository.refreshTransactions(monthYear.toString())
                _currentMonth.value = monthYear
            } catch (e: Exception) {
                println("Failed to refresh transactions: ${e.message}")
            }
        }
    }

    fun refreshCategories() {
        viewModelScope.launch {
            try {
                repository.refreshCategories()
            } catch (e: Exception) {
                println("Failed to refresh categories: ${e.message}")
            }
        }
    }

    // Mutations: hit API + update DB
    fun addTransaction(tx: Transaction) {
        viewModelScope.launch {
            try {
                repository.addTransaction(tx)
                refreshTransactions(_currentMonth.value)
            } catch (e: Exception) {
                println("Failed to add transaction: ${e.message}")
            }
        }
    }

    fun deleteTransaction(tx: Transaction) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(tx.id)
                refreshTransactions(_currentMonth.value)
            } catch (e: Exception) {
                println("Failed to delete transaction: ${e.message}")
            }
        }
    }

    fun addCategory(name: String, type: CategoryType) {
        viewModelScope.launch {
            try {
                repository.createCategory(name, type)
                refreshCategories()
            } catch (e: Exception) {
                println("Failed to add category: ${e.message}")
            }
        }
    }
}
