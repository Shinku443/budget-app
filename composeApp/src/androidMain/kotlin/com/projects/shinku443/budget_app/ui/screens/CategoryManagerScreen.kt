package com.mongo.budget.android.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mongo.budget.model.Category
import com.mongo.budget.model.CategoryType
import com.mongo.budget.viewmodel.CategoryViewModel

@Composable
fun CategoryCreationScreen(viewModel: CategoryViewModel) {
    val categories by viewModel.categories.collectAsState()

    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(CategoryType.EXPENSE) }

    Column(Modifier.padding(16.dp)) {
        Text("Create Category", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        BasicTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        )
        Row {
            Button(onClick = { type = CategoryType.EXPENSE }) {
                Text("Expense")
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { type = CategoryType.INCOME }) {
                Text("Income")
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            if (name.isNotBlank()) {
                viewModel.createCategory(name, type)
                name = ""
            }
        }) {
            Text("Add Category")
        }

        Spacer(Modifier.height(16.dp))
        Text("Categories", style = MaterialTheme.typography.titleMedium)
        LazyColumn {
            items(categories.size) { i ->
                val c = categories[i]
                Text("${c.name} (${c.type})", modifier = Modifier.padding(8.dp))
            }
        }
    }
}
