package com.projects.shinku443.budget_app.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import co.touchlab.kermit.Logger
import com.projects.shinku443.budget_app.api.ApiClient
import com.projects.shinku443.budget_app.db.BudgetDatabase
import com.projects.shinku443.budget_app.model.Transaction
import com.projects.shinku443.budget_app.util.mapper.toDb
import com.projects.shinku443.budget_app.util.mapper.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepository(
    private val api: ApiClient,
    private val db: BudgetDatabase
) {
    private val transactionQueries = db.transactionQueries

    fun observeTransactions(): Flow<List<Transaction>> =
        transactionQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    suspend fun createTransaction(tx: Transaction): Transaction {
        return try {
            val created = api.post<Transaction>("/transactions", tx)
            val dbTx = created.toDb()
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
            created
        } catch (e: Exception) {
            Logger.e("TransactionRepository") { "Failed to create transaction online, saving locally: ${e.message}" }
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
            tx
        }
    }

    suspend fun deleteTransaction(id: String) {
        try {
            api.delete<Unit>("/transactions/$id")
            transactionQueries.deleteById(id)
        } catch (e: Exception) {
            Logger.e("TransactionRepository") { "Failed to delete transaction online, marking for deletion: ${e.message}" }
            transactionQueries.markAsDeleted(id)
        }
    }
}
