package com.projects.shinku443.budgetapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
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
import com.projects.shinku443.budgetapp.viewmodel.BudgetViewModel
import org.koin.androidx.compose.koinViewModel

class BudgetGoalsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: BudgetViewModel = koinViewModel()
        // Observe current goal and expenses
        val monthlyGoal by remember { mutableFloatStateOf(viewModel.monthlyBudgetGoal) }
        val expense by viewModel.expense.collectAsState()

        var newGoalText by remember { mutableStateOf(monthlyGoal.toString()) }

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
                        // Update goal in ViewModel
                        // viewModel.setMonthlyBudgetGoal(parsed)
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
