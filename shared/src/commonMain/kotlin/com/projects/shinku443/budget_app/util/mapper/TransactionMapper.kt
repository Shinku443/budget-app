package com.projects.shinku443.budget_app.util.mapper

import com.projects.shinku443.budgetapp.db.AppTransaction as DbTransaction
import com.projects.shinku443.budget_app.model.Transaction as DomainTransaction
import com.projects.shinku443.budget_app.model.CategoryType

fun DbTransaction.toDomain(): DomainTransaction =
    DomainTransaction(
        id = id,
        amount = amount,
        categoryType = CategoryType.valueOf(categoryType),
        categoryId = categoryId,
        date = date,
        description = description,
        createdAt = createdAt
    )

fun DomainTransaction.toDb(): DbTransaction =
    DbTransaction(
        id = id,
        amount = amount,
        categoryType = categoryType.name,
        categoryId = categoryId,
        date = date,
        description = description,
        createdAt = createdAt
    )
