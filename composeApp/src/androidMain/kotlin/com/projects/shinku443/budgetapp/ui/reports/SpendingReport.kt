package com.projects.shinku443.budgetapp.ui.reports


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.projects.shinku443.budgetapp.model.CategoryType
import com.projects.shinku443.budgetapp.viewmodel.CategoryViewModel
import com.projects.shinku443.budgetapp.viewmodel.TransactionViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SpendingReport(
    transactionViewModel: TransactionViewModel = koinViewModel(),
    categoryViewModel: CategoryViewModel = koinViewModel()
) {
    val transactions by transactionViewModel.transactions.collectAsState()
    val categories by categoryViewModel.categories.collectAsState()

    val categoryNames = remember(categories) {
        categories.associate { it.id to it.name }
    }

    // Income nodes (level 0)
    val incomeNodes = transactions.filter { it.type == CategoryType.INCOME }
        .groupBy { it.categoryId }
        .map { (catId, txs) ->
            SankeyNode(
                id = "income-$catId",
                label = categoryNames[catId]?.let { "Income: $it" } ?: "Income: $catId",
                value = txs.sumOf { it.amount }.toFloat(),
                level = 0
            )
        }

    // Aggregate expenses node (level 1)
    val totalExpenses = transactions.filter { it.type == CategoryType.EXPENSE }
        .sumOf { it.amount }.toFloat()

    val expensesNode = SankeyNode(
        id = "expenses-total",
        label = "Expenses",
        value = totalExpenses,
        level = 1
    )

    // Expense category nodes (level 2)
    val expenseCategoryNodes = transactions.filter { it.type == CategoryType.EXPENSE }
        .groupBy { it.categoryId }
        .map { (catId, txs) ->
            SankeyNode(
                id = "expense-cat-$catId",
                label = categoryNames[catId] ?: "Category: $catId",
                value = txs.sumOf { it.amount }.toFloat(),
                level = 2
            )
        }

    // Links: Income → Expenses
    val incomeToExpensesLinks = incomeNodes.map {
        SankeyLink(from = it.id, to = expensesNode.id, value = it.value)
    }

    // Links: Expenses → Expense Categories
    val expensesToCategoryLinks = expenseCategoryNodes.map {
        SankeyLink(from = expensesNode.id, to = it.id, value = it.value)
    }

    // Render chart
    Column(Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("Spending Breakdown", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        SankeyChart(
            nodes = incomeNodes + expensesNode + expenseCategoryNodes,
            links = incomeToExpensesLinks + expensesToCategoryLinks,
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        )
    }
}
