package com.projects.shinku443.budgetapp.connectivity

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

actual class ConnectivityMonitor(private val context: Context) {
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Connecting) // Start in Connecting state
    actual val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _connectionState.value = ConnectionState.Connected
        }

        override fun onLost(network: Network) {
            _connectionState.value = ConnectionState.Disconnected
        }

        override fun onUnavailable() {
            _connectionState.value = ConnectionState.Disconnected
        }
    }

    init {
        // Immediately check the connection state upon initialization.
        GlobalScope.launch {
            recheckConnection()
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    actual fun startMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        // Also recheck when monitoring starts, to ensure the state is current.
        GlobalScope.launch {
            recheckConnection()
        }
    }

    actual fun stopMonitoring() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    actual suspend fun recheckConnection() {
        val activeNetwork = connectivityManager.activeNetwork
        if (activeNetwork != null) {
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            val isConnected = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            _connectionState.value = if (isConnected) ConnectionState.Connected else ConnectionState.Disconnected
        } else {
            _connectionState.value = ConnectionState.Disconnected
        }
    }
}
