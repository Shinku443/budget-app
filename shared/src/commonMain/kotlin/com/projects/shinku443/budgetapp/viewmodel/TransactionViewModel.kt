package com.projects.shinku443.budgetapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projects.shinku443.budgetapp.model.CategoryType
import com.projects.shinku443.budgetapp.model.Transaction
import com.projects.shinku443.budgetapp.repository.TransactionRepository
import com.projects.shinku443.budgetapp.sync.SyncService
import com.projects.shinku443.budgetapp.util.TimeWrapper
import com.projects.shinku443.budgetapp.util.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModel(
    private val repo: TransactionRepository,
    private val syncService: SyncService
) : ViewModel() {

    private val _filterMonth = MutableStateFlow(TimeWrapper.currentYearMonth())
    val filterMonth: StateFlow<YearMonth> = _filterMonth.asStateFlow()

    private val transactionsForMonth: StateFlow<List<Transaction>> = _filterMonth.flatMapLatest { month ->
        repo.observeTransactionsForMonth(month)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- PENDING DELETION LOGIC RE-ADDED ---
    private val pendingDeleteIds = MutableStateFlow(setOf<String>())

    data class TransactionFilter(
        val query: String? = null,
        val type: CategoryType? = null,
        val categoryId: String? = null
    )

    private val filter = MutableStateFlow(TransactionFilter())

    val transactionsFiltered: StateFlow<List<Transaction>> = combine(
        transactionsForMonth,
        filter,
        pendingDeleteIds // Depend on the pending IDs
    ) { list, f, pendingIds ->
        list.filter { tx ->
            val notPendingDeletion = tx.id !in pendingIds // Filter out pending items
            val matchesQuery = f.query?.let { q -> (tx.description ?: "").contains(q, ignoreCase = true) } ?: true
            val matchesType = f.type?.let { t -> tx.type == t } ?: true
            val matchesCategory = f.categoryId?.let { id -> tx.categoryId == id } ?: true
            notPendingDeletion && matchesQuery && matchesType && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        syncAll()
    }

    fun syncAll() {
        syncService.syncAll()
    }

    fun setFilterMonth(yearMonth: YearMonth) {
        _filterMonth.value = yearMonth
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

    // --- PENDING DELETION METHODS RE-ADDED ---
    fun stageTransactionForDeletion(id: String) {
        pendingDeleteIds.update { it + id }
    }

    fun unstageTransactionForDeletion(id: String) {
        pendingDeleteIds.update { it - id }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            repo.deleteTransaction(id)
            // Ensure it's removed from pending list after deletion
            pendingDeleteIds.update { it - id }
        }
    }

    fun deleteTransactions(ids: List<String>) {
        viewModelScope.launch {
            repo.deleteTransactions(ids)
            pendingDeleteIds.update { it - ids.toSet() }
        }
    }

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
    
    fun getTransactionsForMonth(yearMonth: YearMonth): Flow<List<Transaction>> {
        return repo.observeTransactionsForMonth(yearMonth)
    }
}
