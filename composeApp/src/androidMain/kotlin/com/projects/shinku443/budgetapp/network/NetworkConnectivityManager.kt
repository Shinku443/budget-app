package com.projects.shinku443.budgetapp.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.timeout
import io.ktor.client.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manages network connectivity checking for the app.
 * Tests actual connection by attempting to reach the API server.
 */
class NetworkConnectivityManager(
    private val context: Context,
    private val apiClient: HttpClient,
    private val apiBaseUrl: String
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Checks if device has network connectivity (not just WiFi/Data, but actual internet)
     */
    fun hasNetworkConnection(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /**
     * Tests actual connectivity to the API server by making a lightweight request
     */
    suspend fun testConnection(): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                // Try a lightweight HEAD request to test connectivity
                val response = apiClient.head("$apiBaseUrl/health")
                response.status.value in 200..299
            }
        } catch (e: Exception) {
            // If health endpoint doesn't exist, try a simple GET to categories (lightweight)
            try {
                withContext(Dispatchers.IO) {
                    apiClient.get("$apiBaseUrl/categories") {
                        timeout {
                            requestTimeoutMillis = 5000 // 5 second timeout
                        }
                    }
                    true
                }
            } catch (e2: Exception) {
                false
            }
        }
    }
}
