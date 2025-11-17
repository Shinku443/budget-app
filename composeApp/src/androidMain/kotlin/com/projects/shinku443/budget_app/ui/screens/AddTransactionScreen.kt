package com.projects.shinku443.budget_app.ui.screens

import com.projects.shinku443.budget_app.model.Category
import Transaction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.projects.shinku443.budget_app.model.CategoryType
import com.projects.shinku443.budget_app.ui.components.CategorySelector
import com.projects.shinku443.budget_app.viewmodel.BudgetViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: BudgetViewModel,
    onBack: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var date by remember { mutableStateOf(LocalDate.now()) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }

    // Load categories when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadCategories()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (selectedCategory != null && amt > 0) {
                            val tx = Transaction(
                                id = UUID.randomUUID().toString(),
                                description = description,
                                amount = amt,
                                categoryId = selectedCategory!!.id,
                                categoryType = selectedCategory!!.type,
                                date = date.toString()
                            )
                            viewModel.addTransaction(tx)
                            onBack()
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            // Date picker
            TextButton(onClick = { showDatePicker = true }) {
                Text("Date: ${date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))}")
            }

            CategorySelector(
                categories = categories,
                selected = selectedCategory,
                onSelect = { selectedCategory = it },
                onCreateNew = { showNewCategoryDialog = true }
            )

        }
    }

    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    // date is updated via DatePicker state
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(
                state = rememberDatePickerState(
                    initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                ),
                showModeToggle = true
            )
        }
    }

    // New category dialog
    if (showNewCategoryDialog) {
        var newCategoryName by remember { mutableStateOf("") }
        var selectedType by remember { mutableStateOf(CategoryType.EXPENSE) }

        AlertDialog(
            onDismissRequest = { showNewCategoryDialog = false },
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
                    viewModel.addCategory(newCategoryName, selectedType)
                    showNewCategoryDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showNewCategoryDialog = false }) { Text("Cancel") }
            }
        )
    }
}