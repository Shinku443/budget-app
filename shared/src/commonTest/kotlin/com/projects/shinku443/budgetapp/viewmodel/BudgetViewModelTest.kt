package com.projects.shinku443.budgetapp.viewmodel

import app.cash.turbine.test
import com.projects.shinku443.budgetapp.sync.SyncService
import com.projects.shinku443.budgetapp.sync.SyncState
import com.projects.shinku443.budgetapp.util.YearMonth
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BudgetViewModelTest {

    @Test
    fun `syncDataForMonth updates syncState`() = runTest {
        val syncService = mockk<SyncService>()
        coEvery { syncService.syncAll(any()) } returns SyncState.Idle
        val viewModel = BudgetViewModel(mockk(), mockk(), mockk(), syncService, mockk(), mockk())

        viewModel.syncState.test {
            assertEquals(SyncState.Idle, awaitItem())

            viewModel.syncDataForMonth(YearMonth(2023, 1))

            assertEquals(SyncState.Syncing, awaitItem())
            assertEquals(SyncState.Idle, awaitItem())
        }
    }
}
