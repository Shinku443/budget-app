package com.projects.shinku443.budget_app.model

import kotlinx.serialization.Serializable

@Serializable
enum class TransactionType { INCOME, EXPENSE }
@Serializable
enum class CategoryType { INCOME, EXPENSE }