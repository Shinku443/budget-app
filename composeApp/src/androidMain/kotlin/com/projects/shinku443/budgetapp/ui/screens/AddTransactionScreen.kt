package com.projects.shinku443.budgetapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.projects.shinku443.budgetapp.model.Category
import com.projects.shinku443.budgetapp.model.CategoryType
import com.projects.shinku443.budgetapp.model.Transaction
import com.projects.shinku443.budgetapp.ui.components.CategorySelector
import com.projects.shinku443.budgetapp.viewmodel.BudgetViewModel
import com.projects.shinku443.budgetapp.viewmodel.CategoryViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class AddTransactionScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel: BudgetViewModel = koinViewModel()
        val categoryViewModel: CategoryViewModel = koinViewModel()
        val categories by viewModel.categories.collectAsState()

        var description by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }
        var category by remember { mutableStateOf<Category?>(null) }
        var date by remember { mutableStateOf(LocalDate.now()) }
        var showNewCategoryDialog by remember { mutableStateOf(false) }

        val isSaveEnabled by remember(description, amount, category) {
            derivedStateOf {
                val amt = amount.toDoubleOrNull()
                !description.isBlank() && category != null && amt != null && amt > 0
            }
        }

        val navigator = LocalNavigator.currentOrThrow

        // Fullscreen layout, no Scaffold
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Custom header row instead of TopAppBar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navigator.pop() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text("Add Transaction", style = MaterialTheme.typography.titleLarge)
                IconButton(
                    onClick = {
                        val amt = amount.toDoubleOrNull()
                        if (amt != null && category != null) {
                            val tx = Transaction(
                                id = UUID.randomUUID().toString(),
                                description = description.trim(),
                                amount = amt,
                                categoryId = category!!.id,
                                type = category!!.type,
                                date = date.toString(),
                                createdAt = System.currentTimeMillis()
                            )
                            viewModel.addTransaction(tx)
                            navigator.pop()
                        }
                    },
                    enabled = isSaveEnabled
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Save")
                }
            }

            // Form fields
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            TextButton(onClick = { /* TODO: show date picker */ }) {
                Text("Date: ${date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))}")
            }

            CategorySelector(
                categories = categories,
                selected = category,
                onSelect = { category = it },
                onCreateNew = { showNewCategoryDialog = true }
            )
        }

        // New category dialog
        if (showNewCategoryDialog) {
            var newCategoryName by remember { mutableStateOf("") }
            var selectedType by remember { mutableStateOf(CategoryType.EXPENSE) }

            AlertDialog(
                onDismissRequest = {
                    showNewCategoryDialog = false
                },
                title = { Text("New Category") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            label = { Text("Category Name") }
                        )
                        Row {
                            FilterChip(
                                selected = selectedType == CategoryType.INCOME,
                                onClick = { selectedType = CategoryType.INCOME },
                                label = { Text("Income") }
                            )
                            Spacer(Modifier.width(8.dp))
                            FilterChip(
                                selected = selectedType == CategoryType.EXPENSE,
                                onClick = { selectedType = CategoryType.EXPENSE },
                                label = { Text("Expense") }
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        // TODO: call viewModel.addCategory(newCategoryName, selectedType)
                        categoryViewModel.createCategory(newCategoryName, selectedType)
                        showNewCategoryDialog = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showNewCategoryDialog = false
                    }) { Text("Cancel") }
                }
            )
        }
    }
}
