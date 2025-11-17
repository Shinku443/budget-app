package com.projects.shinku443.budget_app.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.projects.shinku443.budget_app.api.ApiClient
import com.projects.shinku443.budget_app.db.BudgetDatabase
import com.projects.shinku443.budget_app.model.Category
import com.projects.shinku443.budget_app.model.CategoryRequest
import com.projects.shinku443.budget_app.model.CategoryType
import com.projects.shinku443.budget_app.util.mapper.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepository(
    private val api: ApiClient,
    private val db: BudgetDatabase
) {
    // UI always observes local DB
    fun observeCategories(): Flow<List<Category>> =
        db.categoryQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { it.map { dbCat -> dbCat.toDomain() } }

    // Online-first refresh
    suspend fun refreshCategories(): List<Category> {
        return try {
            val remote = api.get<List<Category>>("/categories")
            remote.forEach {
                db.categoryQueries.insertOrReplace(
                    id = it.id,
                    name = it.name,
                    type = it.type.name,
                    isActive = if (it.isActive) 1L else 0L,
                    updatedAt = it.updatedAt
                )
            }
            remote
        } catch (e: Exception) {
            db.categoryQueries.selectAll().executeAsList().map { it.toDomain() }
        }
    }

    suspend fun createCategory(name: String, type: CategoryType, isActive: Boolean): Category {
        val created = api.post<Category>("/categories", CategoryRequest(name, type, isActive))
        db.categoryQueries.insertOrReplace(
            id = created.id,
            name = created.name,
            type = created.type.name,
            isActive = if (created.isActive) 1L else 0L,
            updatedAt = created.updatedAt
        )
        return created
    }

    suspend fun updateCategory(id: String, name: String, type: CategoryType, isActive: Boolean): Category {
        val updated = api.put<Category>("/categories/$id", CategoryRequest(name, type, isActive))
        db.categoryQueries.insertOrReplace(
            id = updated.id,
            name = updated.name,
            type = updated.type.name,
            isActive = if (updated.isActive) 1L else 0L,
            updatedAt = updated.updatedAt
        )
        return updated
    }

    suspend fun deleteCategory(id: String) {
        try {
            api.delete<Unit>("/categories/$id")
            db.categoryQueries.deleteById(id)
        } catch (e: Exception) {
            // optional: mark for deletion later
        }
    }
}
