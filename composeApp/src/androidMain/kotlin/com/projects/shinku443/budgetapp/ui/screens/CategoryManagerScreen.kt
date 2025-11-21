package com.projects.shinku443.budgetapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.projects.shinku443.budgetapp.model.Category
import com.projects.shinku443.budgetapp.model.CategoryType
import com.projects.shinku443.budgetapp.ui.components.ColorPicker
import com.projects.shinku443.budgetapp.ui.components.IconPicker
import com.projects.shinku443.budgetapp.ui.utils.discoverCategoryIconsByPrefix
import com.projects.shinku443.budgetapp.viewmodel.CategoryViewModel
import org.koin.androidx.compose.koinViewModel

class CategoryManagerScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel: CategoryViewModel = koinViewModel()
        val categories by viewModel.categories.collectAsState()

        var name by remember { mutableStateOf("") }
        var categoryType by remember { mutableStateOf(CategoryType.EXPENSE) }
        var selectedIconName by remember { mutableStateOf<String?>(null) }
        var selectedColorLong by remember { mutableStateOf(0xFF64B5F6) }

        val tintColor = Color(selectedColorLong)

        // Discover icons dynamically (or provide curated list)
        val icons = remember { discoverCategoryIconsByPrefix("ic_category_") }

        Scaffold(
            topBar = { TopAppBar(title = { Text("Manage Categories") }) },
            snackbarHost = { SnackbarHost(remember { SnackbarHostState() }) }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Create Category Form
                item {
                    ElevatedCard(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Category name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
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

                            IconPicker(
                                icons = icons,
                                selectedIconName = selectedIconName,
                                onSelect = { selectedIconName = it },
                                tintColor = tintColor
                            )

                            ColorPicker(
                                selectedColor = selectedColorLong,
                                onSelectColor = { selectedColorLong = it }
                            )

                            Button(
                                onClick = {
                                    if (name.isNotBlank() && selectedIconName != null) {
                                        viewModel.createCategory(
                                            name = name,
                                            type = categoryType,
                                            color = selectedColorLong,
                                            icon = selectedIconName
                                        )
                                        name = ""
                                        selectedIconName = null
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = name.isNotBlank() && selectedIconName != null
                            ) {
                                Text("Add category")
                            }
                        }
                    }
                }

                // Existing Categories
                item {
                    Text("Your categories", style = MaterialTheme.typography.titleMedium)
                }

                items(categories) { c ->
                    ElevatedCard(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryChip(category = c, selected = false, onClick = {})
                            AssistChip(
                                onClick = { /* future: edit */ },
                                label = { Text("Edit") },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                                }
                            )
                        }
                    }
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


