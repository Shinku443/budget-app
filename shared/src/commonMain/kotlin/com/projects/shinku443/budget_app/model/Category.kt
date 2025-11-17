package com.projects.shinku443.budget_app.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String,
    val type: CategoryType,
    val isActive: Boolean = true,
    val updatedAt: Long
)

@Serializable
data class CategoryRequest(
    val name: String,
    val type: CategoryType,
    val isActive: Boolean
)
