package com.projects.shinku443.budgetapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.projects.shinku443.budgetapp.model.Category
import com.projects.shinku443.budgetapp.model.CategoryType
import com.projects.shinku443.budgetapp.ui.components.CategorySelector
import com.projects.shinku443.budgetapp.viewmodel.BudgetViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class AddTransactionScreen : Screen {
    @Composable
    override fun Content() {
        // The actual content is now in AddTransactionContent,
        // which is called from RootScreen.
        // This keeps the screen in the navigation hierarchy.
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
) {
    val viewModel: BudgetViewModel = koinViewModel()
    val categories by viewModel.categories.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }

    // Trigger API → DB sync when screen opens
    LaunchedEffect(Unit) {
        viewModel.syncDataForMonth(currentMonth)
    }

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
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
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
            selected = category,
            onSelect = onCategoryChange,
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
                        onDateChange(newDate)
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
