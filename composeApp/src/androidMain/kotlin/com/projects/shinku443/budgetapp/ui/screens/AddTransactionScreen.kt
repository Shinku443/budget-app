package com.projects.shinku443.budgetapp.ui.screens

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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.projects.shinku443.budgetapp.model.Category
import com.projects.shinku443.budgetapp.model.CategoryType
import com.projects.shinku443.budgetapp.model.Transaction
import com.projects.shinku443.budgetapp.ui.components.CategorySelector
import com.projects.shinku443.budgetapp.viewmodel.BudgetViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class AddTransactionScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel: BudgetViewModel = koinViewModel()
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

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Add Transaction") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
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
                )
            }
        ) { innerPadding ->
            Box(Modifier
                .padding(innerPadding)
                .fillMaxSize()) {
                AddTransactionContent(
                    description = description,
                    onDescriptionChange = { description = it },
                    amount = amount,
                    onAmountChange = { amount = it },
                    category = category,
                    onCategoryChange = { category = it },
                    date = date,
                    onDateChange = { date = it },
                    categories = categories,
                    showNewCategoryDialog = showNewCategoryDialog,
                    onShowNewCategoryDialogChange = { showNewCategoryDialog = it }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionContent(
    description: String,
    onDescriptionChange: (String) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    category: Category?,
    onCategoryChange: (Category?) -> Unit,
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    categories: List<Category>,
    showNewCategoryDialog: Boolean,
    onShowNewCategoryDialogChange: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            isError = description.isBlank(),
            supportingText = {
                if (description.isBlank()) Text("Description cannot be empty")
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
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

        // Date picker
        TextButton(onClick = { /* TODO: show date picker */ }) {
            Text("Date: ${date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))}")
        }

        // Category selector
        CategorySelector(
            categories = categories,
            selected = category,
            onSelect = onCategoryChange,
            onCreateNew = { onShowNewCategoryDialogChange(true) }
        )
    }

    // New category dialog
    if (showNewCategoryDialog) {
        var newCategoryName by remember { mutableStateOf("") }
        var selectedType by remember { mutableStateOf(CategoryType.EXPENSE) }

        AlertDialog(
            onDismissRequest = { onShowNewCategoryDialogChange(false) },
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

                    onShowNewCategoryDialogChange(false)
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { onShowNewCategoryDialogChange(false) }) { Text("Cancel") }
            }
        )
    }
}
