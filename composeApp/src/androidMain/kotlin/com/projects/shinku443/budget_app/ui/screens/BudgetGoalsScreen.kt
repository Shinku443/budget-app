package com.projects.shinku443.budget_app.ui.screens

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
import com.projects.shinku443.budget_app.viewmodel.BudgetViewModel
import org.koin.androidx.compose.koinViewModel

class BudgetGoalsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: BudgetViewModel = koinViewModel()

        // Observe current goal and expenses
        val monthlyGoal by remember { mutableStateOf(viewModel.monthlyBudgetGoal) }
        val expense by viewModel.expense.collectAsState()

        var newGoalText by remember { mutableStateOf(monthlyGoal.toString()) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Budget Goal") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            val parsed = newGoalText.toFloatOrNull()
                            if (parsed != null && parsed > 0f) {
                                // Update goal in ViewModel
//                                viewModel.setMonthlyBudgetGoal(parsed)
                                navigator.pop()
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Set your monthly budget goal", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = newGoalText,
                    onValueChange = { newGoalText = it },
                    label = { Text("Monthly Goal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

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
        }
    }
}
