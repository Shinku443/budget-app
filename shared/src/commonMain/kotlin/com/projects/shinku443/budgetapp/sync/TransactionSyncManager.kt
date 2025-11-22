package com.projects.shinku443.budgetapp.sync

import co.touchlab.kermit.Logger
import com.projects.shinku443.budgetapp.api.ApiClient
import com.projects.shinku443.budgetapp.db.BudgetDatabase
import com.projects.shinku443.budgetapp.model.Transaction
import com.projects.shinku443.budgetapp.util.YearMonth
import com.projects.shinku443.budgetapp.util.mapper.toDb
import com.projects.shinku443.budgetapp.util.mapper.toDomain
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TransactionSyncManager(
    private val api: ApiClient,
    private val db: BudgetDatabase
) : SyncManager {

    private val transactionQueries = db.transactionQueries

    private val _status = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val status: StateFlow<SyncStatus> = _status.asStateFlow()

    override suspend fun sync(month: YearMonth?) {
        _status.value = SyncStatus.Syncing
        try {
            val endpoint = if (month != null) "/transactions?month=$month" else "/transactions"
            val remoteTxs = api.get<List<Transaction>>(endpoint)
            val remoteIds = remoteTxs.map { it.id }.toSet()

            // 1. Sync offline deletions
            val deletedTxs = transactionQueries.selectAllIncludingDeleted()
                .executeAsList()
                .filter { it.is_deleted == 1L }
            if (deletedTxs.isNotEmpty()) {
                try {
                    api.post<Unit>("/transactions/batchDelete", deletedTxs.map { it.id })
                    deletedTxs.forEach { transactionQueries.deleteById(it.id) }
                } catch (e: Exception) {
                    Logger.e("TransactionSyncManager") { "Batch delete failed: ${e.message}" }
                }
            }

            // 2. Sync offline creations
            val localTxs = transactionQueries.selectAll().executeAsList().map { it.toDomain() }
            val offlineTxs = localTxs.filter { it.id !in remoteIds }
            if (offlineTxs.isNotEmpty()) {
                try {
                    val syncedTxs = api.post<List<Transaction>>("/transactions/batch", offlineTxs)
                    transactionQueries.transaction {
                        offlineTxs.forEach { transactionQueries.deleteById(it.id) }
                        syncedTxs.forEach { tx ->
                            val dbTx = tx.toDb()
                            transactionQueries.insertOrReplace(
                                id = dbTx.id,
                                amount = dbTx.amount,
                                type = dbTx.type,
                                categoryId = dbTx.categoryId,
                                date = dbTx.date,
                                description = dbTx.description,
                                createdAt = dbTx.createdAt,
                                is_deleted = 0
                            )
                        }
                    }
                } catch (e: Exception) {
                    Logger.e("TransactionSyncManager") { "Batch create failed: ${e.message}" }
                }
            }

            // 3. Final refresh from remote
            val finalRemoteTxs = api.get<List<Transaction>>(endpoint)
            transactionQueries.transaction {
                finalRemoteTxs.forEach { tx ->
                    val dbTx = tx.toDb()
                    transactionQueries.insertOrReplace(
                        id = dbTx.id,
                        amount = dbTx.amount,
                        type = dbTx.type,
                        categoryId = dbTx.categoryId,
                        date = dbTx.date,
                        description = dbTx.description,
                        createdAt = dbTx.createdAt,
                        is_deleted = 0
                    )
                }
            }

            _status.value = SyncStatus.Success(getTimeMillis())
        } catch (e: Exception) {
            _status.value = SyncStatus.Error("Failed to sync transactions: ${e.message}")
            Logger.e("TransactionSyncManager") { "Failed to sync transactions: ${e.message}" }
        }
    }
}
