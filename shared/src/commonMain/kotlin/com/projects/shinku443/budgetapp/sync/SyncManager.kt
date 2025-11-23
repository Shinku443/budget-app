package com.projects.shinku443.budgetapp.sync

import com.projects.shinku443.budgetapp.util.YearMonth
import kotlinx.coroutines.flow.StateFlow

/**
 * Defines a contract for a class that can synchronize local and remote data.
 */
interface SyncManager {
    /**
     * A flow representing the current status of the sync manager.
     */
    val status: StateFlow<SyncStatus>

    /**
     * Performs a two-way synchronization.
     * @param month An optional month to scope the sync to.
     */
    suspend fun sync(month: YearMonth? = null)

    /**
     * Reports an error to the sync manager, updating its status.
     * This is a fallback for when an exception might escape the manager's internal handling.
     */
    fun reportError(message: String)
}

sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    data class Success(val timestamp: Long) : SyncStatus()
    data class Error(val message: String) : SyncStatus()
}
