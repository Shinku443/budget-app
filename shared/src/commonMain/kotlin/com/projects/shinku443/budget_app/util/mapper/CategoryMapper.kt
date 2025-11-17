package com.projects.shinku443.budget_app.util.mapper

import com.projects.shinku443.budgetapp.db.Category as DbCategory
import com.projects.shinku443.budget_app.model.Category as DomainCategory
import com.projects.shinku443.budget_app.model.CategoryType

fun DbCategory.toDomain(): DomainCategory =
    DomainCategory(
        id = id,
        name = name,
        type = CategoryType.valueOf(type),
        isActive = isActive != 0L,
        updatedAt = updatedAt
    )

fun DomainCategory.toDb(): DbCategory =
    DbCategory(
        id = id,
        name = name,
        type = type.name,
        isActive = if (isActive) 1L else 0L,
        updatedAt = updatedAt
    )
