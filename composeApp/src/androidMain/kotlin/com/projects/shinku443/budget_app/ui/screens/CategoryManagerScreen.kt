package com.projects.shinku443.budget_app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.projects.shinku443.budget_app.model.CategoryType
import com.projects.shinku443.budget_app.viewmodel.CategoryViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

class CategoryManagerScreen : Screen {

    @Composable
    override fun Content() {
        // If you’re using Koin:
        val viewModel: CategoryViewModel = koinViewModel()

        val categories by viewModel.categories.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        var name by remember { mutableStateOf("") }
        var categoryType by remember { mutableStateOf(CategoryType.EXPENSE) }
        val selectedIds = remember { mutableStateListOf<String>() }

        Column(
            Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Create Category", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Category Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Row {
                FilterChip(
                    selected = categoryType == CategoryType.EXPENSE,
                    onClick = { categoryType = CategoryType.EXPENSE },
                    label = { Text("Expense") }
                )
                Spacer(Modifier.width(8.dp))
                FilterChip(
                    selected = categoryType == CategoryType.INCOME,
                    onClick = { categoryType = CategoryType.INCOME },
                    label = { Text("Income") }
                )
            }

            Button(onClick = {
                if (name.isNotBlank()) {
                    viewModel.createCategory(name, categoryType)
                    name = ""
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Category added")
                    }
                }
            }) {
                Text("Add Category")
            }

            LazyColumn {
                items(categories) { c ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row {
                            Checkbox(
                                checked = selectedIds.contains(c.id),
                                onCheckedChange = { checked ->
                                    if (checked) selectedIds.add(c.id) else selectedIds.remove(c.id)
                                }
                            )
                            Text("${c.name} (${c.type})", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }

            if (selectedIds.isNotEmpty()) {
                Button(
                    onClick = {
                        viewModel.deleteCategories(selectedIds.toList())
                        selectedIds.clear()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Deleted categories")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                    Spacer(Modifier.width(4.dp))
                    Text("Delete Selected")
                }
            }
        }
    }
}
