package com.projects.shinku443.budget_app.ui.dashboard

import Transaction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mongo.budget.viewmodel.BudgetViewModel
import com.projects.shinku443.budget_app.model.TransactionType
import com.projects.shinku443.budget_app.ui.screens.MonthSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: BudgetViewModel, onAddTransaction: () -> Unit) {
    val transactions by viewModel.transactions.collectAsState()
    val income by viewModel.income.collectAsState()
    val expense by viewModel.expense.collectAsState()
    val net by viewModel.net.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Dashboard") },
                actions = {
                    MonthSelector(
                        currentMonth = viewModel.currentMonth,
                        onMonthSelected = { month ->
                            viewModel.loadTransactions(month.toString())
                        }
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            SummaryCard(income, expense, net)

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(transactions) { tx ->
                    TransactionItem(tx)
                }
            }
        }
    }
}

@Composable
fun SummaryCard(income: Double, expense: Double, net: Double) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Income: $income")
            Text("Expense: $expense")
            Text("Net: $net")
        }
    }
}

@Composable
fun TransactionItem(tx: Transaction) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(tx.categoryId, style = MaterialTheme.typography.bodyLarge)
                Text(tx.date, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = "${tx.amount}",
                style = MaterialTheme.typography.bodyLarge,
                color = if (tx.type == TransactionType.EXPENSE) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )
        }
    }
}
