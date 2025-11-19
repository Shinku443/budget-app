package com.projects.shinku443.budgetapp.ui.components

import com.projects.shinku443.budgetapp.model.Category
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun CategorySelector(
    categories: List<Category>,
    selected: Category?,
    onSelect: (Category) -> Unit,
    onCreateNew: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Text(selected?.name ?: "Select Category", Modifier.clickable { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach {
                DropdownMenuItem(
                    onClick = {
                        onSelect(it)
                        expanded = false
                    },
                    text = { Text(it.name) }
                )
            }
            Divider()
            DropdownMenuItem(
                onClick = {
                    expanded = false
                    onCreateNew()
                },
                text = { Text("➕ New Category") }
            )
        }
    }
}
