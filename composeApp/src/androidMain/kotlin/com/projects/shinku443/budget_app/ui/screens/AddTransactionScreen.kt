package com.projects.shinku443.budget_app.ui

import Category
import Transaction
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.projects.shinku443.budget_app.model.CategoryType
import com.projects.shinku443.budget_app.ui.screens.CategorySelector
import com.projects.shinku443.budget_app.viewmodel.BudgetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: BudgetViewModel,
    onCancel: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val tx = Transaction(
                            id = java.util.UUID.randomUUID().toString(),
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            type = CategoryType.EXPENSE, // or INCOME
                            categoryId = selectedCategory?.id ?: "0",
                            date = viewModel.currentMonth.toString()
                        )
                        viewModel.addTransaction(tx)
                        onCancel()
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") }
            )
            Spacer(Modifier.height(16.dp))
            CategorySelector(
                categories = viewModel.categories.collectAsState().value,
                selected = selectedCategory,
                onSelect = { selectedCategory = it }
            )
        }
    }
}
