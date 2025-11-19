package com.projects.shinku443.budgetapp.sync

import com.projects.shinku443.budgetapp.util.YearMonth
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * Orchestrates the synchronization of all data types in the application.
 */
class SyncService(
    private val transactionSyncManager: TransactionSyncManager,
    private val categorySyncManager: CategorySyncManager
) {
    /**
     * Performs a full synchronization of all application data.
     * @param month An optional month to scope the transaction sync to.
     */
    suspend fun syncAll(month: YearMonth? = null) {
        coroutineScope {
            launch { transactionSyncManager.sync(month) }
            launch { categorySyncManager.sync() }
        }
    }
}
