package com.projects.shinku443.budget_app.viewmodel

import Category
import Transaction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projects.shinku443.budget_app.model.CategoryType
import com.projects.shinku443.budget_app.repository.BudgetRepository
import com.projects.shinku443.budget_app.util.TimeWrapper
import com.projects.shinku443.budget_app.util.YearMonth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BudgetViewModel(
    private val repository: BudgetRepository
) : ViewModel() {

    // Transactions
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _currentMonth = MutableStateFlow(TimeWrapper.currentYearMonth())
    val currentMonth: YearMonth get() = _currentMonth.value

    private val _monthlyBudgetGoal = MutableStateFlow(0.0f)
    val monthlyBudgetGoal: Float get() = _monthlyBudgetGoal.value
    
    
    val expense: StateFlow<Double> = transactions.map { list ->
        list.filter { tx ->
            // expense categories
            tx.categoryType == CategoryType.EXPENSE
        }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    val monthlyExpenses: StateFlow<List<Transaction>> = transactions.map { list ->
        list.filter { tx ->
            tx.categoryType == CategoryType.EXPENSE
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val income: StateFlow<Double> = transactions.map { list ->
        list.filter { tx ->
            // income categories
            tx.categoryType == CategoryType.INCOME
        }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    val net: StateFlow<Double> = combine(income, expense) { inc, exp -> inc - exp }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    fun loadTransactions(monthYear: YearMonth) {
        viewModelScope.launch {
            try {
                val txs = repository.getTransactions(monthYear.toString())
                _transactions.value = txs
                _currentMonth.value = monthYear
            } catch (e: Exception) {
                println("Failed to load transactions: ${e.message}")
                _transactions.value = emptyList()
            }
        }
    }

    fun addTransaction(tx: Transaction) {
        viewModelScope.launch {
            try {
                repository.addTransaction(tx)
                loadTransactions(_currentMonth.value)
            } catch (e: Exception) {
                println("Failed to add transaction: ${e.message}")
            }
        }
    }

    fun deleteTransaction(tx: Transaction) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(tx.id)
                loadTransactions(_currentMonth.value)
            } catch (e: Exception) {
                println("Failed to delete transaction: ${e.message}")
            }
        }
    }

    // Categories
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    fun loadCategories() {
        viewModelScope.launch {
            try {
                _categories.value = repository.getCategories()
            } catch (e: Exception) {
                println("Failed to load categories: ${e.message}")
                _categories.value = emptyList()
            }
        }
    }

    fun addCategory(name: String, type: CategoryType) {
        viewModelScope.launch {
            try {
                val cat = repository.createCategory(name, type)
                _categories.value += cat
            } catch (e: Exception) {
                println("Failed to add category: ${e.message}")
            }
        }
    }
}
