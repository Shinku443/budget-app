package com.projects.shinku443.budget_app.repository

import app.cash.sqldelight.coroutines.asFlow
import co.touchlab.kermit.Logger
import com.projects.shinku443.budget_app.api.ApiClient
import com.projects.shinku443.budget_app.db.BudgetDatabase
import com.projects.shinku443.budget_app.model.Category
import com.projects.shinku443.budget_app.model.CategoryType
import com.projects.shinku443.budget_app.model.Transaction
import com.projects.shinku443.budget_app.util.mapper.toDb
import com.projects.shinku443.budget_app.util.mapper.toDomain
import io.ktor.client.request.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BudgetRepository(
    private val api: ApiClient,
    private val db: BudgetDatabase
) {
    private val categoryQueries = db.categoryQueries
    private val transactionQueries = db.transactionQueries

    fun observeTransactions(): Flow<List<Transaction>> =
        transactionQueries.selectAll().asFlow().map { it.executeAsList().map { it.toDomain() } }

    fun observeCategories(): Flow<List<Category>> =
        categoryQueries.selectAll().asFlow().map { it.executeAsList().map { it.toDomain() } }

    suspend fun refreshTransactions(month: String) {
        try {
            val remoteTxs = api.get<List<Transaction>>("/transactions")
            val remoteIds = remoteTxs.map { it.id }.toSet()

            // Sync offline deletions
            val deletedTxs = transactionQueries.selectAllIncludingDeleted().executeAsList().filter { it.is_deleted == 1L }
            deletedTxs.forEach { tx ->
                if (tx.id in remoteIds) {
                    try {
                        api.client.delete("${api.baseUrl}/transactions/${tx.id}")
                        transactionQueries.deleteById(tx.id)
                    } catch (e: Exception) {
                        Logger.e("BudgetRepository") { "Failed to sync deleted transaction: ${e.message}" }
                    }
                } else {
                    // Transaction was created and deleted offline, just remove it locally
                    transactionQueries.deleteById(tx.id)
                }
            }


            // Sync offline creations
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
                        categoryId = dbTx.categoryId,
                        date = dbTx.date,
                        description = dbTx.description,
                        type = dbTx.type,
                        createdAt = dbTx.createdAt,
                        is_deleted = 0
                    )
                } catch (e: Exception) {
                    Logger.e("BudgetRepository") { "Failed to sync offline transaction: ${e.message}" }
                }
            }

            // Final refresh from remote
            val finalRemoteTxs = api.get<List<Transaction>>("/transactions?month=$month")
            finalRemoteTxs.forEach { tx ->
                val dbTx = tx.toDb()
                transactionQueries.insertOrReplace(
                    id = dbTx.id,
                    amount = dbTx.amount,
                    categoryId = dbTx.categoryId,
                    date = dbTx.date,
                    description = dbTx.description,
                    type = dbTx.type,
                    createdAt = dbTx.createdAt,
                    is_deleted = 0
                )
            }
        } catch (e: Exception) {
            Logger.e("BudgetRepository") { "Failed to refresh transactions: ${e.message}" }
        }
    }

    suspend fun refreshCategories() {
        val remote = api.get<List<Category>>("/categories")
        remote.forEach { cat ->
            val dbCat = cat.toDb()
            categoryQueries.insertOrReplace(
                id = dbCat.id,
                name = dbCat.name,
                type = dbCat.type,
                isActive = dbCat.isActive,
                updatedAt = dbCat.updatedAt,
                is_deleted = dbCat.is_deleted
            )
        }
    }

    suspend fun addTransaction(tx: Transaction): Transaction {
        try {
            val created = api.post<Transaction>("/transactions", tx)
            val dbTx = created.toDb()
            transactionQueries.insertOrReplace(
                id = dbTx.id,
                amount = dbTx.amount,
                categoryId = dbTx.categoryId,
                date = dbTx.date,
                description = dbTx.description,
                type = dbTx.type,
                createdAt = dbTx.createdAt,
                is_deleted = 0
            )
            return created
        } catch (e: Exception) {
            Logger.e("BudgetRepository") { "Failed to add transaction: ${e.message}" }
            val dbTx = tx.toDb()
            transactionQueries.insertOrReplace(
                id = dbTx.id,
                amount = dbTx.amount,
                categoryId = dbTx.categoryId,
                date = dbTx.date,
                description = dbTx.description,
                type = dbTx.type,
                createdAt = dbTx.createdAt,
                is_deleted = 0
            )
            return tx
        }
    }

    suspend fun createCategory(name: String, type: CategoryType): Category {
        val created = api.post<Category>("/categories", mapOf("name" to name, "type" to type.name))
        val dbCat = created.toDb()
        categoryQueries.insertOrReplace(
            id = dbCat.id,
            name = dbCat.name,
            type = dbCat.type,
            isActive = dbCat.isActive,
            updatedAt = dbCat.updatedAt,
            is_deleted = dbCat.is_deleted
        )
        return created
    }

    suspend fun deleteTransaction(id: String): Boolean {
        return try {
            val response = api.client.delete("${api.baseUrl}/transactions/$id")
            if (response.status.value in 200..299) {
                transactionQueries.deleteById(id)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Logger.e("BudgetRepository") { "Failed to delete transaction, marking as deleted: ${e.message}" }
            transactionQueries.markAsDeleted(id)
            true
        }
    }
}
