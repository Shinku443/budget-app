package com.projects.shinku443.budgetapp.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: String,
    val name: String,
    val type: CategoryType,
    val isActive: Boolean = true,
    val updatedAt: Long? = null,
    val isDeleted: Boolean = false,
    val color: Long = 0xFF2196F3, // default blue
    val icon: String? = null
)

@Serializable
data class CategoryRequest(
    val name: String,
    val type: CategoryType,
    val isActive: Boolean,
    val color: Long,
    val icon: String?
)

data class CategoryPreset(
    val displayName: String,
    val iconName: String, // e.g. "ic_category_groceries"
    val color: Long
)

val categoryPresets = listOf(
    CategoryPreset("Groceries", "ic_category_groceries", 0xFFE57373),
    CategoryPreset("Entertainment", "ic_category_entertainment", 0xFF9C27B0),
    CategoryPreset("Transport", "ic_category_transport", 0xFF81C784),
    CategoryPreset("Shopping", "ic_category_shopping", 0xFFFFB74D),
    CategoryPreset("Savings", "ic_category_savings", 0xFF4CAF50),
    // … add more
)


@Serializable
enum class CategoryType { INCOME, EXPENSE, SAVINGS }