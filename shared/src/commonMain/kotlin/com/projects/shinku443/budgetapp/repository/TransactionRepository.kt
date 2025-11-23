package com.projects.shinku443.budgetapp.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import co.touchlab.kermit.Logger
import com.projects.shinku443.budgetapp.api.ApiClient
import com.projects.shinku443.budgetapp.db.BudgetDatabase
import com.projects.shinku443.budgetapp.model.Transaction
import com.projects.shinku443.budgetapp.util.YearMonth
import com.projects.shinku443.budgetapp.util.mapper.toDb
import com.projects.shinku443.budgetapp.util.mapper.toDomain
import com.projects.shinku443.budgetapp.util.toSqlStartDate
import com.projects.shinku443.budgetapp.util.toSqlStartOfNextMonthDate
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
            Logger.d("TransactionRepository") { "Deleting transaction $id" }
            api.delete<Unit>("/transactions/$id")
            transactionQueries.deleteById(id)
        } catch (e: Exception) {
            Logger.e("TransactionRepository") { "Failed to delete transaction online, marking for deletion: ${e.message}" }
            transactionQueries.markAsDeleted(id)
        }
    }

    suspend fun deleteTransactions(ids: List<String>) {
        try {
            api.delete<Unit>("/transactions", bodyObj = ids)
            transactionQueries.deleteByIds(ids)
        } catch (e: Exception) {
            Logger.e("TransactionRepository") { "Failed to delete transaction online, marking them for deletion: ${e.message}" }
            transactionQueries.markAsDeletedByIds(ids)
        }
    }

    suspend fun updateTransaction(tx: Transaction): Transaction {
        return try {
            val updated = api.put<Transaction>("/transactions/${tx.id}", tx)
            val dbTx = updated.toDb()
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
            updated
        } catch (e: Exception) {
            Logger.e("TransactionRepository") { "Failed to update transaction online, updating locally: ${e.message}" }
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

    fun observeTransactionsForMonth(yearMonth: YearMonth): Flow<List<Transaction>> {
        val start = yearMonth.toSqlStartDate()
        val next = yearMonth.toSqlStartOfNextMonthDate()
        return db.transactionQueries.selectBetween(start, next)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }
    }

}
