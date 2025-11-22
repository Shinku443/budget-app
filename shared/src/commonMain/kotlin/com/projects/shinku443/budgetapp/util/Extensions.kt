package com.projects.shinku443.budgetapp.util

// kotlin

// Top-level extension helpers safe for KMP commonMain

fun YearMonth.toSqlStartDate(): String {
    val y = year.toString().padStart(4, '0')
    val m = month.toString().padStart(2, '0')
    return "$y-$m-01"
}

fun YearMonth.toSqlStartOfNextMonthDate(): String {
    val (ny, nm) = if (month == 12) Pair(year + 1, 1) else Pair(year, month + 1)
    return "${ny.toString().padStart(4, '0')}-${nm.toString().padStart(2, '0')}-01"
}

fun YearMonth.toMonthPrefix(): String =
    "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}"

// Usage examples:
//
// In-memory UI/filtering:
// val prefix = selectedMonthLocal.toMonthPrefix() // "2025-11"
// val currentMonthTxs = allTransactions.filter { it.date.startsWith(prefix) }
//
// For SQL/queries (preferred: range query):
// val start = month.toSqlStartDate()            // "2025-11-01"
// val nextStart = month.toSqlStartOfNextMonthDate() // "2025-12-01"
// db.transactionsQueries.selectBetween(start, nextStart)...
