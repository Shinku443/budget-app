package com.projects.shinku443.budget_app.ui.components

import Transaction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun TransactionList(
    transactions: List<Transaction>,
    onDelete: (Transaction) -> Unit
) {
    Column {
        transactions.forEach { tx ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(tx.type.name)
                    Text("${tx.amount}")
                }
                IconButton(onClick = { onDelete(tx) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
            Divider()
        }
    }
}
