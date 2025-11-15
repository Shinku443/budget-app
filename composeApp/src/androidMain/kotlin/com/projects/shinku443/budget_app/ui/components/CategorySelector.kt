package com.projects.shinku443.budget_app.ui.screens

import Category
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.projects.shinku443.budget_app.model.CategoryType
import java.util.*

@Composable
fun CategorySelector(
    categories: List<Category>,
    selected: Category?,
    onSelect: (Category) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Text(selected?.name ?: "Select Category", Modifier.clickable { expanded = true })
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach {
                DropdownMenuItem(onClick = {
                    onSelect(it)
                    expanded = false
                }, text = { Text(it.name) })
            }
            Divider()
            DropdownMenuItem(onClick = {
                val newCat = Category(UUID.randomUUID().toString(), "New Category", CategoryType.EXPENSE)
                onSelect(newCat)
                expanded = false
            }, text = { Text("Create new category") })
        }
    }
}
