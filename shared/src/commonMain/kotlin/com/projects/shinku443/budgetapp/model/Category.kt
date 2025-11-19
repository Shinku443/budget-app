package com.projects.shinku443.budgetapp.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String,
    val type: CategoryType,
    val isActive: Boolean = true,
    val updatedAt: Long? = null,
    val isDeleted: Boolean = false
)

@Serializable
data class CategoryRequest(
    val name: String,
    val type: CategoryType,
    val isActive: Boolean
)

@Serializable
enum class CategoryType { INCOME, EXPENSE, SAVINGS }