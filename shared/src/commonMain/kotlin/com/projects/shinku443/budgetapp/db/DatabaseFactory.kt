package com.projects.shinku443.budgetapp.db

import app.cash.sqldelight.db.SqlDriver

object DatabaseFactory {
    fun create(driver: SqlDriver): BudgetDatabase {
        return BudgetDatabase(driver)
    }
}
