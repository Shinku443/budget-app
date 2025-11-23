package com.projects.shinku443.budgetapp.sync

import com.projects.shinku443.budgetapp.connectivity.ConnectionState
import com.projects.shinku443.budgetapp.connectivity.ConnectivityMonitor
import com.projects.shinku443.budgetapp.sync.SyncService
import com.projects.shinku443.budgetapp.sync.SyncState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SyncServiceTest {

    @Test
    fun `syncAll returns Offline when disconnected`() = runTest {
        val connectivityMonitor = mockk<ConnectivityMonitor>()
        coEvery { connectivityMonitor.connectionState } returns MutableStateFlow(ConnectionState.Disconnected)
        val syncService = SyncService(mockk(), mockk(), connectivityMonitor)

        val result = syncService.syncAll()

        assertEquals(SyncState.Offline, result)
    }

    @Test
    fun `syncAll returns Error when sync fails`() = runTest {
        val connectivityMonitor = mockk<ConnectivityMonitor>()
        coEvery { connectivityMonitor.connectionState } returns MutableStateFlow(ConnectionState.Connected)
        val transactionSyncManager = mockk<TransactionSyncManager>()
        coEvery { transactionSyncManager.sync(any()) } throws Exception("Sync failed")
        val syncService = SyncService(transactionSyncManager, mockk(), connectivityMonitor)

        val result = syncService.syncAll()

        assertEquals(SyncState.Error("Sync failed"), result)
    }

    @Test
    fun `syncAll returns Idle when sync succeeds`() = runTest {
        val connectivityMonitor = mockk<ConnectivityMonitor>()
        coEvery { connectivityMonitor.connectionState } returns MutableStateFlow(ConnectionState.Connected)
        val transactionSyncManager = mockk<TransactionSyncManager>()
        coEvery { transactionSyncManager.sync(any()) } returns Unit
        val categorySyncManager = mockk<CategorySyncManager>()
        coEvery { categorySyncManager.sync() } returns Unit
        val syncService = SyncService(transactionSyncManager, categorySyncManager, connectivityMonitor)

        val result = syncService.syncAll()

        assertEquals(SyncState.Idle, result)
    }
}
