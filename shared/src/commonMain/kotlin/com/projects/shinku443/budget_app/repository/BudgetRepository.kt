package com.projects.shinku443.budget_app.repository

import Category
import Transaction
import co.touchlab.kermit.Logger
import com.projects.shinku443.budget_app.api.ApiClient
import com.projects.shinku443.budget_app.model.*
import io.ktor.client.request.delete

class BudgetRepository(private val api: ApiClient) {
    suspend fun getTransactions(month: String): List<Transaction> =
        api.get("/transactions?month=$month")

    suspend fun addTransaction(tx: Transaction): Transaction =
        api.post("/transactions", tx)

    suspend fun getCategories(): List<Category> =
        api.get("/categories")

    suspend fun createCategory(name: String, type: CategoryType): Category =
        api.post("/categories", mapOf("name" to name, "type" to type.name))

    suspend fun deleteTransaction(id: String): Boolean {
        // Assuming backend supports DELETE
        return try {
            api.client.delete("${api.baseUrl}/transactions/$id").status.value == 200
        } catch (e: Exception) {
            Logger.e("BudgetRepository") { "Failed to delete transaction: ${e.message}" }
            false
        }
    }
}
