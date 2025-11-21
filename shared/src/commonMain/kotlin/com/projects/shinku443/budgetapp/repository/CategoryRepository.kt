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

    suspend fun createCategory(name: String, type: CategoryType, isActive: Boolean, color: Long, icon: String?): Category {
        return try {
            val created = api.post<Category>("/categories", CategoryRequest(name, type, isActive, color, icon))
            val dbCat = created.toDb()
            categoryQueries.insertOrReplace(
                id = dbCat.id,
                name = dbCat.name,
                type = dbCat.type,
                isActive = dbCat.isActive,
                updatedAt = dbCat.updatedAt,
                is_deleted = dbCat.is_deleted,
                color = dbCat.color,
                icon = dbCat.icon
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
                isDeleted = false, // Not deleted when created locally
                color = color,
                icon = icon
            )
            val dbCat = localCategory.toDb()
            categoryQueries.insertOrReplace(
                id = dbCat.id,
                name = dbCat.name,
                type = dbCat.type,
                isActive = dbCat.isActive,
                updatedAt = dbCat.updatedAt,
                is_deleted = dbCat.is_deleted,
                color = dbCat.color,
                icon = dbCat.icon
            )
            localCategory
        }
    }

    suspend fun updateCategory(id: String, name: String, type: CategoryType, isActive: Boolean, color: Long, icon: String): Category {
        return try {
            val updated = api.put<Category>("/categories/$id", CategoryRequest(name, type, isActive, color, icon))
            val dbCat = updated.toDb()
            categoryQueries.insertOrReplace(
                id = dbCat.id,
                name = dbCat.name,
                type = dbCat.type,
                isActive = dbCat.isActive,
                updatedAt = dbCat.updatedAt,
                is_deleted = dbCat.is_deleted,
                color = dbCat.color,
                icon = dbCat.icon
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
                is_deleted = dbCat.is_deleted,
                color = dbCat.color,
                icon = dbCat.icon
            )
            updatedCategory
        }
    }

    suspend fun renameCategory(id: String, newName: String): Category {
        return try {
            val updated = api.patch<Category>("/categories/$id", mapOf("name" to newName))
            val dbCat = updated.toDb()
            categoryQueries.insertOrReplace(
                id = dbCat.id,
                name = dbCat.name,
                type = dbCat.type,
                isActive = dbCat.isActive,
                updatedAt = dbCat.updatedAt,
                is_deleted = dbCat.is_deleted,
                color = dbCat.color,
                icon = dbCat.icon
            )
            updated
        } catch (e: Exception) {
            Logger.e("CategoryRepository") { "Failed to rename category online, updating locally: ${e.message}" }
            val existing = categoryQueries.selectById(id).executeAsOneOrNull()?.toDomain()
            val renamed = existing?.copy(
                name = newName,
                updatedAt = TimeWrapper.currentTimeMillis()
            ) ?: Category(
                id = id,
                name = newName,
                type = CategoryType.EXPENSE,
                isActive = true,
                updatedAt = TimeWrapper.currentTimeMillis(),
                isDeleted = false,
                color = 0xFF64B5F6,
                icon = null
            )
            val dbCat = renamed.toDb()
            categoryQueries.insertOrReplace(
                id = dbCat.id,
                name = dbCat.name,
                type = dbCat.type,
                isActive = dbCat.isActive,
                updatedAt = dbCat.updatedAt,
                is_deleted = dbCat.is_deleted,
                color = dbCat.color,
                icon = dbCat.icon
            )
            renamed
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

    suspend fun deleteCategories(ids: List<String>) {
        try {
            api.delete<Unit>("/categories", bodyObj = ids)
            categoryQueries.deleteByIds(ids)
        } catch (e: Exception) {
            Logger.e("CategoryRepository") { "Failed to delete categories online, marking for deletion: ${e.message}" }
            categoryQueries.transaction {
                ids.forEach {
                    categoryQueries.markAsDeleted(it)
                }
            }
        }
    }
}
