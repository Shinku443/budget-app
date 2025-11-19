package com.projects.shinku443.budgetapp.model

import kotlinx.serialization.Serializable

@Serializable
data class Transaction(
    val id: String,
    val amount: Double,
    val type: CategoryType,
    val categoryId: String,   // ✅ store just the ID
    val date: String,
    val description: String? = null,
    val createdAt: Long,
    val isDeleted: Boolean = false
)
