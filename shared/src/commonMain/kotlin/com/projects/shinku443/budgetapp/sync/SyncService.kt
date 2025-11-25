package com.projects.shinku443.budgetapp.sync

import com.projects.shinku443.budgetapp.connectivity.ConnectivityMonitor
import com.projects.shinku443.budgetapp.connectivity.ConnectionState
import com.projects.shinku443.budgetapp.util.YearMonth
import com.projects.shinku443.budgetapp.viewmodel.UiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
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

    // Internal flow to track if any sync manager is actively syncing
    private val _isSyncActive = combine(
        transactionSyncManager.status,
        categorySyncManager.status
    ) { txStatus, catStatus ->
        txStatus is SyncStatus.Syncing || catStatus is SyncStatus.Syncing
    }.stateIn(serviceScope, SharingStarted.Eagerly, false)

    // Debounced version of _isSyncActive to prevent flickering for very fast syncs
    private val _showSyncingIndicator = _isSyncActive
        .debounce { isActive -> if (isActive) 500L else 0L } // Delay showing 'Syncing' for 500ms, but hide immediately
        .stateIn(serviceScope, SharingStarted.Eagerly, false)

    // New flow to indicate if a sync is visually in progress (for progress bar)
    val isSyncInProgress: StateFlow<Boolean> = _showSyncingIndicator

    // Combine the status of all sync managers into a single UI state for the banner.
    // This flow now focuses purely on network/error status, not sync activity.
    val overallState: StateFlow<UiState> = combine(
        transactionSyncManager.status,
        categorySyncManager.status,
        connectivityMonitor.connectionState
    ) { txStatus, catStatus, connection ->
        // 1. If disconnected, that's the most important state for the banner.
        if (connection is ConnectionState.Disconnected) {
            return@combine UiState.Offline
        }

        // 2. If connecting, show the connecting state.
        if (connection is ConnectionState.Connecting) {
            return@combine UiState.Connecting
        }

        // 3. If any manager has an error (and we're not disconnected or connecting).
        if (txStatus is SyncStatus.Error) {
            return@combine UiState.Error(txStatus.message)
        }
        if (catStatus is SyncStatus.Error) {
            return@combine UiState.Error(catStatus.message)
        }

        // 4. Otherwise, we are idle (connected, not in an error state).
        //    Syncing status is now handled by a separate flow (isSyncInProgress).
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
            // Removed the problematic connection check here.
            // Let individual sync managers handle API errors and report their status.

            // Launch sync jobs for each manager, wrapping each in a try-catch for robustness.
            launch {
                try {
                    transactionSyncManager.sync(month)
                } catch (e: Exception) {
                    // Fallback error reporting if an exception escapes the manager's internal handling.
                    transactionSyncManager.reportError(e.message ?: "Unknown transaction sync error")
                }
            }
            launch {
                try {
                    categorySyncManager.sync()
                } catch (e: Exception) {
                    // Fallback error reporting if an exception escapes the manager's internal handling.
                    categorySyncManager.reportError(e.message ?: "Unknown category sync error")
                }
            }
        }
    }
}
