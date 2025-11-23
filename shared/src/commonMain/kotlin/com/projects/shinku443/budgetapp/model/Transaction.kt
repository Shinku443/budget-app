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

fun Transaction.toTransactionRequest(): TransactionRequest {
    return TransactionRequest(
        amount = this.amount,
        type = this.type,
        categoryId = this.categoryId,
        date = this.date,
        description = this.description
    )
}

@Serializable
data class TransactionRequest(
    val amount: Double,
    val type: CategoryType,
    val categoryId: String,
    val date: String,
    val description: String? = null
)
