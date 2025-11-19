package com.projects.shinku443.budgetapp.util.mapper

import com.projects.shinku443.budgetapp.db.BudgetGoal as DbBudgetGoal
import com.projects.shinku443.budgetapp.model.BudgetGoal as DomainBudgetGoal
import com.projects.shinku443.budgetapp.util.YearMonth

// DB → Domain
fun DbBudgetGoal.toDomain(): DomainBudgetGoal {
    return DomainBudgetGoal(
        id = id,
        yearMonth = YearMonth(year.toInt(), month.toInt()),
        amount = amount.toFloat(),
        updatedAt = updatedAt
    )
}

// Domain → DB
fun DomainBudgetGoal.toDb(): DbBudgetGoal {
    return DbBudgetGoal(
        id = id,
        year = yearMonth.year.toLong(),
        month = yearMonth.month.toLong(),
        amount = amount.toDouble(),
        updatedAt = updatedAt
    )
}
