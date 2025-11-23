package com.projects.shinku443.budgetapp.sync

import com.projects.shinku443.budgetapp.connectivity.ConnectivityMonitor
import com.projects.shinku443.budgetapp.connectivity.ConnectionState
import com.projects.shinku443.budgetapp.util.YearMonth
import com.projects.shinku443.budgetapp.viewmodel.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Orchestrates the synchronization of all data types in the application and reports the overall status.
 */
class SyncService(
    private val transactionSyncManager: TransactionSyncManager,
    private val categorySyncManager: CategorySyncManager,
    private val connectivityMonitor: ConnectivityMonitor
) {
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Combine the status of all sync managers into a single UI state.
    val overallState: StateFlow<UiState> = combine(
        transactionSyncManager.status,
        categorySyncManager.status,
        connectivityMonitor.connectionState
    ) { txStatus, catStatus, connection ->
        // If disconnected, that's the most important state.
        if (connection is ConnectionState.Disconnected) {
            return@combine UiState.Offline
        }

        // If any manager is syncing, the overall state is Syncing.
        if (txStatus is SyncStatus.Syncing || catStatus is SyncStatus.Syncing) {
            return@combine UiState.Syncing
        }

        // If any manager has an error, report the first error found.
        if (txStatus is SyncStatus.Error) {
            return@combine UiState.Error(txStatus.message)
        }
        if (catStatus is SyncStatus.Error) {
            return@combine UiState.Error(catStatus.message)
        }

        // Otherwise, we are idle.
        UiState.Idle
    }.stateIn(
        scope = serviceScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Idle
    )

    /**
     * Performs a full synchronization of all application data.
     * This is now a non-blocking call that launches the sync jobs in the background.
     * The UI will reactively update by observing the `overallState` flow.
     */
    fun syncAll(month: YearMonth? = null) {
        serviceScope.launch {
            // Wait until the connection is confirmed before proceeding.
            // This avoids a race condition on app startup.
            connectivityMonitor.connectionState.first { it is ConnectionState.Connected }

            // Launch sync jobs for each manager.
            // The `overallState` will automatically update as each manager changes its status.
            launch { transactionSyncManager.sync(month) }
            launch { categorySyncManager.sync() }
        }
    }
}
