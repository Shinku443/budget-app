package com.projects.shinku443.budgetapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.projects.shinku443.budgetapp.model.Category
import com.projects.shinku443.budgetapp.model.CategoryType
import com.projects.shinku443.budgetapp.ui.utils.iconNameToRes
import com.projects.shinku443.budgetapp.viewmodel.CategoryViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

class CategoryManagerScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel: CategoryViewModel = koinViewModel()
        val categories by viewModel.categories.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        var name by remember { mutableStateOf("") }
        var categoryType by remember { mutableStateOf(CategoryType.EXPENSE) }
        var selectedColor by remember { mutableStateOf(0xFF64B5F6) }
        var selectedIcon by remember { mutableStateOf("ic_food") }
        val selectedIds = remember { mutableStateListOf<String>() }

        val availableColors = listOf(0xFFE57373, 0xFF64B5F6, 0xFF81C784, 0xFFFFB74D, 0xFFBA68C8)
        val availableIcons = listOf("ic_food", "ic_home", "ic_transport", "ic_shopping", "ic_savings")

        Column(
            Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Create Category", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Category Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = categoryType == CategoryType.EXPENSE,
                    onClick = { categoryType = CategoryType.EXPENSE },
                    label = { Text("Expense") }
                )
                FilterChip(
                    selected = categoryType == CategoryType.INCOME,
                    onClick = { categoryType = CategoryType.INCOME },
                    label = { Text("Income") }
                )
            }

            Text("Pick Color")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                availableColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(color), shape = CircleShape)
                            .border(
                                width = if (selectedColor == color) 3.dp else 1.dp,
                                color = if (selectedColor == color) Color.Black else Color.Gray,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = color }
                    )
                }
            }

            Text("Pick Icon")
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedIcon,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Icon") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    availableIcons.forEach { icon ->
                        DropdownMenuItem(
                            text = { Text(icon) },
                            onClick = {
                                selectedIcon = icon
                                expanded = false
                            }
                        )
                    }
                }
            }

            Button(onClick = {
                if (name.isNotBlank()) {
                    viewModel.createCategory(name, categoryType, selectedColor, selectedIcon)
                    name = ""
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Category added")
                    }
                }
            }) {
                Text("Add Category")
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { c ->
                    CategoryChip(
                        category = c,
                        selected = selectedIds.contains(c.id),
                        onClick = {
                            if (selectedIds.contains(c.id)) selectedIds.remove(c.id)
                            else selectedIds.add(c.id)
                        }
                    )
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

@Composable
fun CategoryChip(category: Category, selected: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(category.name) },
        leadingIcon = {
            category.icon?.let { iconName ->
                val resId = context.resources.getIdentifier(
                    iconName, // e.g. "ic_category_groceries"
                    "drawable",
                    context.packageName
                )
                if (resId != 0) {
                    Icon(
                        painterResource(resId),
                        contentDescription = category.name,
                        tint = Color(category.color)
                    )
                }
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(category.color).copy(alpha = 0.2f)
        )
    )
}


