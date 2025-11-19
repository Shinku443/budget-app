package com.projects.shinku443.budgetapp.model

import com.projects.shinku443.budgetapp.util.YearMonth

data class BudgetGoal(
    val id: String,              // unique ID
    val yearMonth: YearMonth,        // which month the goal applies to
    val amount: Float,           // target amount
    val updatedAt: Long          // timestamp for sync conflict resolution
)
