package com.projects.shinku443.budget_app.sync

import co.touchlab.kermit.Logger
import com.projects.shinku443.budget_app.api.ApiClient
import com.projects.shinku443.budget_app.db.BudgetDatabase
import com.projects.shinku443.budget_app.model.Transaction
import com.projects.shinku443.budget_app.util.YearMonth
import com.projects.shinku443.budget_app.util.mapper.toDb
import com.projects.shinku443.budget_app.util.mapper.toDomain

class TransactionSyncManager(
    private val api: ApiClient,
    private val db: BudgetDatabase
) : SyncManager {
    private val transactionQueries = db.transactionQueries

    override suspend fun sync(month: YearMonth?) {
        try {
            val endpoint = if (month != null) "/transactions?month=${month}" else "/transactions"
            val remoteTxs = api.get<List<Transaction>>(endpoint)
            val remoteIds = remoteTxs.map { it.id }.toSet()

            // 1. Sync offline deletions
            val deletedTxs = transactionQueries.selectAllIncludingDeleted().executeAsList().filter { it.is_deleted == 1L }
            deletedTxs.forEach { tx ->
                if (tx.id in remoteIds) {
                    try {
                        api.delete<Unit>("/transactions/${tx.id}")
                        transactionQueries.deleteById(tx.id)
                    } catch (e: Exception) {
                        Logger.e("TransactionSyncManager") { "Failed to sync deleted transaction: ${e.message}" }
                    }
                } else {
                    transactionQueries.deleteById(tx.id)
                }
            }

            // 2. Sync offline creations
            val localTxs = transactionQueries.selectAll().executeAsList().map { it.toDomain() }
            val offlineTxs = localTxs.filter { it.id !in remoteIds }
            offlineTxs.forEach { offlineTx ->
                try {
                    val syncedTx = api.post<Transaction>("/transactions", offlineTx)
                    transactionQueries.deleteById(offlineTx.id)
                    val dbTx = syncedTx.toDb()
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
                } catch (e: Exception) {
                    Logger.e("TransactionSyncManager") { "Failed to sync created transaction: ${e.message}" }
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
        } catch (e: Exception) {
            Logger.e("TransactionSyncManager") { "Failed to sync transactions: ${e.message}" }
        }
    }
}
