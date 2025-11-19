package com.projects.shinku443.budgetapp.db

import app.cash.sqldelight.driver.native.NativeSqliteDriver

fun ProvideDatabase(): BudgetDatabase {
    val driver = NativeSqliteDriver(BudgetDatabase.Schema, "budget.db")
    return DatabaseFactory.create(driver)
}
