package com.projects.shinku443.budgetapp.repository

import app.cash.sqldelight.coroutines.asFlow
import co.touchlab.kermit.Logger
import com.projects.shinku443.budgetapp.api.ApiClient
import com.projects.shinku443.budgetapp.db.BudgetDatabase
import com.projects.shinku443.budgetapp.model.Category
import com.projects.shinku443.budgetapp.model.CategoryType
import com.projects.shinku443.budgetapp.model.Transaction
import com.projects.shinku443.budgetapp.model.BudgetGoal
import com.projects.shinku443.budgetapp.util.YearMonth
import com.projects.shinku443.budgetapp.util.mapper.toDb
import com.projects.shinku443.budgetapp.util.mapper.toDomain
import io.ktor.client.request.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Interface definition
interface IBudgetRepository {
    fun observeTransactions(): Flow<List<Transaction>>
    fun observeCategories(): Flow<List<Category>>
    fun observeBudgetGoal(yearMonth: YearMonth): Flow<BudgetGoal?>

    suspend fun refreshTransactions(month: String)
    suspend fun refreshCategories()
    suspend fun refreshBudgetGoal(yearMonth: YearMonth)

    suspend fun addTransaction(tx: Transaction): Transaction
    suspend fun deleteTransaction(id: String): Boolean
    suspend fun createCategory(name: String, type: CategoryType): Category

    suspend fun setBudgetGoal(goal: BudgetGoal)
    suspend fun deleteBudgetGoal(yearMonth: YearMonth)
}

// Implementation
class BudgetRepository(
    private val api: ApiClient,
    private val db: BudgetDatabase
) : IBudgetRepository {

    private val categoryQueries = db.categoryQueries
    private val transactionQueries = db.transactionQueries
    private val budgetGoalQueries = db.budgetGoalQueries

    override fun observeTransactions(): Flow<List<Transaction>> =
        transactionQueries.selectAll().asFlow().map { it.executeAsList().map { it.toDomain() } }

    override fun observeCategories(): Flow<List<Category>> =
        categoryQueries.selectAll().asFlow().map { it.executeAsList().map { it.toDomain() } }

    override fun observeBudgetGoal(yearMonth: YearMonth): Flow<BudgetGoal?> =
        budgetGoalQueries.selectByMonth(yearMonth.year.toLong(), yearMonth.month.toLong()).asFlow().map {
            it.executeAsOneOrNull()?.toDomain()
        }

    override suspend fun refreshTransactions(month: String) {
        try {
            val remoteTxs = api.get<List<Transaction>>("/transactions?month=$month")
            remoteTxs.forEach { tx ->
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

    override suspend fun refreshCategories() {
        try {
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
        } catch (e: Exception) {
            Logger.e("BudgetRepository") { "Failed to refresh categories: ${e.message}" }
        }
    }

    override suspend fun refreshBudgetGoal(yearMonth: YearMonth) {
        try {
            val remoteGoal = api.get<BudgetGoal>("/budgetGoal?year=${yearMonth.year}&month=${yearMonth.month}")
            val dbGoal = remoteGoal.toDb()
            budgetGoalQueries.insertOrReplace(
                id = dbGoal.id,
                year = dbGoal.year,
                month = dbGoal.month,
                amount = dbGoal.amount,
                updatedAt = dbGoal.updatedAt
            )
        } catch (e: Exception) {
            Logger.e("BudgetRepository") { "Failed to refresh budget goal: ${e.message}" }
        }
    }

    override suspend fun addTransaction(tx: Transaction): Transaction {
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

    override suspend fun deleteTransaction(id: String): Boolean {
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

    override suspend fun createCategory(name: String, type: CategoryType): Category {
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

    override suspend fun setBudgetGoal(goal: BudgetGoal) {
        try {
            val remote = api.post<BudgetGoal>("/budgetGoal", goal)
            val dbGoal = remote.toDb()
            budgetGoalQueries.insertOrReplace(
                id = dbGoal.id,
                year = dbGoal.year,
                month = dbGoal.month,
                amount = dbGoal.amount,
                updatedAt = dbGoal.updatedAt
            )
        } catch (e: Exception) {
            Logger.e("BudgetRepository") { "Failed to set budget goal offline: ${e.message}" }
            val dbGoal = goal.toDb()
            budgetGoalQueries.insertOrReplace(
                id = dbGoal.id,
                year = dbGoal.year,
                month = dbGoal.month,
                amount = dbGoal.amount,
                updatedAt = dbGoal.updatedAt
            )
        }
    }

    override suspend fun deleteBudgetGoal(yearMonth: YearMonth) {
        try {
            api.client.delete("${api.baseUrl}/budgetGoal?year=${yearMonth.year}&month=${yearMonth.month}")
            budgetGoalQueries.deleteByMonth(yearMonth.year.toLong(), yearMonth.month.toLong())
        } catch (e: Exception) {
            Logger.e("BudgetRepository") { "Failed to delete budget goal: ${e.message}" }
            budgetGoalQueries.deleteByMonth(yearMonth.year.toLong(), yearMonth.month.toLong())
        }
    }
}
