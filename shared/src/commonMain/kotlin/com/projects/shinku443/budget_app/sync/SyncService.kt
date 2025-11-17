package com.projects.shinku443.budget_app.sync

import com.projects.shinku443.budget_app.repository.CategoryRepository
import com.projects.shinku443.budget_app.repository.TransactionRepository
import kotlinx.coroutines.flow.first

class SyncService(
    private val categoryRepo: CategoryRepository,
    private val transactionRepo: TransactionRepository
) {
    suspend fun syncAll() {
        syncCategories()
        syncTransactions()
    }

    suspend fun syncCategories() {
        val remote = categoryRepo.refreshCategories()
        val local = categoryRepo.observeCategories().first()

        // Example reconciliation: prefer newer updatedAt
        local.forEach { localCat ->
            val remoteCat = remote.find { it.id == localCat.id }
            if (remoteCat == null) {
                // push local to server
                categoryRepo.createCategory(localCat.name, localCat.type, localCat.isActive)
            } else if (localCat.updatedAt > remoteCat.updatedAt) {
                // local is newer, push update
                categoryRepo.updateCategory(localCat.id, localCat.name, localCat.type, localCat.isActive)
            }
        }
    }

    private suspend fun syncTransactions() {
        val remote = transactionRepo.refreshTransactions()
        val local = transactionRepo.observeTransactions().first()

        // Example reconciliation: prefer newer createdAt
        local.forEach { localTx ->
            val remoteTx = remote.find { it.id == localTx.id }
            if (remoteTx == null) {
                // push local to server
                transactionRepo.createTransaction(localTx)
            } else if (localTx.createdAt > remoteTx.createdAt) {
                // local is newer, overwrite remote
                transactionRepo.createTransaction(localTx) // or update if you support it
            }
        }
    }
}
