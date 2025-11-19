package com.projects.shinku443.budget_app.sync

import com.projects.shinku443.budget_app.util.YearMonth

/**
 * Defines a contract for a class that can synchronize local and remote data.
 */
interface SyncManager {
    /**
     * Performs a two-way synchronization.
     * @param month An optional month to scope the sync to.
     */
    suspend fun sync(month: YearMonth? = null)
}
