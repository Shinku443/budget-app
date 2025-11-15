package com.projects.shinku443.budget_app.repository

import Category
import com.projects.shinku443.budget_app.api.ApiClient
import com.projects.shinku443.budget_app.model.CategoryType

class CategoryRepository(private val api: ApiClient) {
    suspend fun getCategories(): List<Category> =
        api.get("/categories")

    suspend fun createCategory(name: String, type: CategoryType): Category =
        api.post("/categories", mapOf("name" to name, "type" to type.name))
}
