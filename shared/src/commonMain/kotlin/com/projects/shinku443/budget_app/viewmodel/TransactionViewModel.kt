package com.projects.shinku443.budget_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projects.shinku443.budget_app.model.Transaction
import com.projects.shinku443.budget_app.repository.TransactionRepository
import com.projects.shinku443.budget_app.sync.SyncService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val repo: TransactionRepository,
    private val syncService: SyncService
) : ViewModel() {

    // Observe transactions directly from DB
    val transactions: StateFlow<List<Transaction>> = repo.observeTransactions()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        refreshTransactions()
    }

    fun refreshTransactions() {
        viewModelScope.launch {
            syncService.syncTransactions() // API → DB reconciliation
        }
    }

    fun createTransaction(tx: Transaction) {
        viewModelScope.launch {
            repo.createTransaction(tx)   // API + DB
            syncService.syncTransactions()
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            repo.deleteTransaction(id)   // API + DB
            syncService.syncTransactions()
        }
    }
}
