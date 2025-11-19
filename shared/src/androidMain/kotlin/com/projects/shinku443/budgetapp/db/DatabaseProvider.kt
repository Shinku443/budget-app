package com.projects.shinku443.budgetapp.db

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

fun ProvideDatabase(context: Context): BudgetDatabase {
    val driver = AndroidSqliteDriver(BudgetDatabase.Schema, context, "budget.db")
    return DatabaseFactory.create(driver)
}
