package com.projects.shinku443.budget_app.repository

import com.projects.shinku443.budget_app.model.Category
import com.projects.shinku443.budget_app.api.ApiClient
import com.projects.shinku443.budget_app.model.CategoryRequest
import com.projects.shinku443.budget_app.model.CategoryType
import kotlinx.serialization.Serializable


class CategoryRepository(private val api: ApiClient) {
    suspend fun getCategories(): List<Category> =
        api.get("/categories")

    suspend fun createCategory(name: String, type: CategoryType): Category =
        api.post("/categories", CategoryRequest(name, type))

    suspend fun updateCategory(id: String, name: String, type: CategoryType): Category =
        api.put("/categories/$id", CategoryRequest(name, type))

    suspend fun deleteCategory(id: String) {
        api.delete<Unit>("/categories/$id")
    }

    suspend fun deleteCategories(ids: List<String>) {
        ids.forEach { deleteCategory(it) }
    }
}
