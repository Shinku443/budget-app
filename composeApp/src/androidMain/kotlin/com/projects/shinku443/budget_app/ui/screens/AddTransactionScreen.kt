package com.projects.shinku443.budget_app.ui.add

import Transaction
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.projects.shinku443.budget_app.model.Transaction
import com.projects.shinku443.budget_app.model.TransactionType
import com.projects.shinku443.budget_app.viewmodel.BudgetViewModel
import java.util.UUID
import java.time.LocalDate

@Composable
fun AddTransactionScreen(viewModel: BudgetViewModel, onDone: () -> Unit) {
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Simple toggle for type
        Row {
            RadioButton(selected = type == TransactionType.EXPENSE, onClick = { type = TransactionType.EXPENSE })
            Text("Expense")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = type == TransactionType.INCOME, onClick = { type = TransactionType.INCOME })
            Text("Income")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val tx = Transaction(
                id = UUID.randomUUID().toString(),
                date = LocalDate.now().toString(),
                amount = amount.toDoubleOrNull() ?: 0.0,
                categoryId = "Groceries", // TODO: hook up category selector
                type = type,
                notes = notes
            )
            viewModel.addTransaction(tx)
            onDone()
        }) {
            Text("Save Transaction")
        }
    }
}
