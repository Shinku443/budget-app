package com.projects.shinku443.budgetapp.ui.reports

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.projects.shinku443.budgetapp.ui.Util.ReportView
import com.projects.shinku443.budgetapp.viewmodel.BudgetViewModel
import org.koin.androidx.compose.koinViewModel

class ReportsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: BudgetViewModel = koinViewModel()

        val income by viewModel.income.collectAsState()
        val expense by viewModel.expense.collectAsState()
        val net by viewModel.net.collectAsState()

        Column(
            modifier = Modifier.Companion
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            var selectedView by remember { mutableStateOf(ReportView.OVERVIEW) }

            Column {
                // Horizontal chip row
                Row(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportView.entries.forEach { view ->
                        FilterChip(
                            selected = selectedView == view,
                            onClick = { selectedView = view },
                            label = { Text(view.name.replace("_", " ")) }
                        )
                    }
                }
                // Swap content based on selected chip
                when (selectedView) {
                    ReportView.OVERVIEW -> ReportsOverview()
                    ReportView.CASH_FLOW -> CashFlowReport()
                    ReportView.INCOME -> {}// IncomeReport()
                    ReportView.SPENDING -> {}//SpendingReport()
                }
            }

        }
    }
}