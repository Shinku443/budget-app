package com.projects.shinku443.budgetapp.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import co.touchlab.kermit.Logger
import com.projects.shinku443.budgetapp.api.ApiClient
import com.projects.shinku443.budgetapp.db.BudgetDatabase
import com.projects.shinku443.budgetapp.model.Category
import com.projects.shinku443.budgetapp.model.CategoryRequest
import com.projects.shinku443.budgetapp.model.CategoryType
import com.projects.shinku443.budgetapp.util.TimeWrapper
import com.projects.shinku443.budgetapp.util.mapper.toDb
import com.projects.shinku443.budgetapp.util.mapper.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepository(
    private val api: ApiClient,
    private val db: BudgetDatabase
) {
    private val categoryQueries = db.categoryQueries

    fun observeCategories(): Flow<List<Category>> =
        categoryQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.map { dbCat -> dbCat.toDomain() } }

    suspend fun createCategory(name: String, type: CategoryType, isActive: Boolean): Category {
        return try {
            val created = api.post<Category>("/categories", CategoryRequest(name, type, isActive))
            val dbCat = created.toDb()
            categoryQueries.insertOrReplace(
                id = dbCat.id,
                name = dbCat.name,
                type = dbCat.type,
                isActive = dbCat.isActive,
                updatedAt = dbCat.updatedAt,
                is_deleted = dbCat.is_deleted
            )
            created
        } catch (e: Exception) {
            Logger.e("CategoryRepository") { "Failed to create category online, saving locally: ${e.message}" }
            val localCategory = Category(
                id = "local_${TimeWrapper.currentTimeMillis()}",
                name = name,
                type = type,
                isActive = isActive,
                updatedAt = TimeWrapper.currentTimeMillis(),
                isDeleted = false // Not deleted when created locally
            )
            val dbCat = localCategory.toDb()
            categoryQueries.insertOrReplace(
                id = dbCat.id,
                name = dbCat.name,
                type = dbCat.type,
                isActive = dbCat.isActive,
                updatedAt = dbCat.updatedAt,
                is_deleted = dbCat.is_deleted
            )
            localCategory
        }
    }

    suspend fun updateCategory(id: String, name: String, type: CategoryType, isActive: Boolean): Category {
        return try {
            val updated = api.put<Category>("/categories/$id", CategoryRequest(name, type, isActive))
            val dbCat = updated.toDb()
            categoryQueries.insertOrReplace(
                id = dbCat.id,
                name = dbCat.name,
                type = dbCat.type,
                isActive = dbCat.isActive,
                updatedAt = dbCat.updatedAt,
                is_deleted = dbCat.is_deleted
            )
            updated
        } catch (e: Exception) {
            Logger.e("CategoryRepository") { "Failed to update category online, updating locally: ${e.message}" }
            // Fetch existing category to preserve isDeleted status if it was already marked for deletion
            val existingCategory = categoryQueries.selectById(id).executeAsOneOrNull()?.toDomain()
            val updatedCategory = Category(
                id = id,
                name = name,
                type = type,
                isActive = isActive,
                updatedAt = TimeWrapper.currentTimeMillis(),
                isDeleted = existingCategory?.isDeleted ?: false // Preserve existing deleted status
            )
            val dbCat = updatedCategory.toDb()
            categoryQueries.insertOrReplace(
                id = dbCat.id,
                name = dbCat.name,
                type = dbCat.type,
                isActive = dbCat.isActive,
                updatedAt = dbCat.updatedAt,
                is_deleted = dbCat.is_deleted
            )
            updatedCategory
        }
    }

    suspend fun deleteCategory(id: String) {
        try {
            api.delete<Unit>("/categories/$id")
            categoryQueries.deleteById(id)
        } catch (e: Exception) {
            Logger.e("CategoryRepository") { "Failed to delete category online, marking for deletion: ${e.message}" }
            categoryQueries.markAsDeleted(id)
        }
    }
}
