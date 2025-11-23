package com.projects.shinku443.budgetapp.sync

import co.touchlab.kermit.Logger
import com.projects.shinku443.budgetapp.api.ApiClient
import com.projects.shinku443.budgetapp.db.BudgetDatabase
import com.projects.shinku443.budgetapp.model.Transaction
import com.projects.shinku443.budgetapp.util.YearMonth
import com.projects.shinku443.budgetapp.util.mapper.toDb
import com.projects.shinku443.budgetapp.util.mapper.toDomain
import com.projects.shinku443.budgetapp.util.toSqlStartDate
import com.projects.shinku443.budgetapp.util.toSqlStartOfNextMonthDate
import io.ktor.util.date.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TransactionSyncManager(
    private val api: ApiClient,
    private val db: BudgetDatabase
) : SyncManager {

    private val transactionQueries = db.transactionQueries

    private val _status = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    override val status: StateFlow<SyncStatus> = _status.asStateFlow()

    private suspend fun needsSync(month: YearMonth?): Boolean {
        val endpoint = if (month != null) "/transactions?month=$month" else "/transactions"
        try {
            val remoteTxs = api.get<List<Transaction>>(endpoint)
            val localTxs = getLocalTransactions(month)

            if (remoteTxs.size != localTxs.size) {
                Logger.d("TransactionSyncManager") { "Sync needed: Different number of transactions (Remote: ${remoteTxs.size}, Local: ${localTxs.size})." }
                return true
            }

            val remoteSet = remoteTxs.map { it.id }.toSet()
            val localSet = localTxs.map { it.id }.toSet()
            if (remoteSet != localSet) {
                Logger.d("TransactionSyncManager") { "Sync needed: Transaction content appears to differ." }
                return true
            }

            Logger.d("TransactionSyncManager") { "No sync needed for transactions." }
            return false
        } catch (e: Exception) {
            Logger.e("TransactionSyncManager") { "Could not check for sync, assuming needed: ${e.message}" }
            // If we can't check, assume a sync is needed, but also report the error.
            _status.value = SyncStatus.Error("Failed to check for sync: ${e.message}")
            return true
        }
    }

    override suspend fun sync(month: YearMonth?) {
        if (_status.value is SyncStatus.Syncing) {
            Logger.d("TransactionSyncManager") { "Sync already in progress." }
            return
        }
        _status.value = SyncStatus.Syncing
        try {
            val endpoint = if (month != null) "/transactions?month=$month" else "/transactions"

            val remoteTxs = api.get<List<Transaction>>(endpoint)
            val remoteIds = remoteTxs.map { it.id }.toSet()
            val localTxs = getLocalTransactions(month)
            val offlineTxs = localTxs.filter { it.id !in remoteIds }

            if (offlineTxs.isNotEmpty()) {
                Logger.d("TransactionSyncManager") { "Pushing ${offlineTxs.size} offline transaction(s) to server." }
                api.post<Unit>("/transactions/batch", offlineTxs)
            }

            Logger.d("TransactionSyncManager") { "Refreshing local state from server." }
            val finalRemoteTxs = api.get<List<Transaction>>(endpoint)
            transactionQueries.transaction {
                // Step 1: Delete local transactions for the sync scope
                val idsToDelete = getLocalTransactions(month).map { it.id }
                if (idsToDelete.isNotEmpty()) {
                    transactionQueries.deleteByIds(idsToDelete)
                }

                // Step 2: Insert the authoritative list from the server
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
                        is_deleted = dbTx.is_deleted
                    )
                }
            }

            _status.value = SyncStatus.Success(getTimeMillis())
            Logger.d("TransactionSyncManager") { "Sync successful." }
        } catch (e: Exception) {
            reportError("Failed to sync transactions: ${e.message}")
            Logger.e("TransactionSyncManager") { "Failed to sync transactions: ${e.message}" }
        }
    }

    override fun reportError(message: String) {
        _status.value = SyncStatus.Error(message)
    }

    private fun getLocalTransactions(month: YearMonth?): List<Transaction> {
        val query = if (month != null) {
            transactionQueries.selectBetween(month.toSqlStartDate(), month.toSqlStartOfNextMonthDate())
        } else {
            transactionQueries.selectAll()
        }
        return query.executeAsList().map { it.toDomain() }
    }
}
