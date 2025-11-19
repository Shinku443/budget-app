package com.projects.shinku443.budgetapp.ui.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.projects.shinku443.budgetapp.viewmodel.BudgetViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun CashFlowReport() {
    val viewModel: BudgetViewModel = koinViewModel()

    val income by viewModel.income.collectAsState()
    val expense by viewModel.expense.collectAsState()
    val net by viewModel.net.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Cash Flow Report",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        IncomeToExpensesToCategoriesSankey()
    }

    Text("Income: $${income}", style = MaterialTheme.typography.bodyLarge)
    Text("Expenses: $${expense}", style = MaterialTheme.typography.bodyLarge)
    Text("Net: $${net}", style = MaterialTheme.typography.bodyLarge)
}


