package com.projects.shinku443.budget_app.db

import app.cash.sqldelight.db.SqlDriver

object DatabaseFactory {
    fun create(driver: SqlDriver): BudgetDatabase {
        return BudgetDatabase(driver)
    }
}
