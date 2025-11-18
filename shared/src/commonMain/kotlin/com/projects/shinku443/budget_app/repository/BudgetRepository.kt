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
        val remote = api.get<List<Transaction>>("/transactions?month=$month")
        remote.forEach { tx ->
            val dbTx = tx.toDb()
            transactionQueries.insertOrReplace(
                id = dbTx.id,
                amount = dbTx.amount,
                categoryId = dbTx.categoryId,
                date = dbTx.date,
                description = dbTx.description,
                type = dbTx.type,
                createdAt = dbTx.createdAt
            )
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
                updatedAt = dbCat.updatedAt
            )
        }
    }

    suspend fun addTransaction(tx: Transaction): Transaction {
        val created = api.post<Transaction>("/transactions", tx)
        val dbTx = created.toDb()
        transactionQueries.insertOrReplace(
            id = dbTx.id,
            amount = dbTx.amount,
            categoryId = dbTx.categoryId,
            date = dbTx.date,
            description = dbTx.description,
            type = dbTx.type,
            createdAt = dbTx.createdAt
        )
        return created
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
        )
        return created
    }

    suspend fun deleteTransaction(id: String): Boolean {
        return try {
            val response = api.client.delete("${api.baseUrl}/transactions/$id")
            if (response.status.value == 200) {
                transactionQueries.deleteById(id)
                true
            } else false
        } catch (e: Exception) {
            Logger.e("BudgetRepository") { "Failed to delete transaction: ${e.message}" }
            false
        }
    }
}

