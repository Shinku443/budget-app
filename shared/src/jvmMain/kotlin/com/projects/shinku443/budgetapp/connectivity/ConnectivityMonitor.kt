package com.projects.shinku443.budgetapp.connectivity

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress

actual class ConnectivityMonitor {
    private val monitorScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    actual val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    actual fun startMonitoring() {
        // Perform an initial check on startup
        monitorScope.launch {
            recheckConnection()
        }
    }

    actual fun stopMonitoring() {
        monitorScope.cancel()
    }

    actual suspend fun recheckConnection() {
        withContext(Dispatchers.IO) {
            try {
                val address = InetAddress.getByName("8.8.8.8") // Google's DNS
                if (address.isReachable(5000)) {
                    _connectionState.value = ConnectionState.Connected
                } else {
                    _connectionState.value = ConnectionState.Disconnected
                }
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.Disconnected
            }
        }
    }
}
