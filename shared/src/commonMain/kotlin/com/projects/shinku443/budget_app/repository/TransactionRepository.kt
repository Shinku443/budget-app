package com.projects.shinku443.budget_app.repository


import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.projects.shinku443.budget_app.api.ApiClient
import com.projects.shinku443.budget_app.db.BudgetDatabase
import com.projects.shinku443.budget_app.model.Transaction
import com.projects.shinku443.budget_app.util.mapper.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepository(
    private val api: ApiClient,
    private val db: BudgetDatabase
) {
    // Reactive stream from local DB
    fun observeTransactions(): Flow<List<Transaction>> =
        db.transactionQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list -> list.map { it.toDomain() } }

    // Online-first refresh
    suspend fun refreshTransactions(): List<Transaction> {
        return try {
            val remote = api.get<List<Transaction>>("/transactions")
            remote.forEach {
                db.transactionQueries.insertOrReplace(
                    id = it.id,
                    amount = it.amount,
                    categoryType = it.categoryType.name,
                    categoryId = it.categoryId,
                    date = it.date,
                    description = it.description,
                    createdAt = it.createdAt
                )
            }
            remote
        } catch (e: Exception) {
            db.transactionQueries.selectAll().executeAsList().map { it.toDomain() }
        }
    }

    suspend fun createTransaction(tx: Transaction): Transaction {
        val created = api.post<Transaction>("/transactions", tx)
        db.transactionQueries.insertOrReplace(
            id = created.id,
            amount = created.amount,
            categoryType = created.categoryType.name,
            categoryId = created.categoryId,
            date = created.date,
            description = created.description,
            createdAt = created.createdAt
        )
        return created
    }

    suspend fun deleteTransaction(id: String) {
        try {
            api.delete<Unit>("/transactions/$id")
            db.transactionQueries.deleteById(id)
        } catch (e: Exception) {
            // optional: mark for deletion later
        }
    }
}
