//package com.projects.shinku443.budgetapp.connectivity
//
//import app.cash.turbine.test
//import com.projects.shinku443.budgetapp.connectivity.ConnectionState
//import com.projects.shinku443.budgetapp.connectivity.ConnectivityMonitor
//import kotlinx.coroutines.test.runTest
//import kotlin.test.Test
//import kotlin.test.assertEquals
//
//class ConnectivityMonitorTest {
//
//    @Test
//    fun `initial state is Disconnected`() = runTest {
//        val monitor = ConnectivityMonitor()
//        assertEquals(ConnectionState.Disconnected, monitor.connectionState.value)
//    }
//
//    @Test
//    fun `startMonitoring updates state to Connected`() = runTest {
//        val monitor = ConnectivityMonitor()
//        monitor.startMonitoring()
//        monitor.connectionState.test {
//            assertEquals(ConnectionState.Connected, awaitItem())
//        }
//    }
//
//    @Test
//    fun `stopMonitoring updates state to Disconnected`() = runTest {
//        val monitor = ConnectivityMonitor()
//        monitor.startMonitoring()
//        monitor.stopMonitoring()
//        monitor.connectionState.test {
//            assertEquals(ConnectionState.Disconnected, awaitItem())
//        }
//    }
//}
