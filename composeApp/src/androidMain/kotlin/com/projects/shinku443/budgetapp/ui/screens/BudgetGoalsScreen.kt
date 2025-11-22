package com.projects.shinku443.budgetapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.projects.shinku443.budgetapp.model.CategoryType
import com.projects.shinku443.budgetapp.util.YearMonth
import com.projects.shinku443.budgetapp.viewmodel.BudgetViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.Month
import java.time.format.TextStyle
import java.util.*

class BudgetGoalsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: BudgetViewModel = koinViewModel()
        val currentMonth by viewModel.currentMonth.collectAsState()
        
        // Allow user to select which month/year to set goal for
        var selectedMonth by remember { mutableStateOf(currentMonth) }
        var showMonthPicker by remember { mutableStateOf(false) }
        
        // Observe goal for selected month
        val monthlyGoalFlow = remember(selectedMonth) {
            viewModel.getBudgetGoalForMonth(selectedMonth)
        }
        val monthlyGoal by monthlyGoalFlow.collectAsState()
        
        // Get expenses for the selected month
        val allTransactions by viewModel.transactions.collectAsState()
        val expense = remember(allTransactions, selectedMonth) {
            val monthStr = selectedMonth.toString()
            allTransactions
                .filter { it.date.startsWith(monthStr) && it.type == CategoryType.EXPENSE }
                .sumOf { it.amount }
        }

        var newGoalText by remember(monthlyGoal) { 
            mutableStateOf(if (monthlyGoal > 0) monthlyGoal.toString() else "") 
        }
        
        // Update text when monthlyGoal changes
        LaunchedEffect(monthlyGoal) {
            newGoalText = if (monthlyGoal > 0) monthlyGoal.toString() else ""
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title at the top
                Text(
                    "Budget Goal",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                
                // Month/Year Selector
                TextButton(
                    onClick = { showMonthPicker = true },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "${Month.of(selectedMonth.month).getDisplayName(TextStyle.FULL, Locale.getDefault())} ${selectedMonth.year}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                OutlinedTextField(
                    value = newGoalText,
                    onValueChange = { newGoalText = it },
                    label = { Text("Monthly Goal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                //MonthlyBudgetProgress

                // Progress indicator
                val progress = if (monthlyGoal > 0) (expense / monthlyGoal).coerceIn(0.0, 1.0) else 0.0
                LinearProgressIndicator(
                    progress = progress.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Spent: $expense / Goal: $monthlyGoal",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Floating Save Button
            FloatingActionButton(
                onClick = {
                    val parsed = newGoalText.toFloatOrNull()
                    if (parsed != null && parsed > 0f) {
                        viewModel.setMonthlyBudgetGoal(parsed, selectedMonth)
                        navigator.pop()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save")
            }
        }
        
        // Month/Year Picker Dialog
        if (showMonthPicker) {
            MonthYearPickerDialog(
                initialMonth = selectedMonth,
                onDismiss = { showMonthPicker = false },
                onConfirm = { month ->
                    selectedMonth = month
                    showMonthPicker = false
                    // The goal text will update automatically via LaunchedEffect when monthlyGoal changes
                }
            )
        }
    }
}

@Composable
fun MonthlyBudgetProgress(expenses: Double, budget: Double) {
    val progress = (expenses / budget).coerceIn(0.0, 1.0)
    Column {
        Text("Monthly Budget: $expenses / $budget")
        LinearProgressIndicator(
            progress = progress.toFloat(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
