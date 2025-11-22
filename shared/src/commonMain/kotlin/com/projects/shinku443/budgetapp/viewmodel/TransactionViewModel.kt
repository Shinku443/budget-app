package com.projects.shinku443.budgetapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projects.shinku443.budgetapp.model.Transaction
import com.projects.shinku443.budgetapp.model.CategoryType
import com.projects.shinku443.budgetapp.repository.TransactionRepository
import com.projects.shinku443.budgetapp.sync.SyncService
import com.projects.shinku443.budgetapp.util.TimeWrapper
import com.projects.shinku443.budgetapp.util.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val repo: TransactionRepository,
    private val syncService: SyncService
) : ViewModel() {

    // Observe transactions directly from DB
    val transactions: StateFlow<List<Transaction>> = repo.observeTransactions()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _filterMonth = MutableStateFlow(TimeWrapper.currentYearMonth())
    val filterMonth: StateFlow<YearMonth> = _filterMonth.asStateFlow()

    // Keep track of transactions staged for deletion
    private val pendingDeleteIds = MutableStateFlow(setOf<String>())

    // Global filter state and derived filtered list
    data class TransactionFilter(
        val query: String? = null,
        val type: CategoryType? = null,
        val categoryId: String? = null
    )

    private val filter = MutableStateFlow(TransactionFilter())

    val transactionsFiltered: StateFlow<List<Transaction>> = combine(transactions, filter, pendingDeleteIds) { list, f, pendingIds ->
        list.filter { tx ->
            val matchesQuery = f.query?.let { q -> (tx.description ?: "").contains(q, ignoreCase = true) } ?: true
            val matchesType = f.type?.let { t -> tx.type == t } ?: true
            val matchesCategory = f.categoryId?.let { id -> tx.categoryId == id } ?: true
            val notPendingDeletion = tx.id !in pendingIds
            matchesQuery && matchesType && matchesCategory && notPendingDeletion
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        syncAll()
    }

    fun syncAll() {
        viewModelScope.launch {
            syncService.syncAll()
        }
    }

    fun createTransaction(tx: Transaction) {
        viewModelScope.launch {
            repo.createTransaction(tx)
        }
    }

    fun updateTransaction(tx: Transaction) {
        viewModelScope.launch {
            repo.updateTransaction(tx)
        }
    }

    fun stageTransactionForDeletion(id: String) {
        pendingDeleteIds.update { it + id }
    }

    fun unstageTransactionForDeletion(id: String) {
        pendingDeleteIds.update { it - id }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            repo.deleteTransaction(id)
            pendingDeleteIds.update { it - id } // Ensure it's removed from pending
        }
    }

    fun deleteTransactions(ids: List<String>) {
        viewModelScope.launch {
            repo.deleteTransactions(ids)
            pendingDeleteIds.update { it - ids.toSet() }
        }
    }

    // Filter setters
    fun setQuery(query: String) {
        val q = query.trim().ifBlank { null }
        filter.update { it.copy(query = q) }
    }

    fun setTypeFilter(type: CategoryType?) {
        filter.update { it.copy(type = type) }
    }

    fun setCategoryFilter(categoryId: String?) {
        val id = categoryId?.ifBlank { null }
        filter.update { it.copy(categoryId = id) }
    }
}
