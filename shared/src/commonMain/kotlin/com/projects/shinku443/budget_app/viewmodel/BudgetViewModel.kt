package com.projects.shinku443.budget_app.viewmodel

import Transaction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projects.shinku443.budget_app.repository.BudgetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.projects.shinku443.budget_app.util.TimeWrapper
import com.projects.shinku443.budget_app.util.YearMonth

class BudgetViewModel(
    private val repository: BudgetRepository
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _currentMonth = MutableStateFlow(TimeWrapper.currentYearMonth())
    val currentMonth: YearMonth get() = _currentMonth.value

    val income: StateFlow<Double> = transactions.map { list ->
        list.filter { it.type.name == "INCOME" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    val expense: StateFlow<Double> = transactions.map { list ->
        list.filter { it.type.name == "EXPENSE" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    val net: StateFlow<Double> = combine(income, expense) { inc, exp -> inc - exp }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)


    fun loadTransactions(monthYear: YearMonth) {
        viewModelScope.launch {
            val txs = repository.getTransactions(TimeWrapper.formatMonthYear(monthYear))
            _transactions.value = txs
            _currentMonth.value = monthYear
        }
    }

    fun addTransaction(tx: Transaction) {
        viewModelScope.launch {
            repository.addTransaction(tx)
            // reload current month after adding
            loadTransactions(_currentMonth.value)
        }
    }

}
