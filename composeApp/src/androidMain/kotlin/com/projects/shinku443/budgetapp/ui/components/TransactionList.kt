package com.projects.shinku443.budgetapp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.projects.shinku443.budgetapp.model.CategoryType
import com.projects.shinku443.budgetapp.model.Transaction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionList(
    transactions: List<Transaction>,
    onDelete: (Transaction) -> Unit,
    onSwipeDelete: (Transaction) -> Unit = onDelete
) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(transactions, key = { it.id }) { tx ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { value ->
                    if (value == SwipeToDismissBoxValue.StartToEnd || value == SwipeToDismissBoxValue.EndToStart) {
                        onSwipeDelete(tx)
                        true
                    } else {
                        false
                    }
                }
            )

            SwipeToDismissBox(
                state = dismissState,
                enableDismissFromStartToEnd = true,
                enableDismissFromEndToStart = true,
                backgroundContent = {
                    val bgColor = if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) Color(0xFFFFCDD2) else Color.Transparent
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .background(bgColor)
                            .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.padding(end = 16.dp))
                    }
                }
            ) {
                AnimatedVisibility(
                    visible = dismissState.targetValue == SwipeToDismissBoxValue.Settled,
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(tx.description ?: "", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    tx.type.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (tx.type == CategoryType.EXPENSE) {
                                        Color.Red
                                    } else {
                                        Color.Green
                                    }
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$${tx.amount}",
                                    color = if (tx.type == CategoryType.EXPENSE) {
                                        Color.Red
                                    } else {
                                        Color.Green
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                                IconButton(onClick = { onDelete(tx) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
