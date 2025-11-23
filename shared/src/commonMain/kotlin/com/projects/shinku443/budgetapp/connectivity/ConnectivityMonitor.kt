package com.projects.shinku443.budgetapp.connectivity

import kotlinx.coroutines.flow.StateFlow

expect class ConnectivityMonitor {
    val connectionState: StateFlow<ConnectionState>

    fun startMonitoring()

    fun stopMonitoring()

    suspend fun recheckConnection()
}
