package com.projects.shinku443.budget_app.util.mapper

import com.projects.shinku443.budgetapp.db.AppTransaction as DbTransaction
import com.projects.shinku443.budget_app.model.Transaction as DomainTransaction
import com.projects.shinku443.budget_app.model.CategoryType

fun DbTransaction.toDomain(): DomainTransaction =
    DomainTransaction(
        id = id,
        amount = amount,
        type = CategoryType.valueOf(type),
        categoryId = categoryId,
        date = date,
        description = description,
        createdAt = createdAt,
        isDeleted = is_deleted == 1L
    )

fun DomainTransaction.toDb(): DbTransaction =
    DbTransaction(
        id = id,
        amount = amount,
        type = type.name,
        categoryId = categoryId,
        date = date,
        description = description,
        createdAt = createdAt,
        is_deleted = if (isDeleted) 1L else 0L
    )
