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
import com.projects.shinku443.budgetapp.viewmodel.CategoryViewModel
import com.projects.shinku443.budgetapp.viewmodel.TransactionViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class AddTransactionScreen(private val onDismiss: () -> Unit = {}) : Screen {
    @Composable
    override fun Content() {
        val transactionViewModel: TransactionViewModel = koinViewModel()
        val categoryViewModel: CategoryViewModel = koinViewModel()
        val categories by categoryViewModel.categories.collectAsState()
        var showDatePicker by remember { mutableStateOf(false) }

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
                IconButton(onClick = { onDismiss() }) {
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
                            transactionViewModel.createTransaction(tx)
                            onDismiss()
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
                modifier = Modifier.fillMaxWidth(),
                isError = description.isBlank(),
                supportingText = {
                    if (description.isBlank()) Text("Description cannot be empty")
                },
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = amount.toDoubleOrNull()?.let { it <= 0 } ?: true,
                supportingText = {
                    if (amount.toDoubleOrNull() == null) {
                        Text("Enter a valid number")
                    } else if (amount.toDoubleOrNull()!! <= 0) {
                        Text("Amount must be greater than 0")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            TextButton(onClick = { showDatePicker = true }) {
                Text("Date: ${date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))}")
            }

            CategorySelector(
                categories = categories,
                selected = category,
                onSelect = { category = it },
                onCreateNew = { showNewCategoryDialog = true }
            )
        }

        // Date picker dialog
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val newDate = Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalDate()
                            date = newDate
                        }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState, showModeToggle = true)
            }
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
