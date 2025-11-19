package com.projects.shinku443.budget_app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.projects.shinku443.budget_app.model.Transaction
import com.projects.shinku443.budget_app.viewmodel.BudgetViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootScreen() {
    Navigator(DashboardScreen()) { navigator ->

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val viewModel: BudgetViewModel = koinViewModel()

        // State for the AddTransactionScreen, hoisted to the RootScreen
        var newTransactionDescription by remember { mutableStateOf("") }
        var newTransactionAmount by remember { mutableStateOf("") }
        var newTransactionCategory by remember { mutableStateOf<com.projects.shinku443.budget_app.model.Category?>(null) }
        var newTransactionDate by remember { mutableStateOf(LocalDate.now()) }

        val isAddTransactionSaveEnabled by remember(newTransactionAmount, newTransactionCategory) {
            derivedStateOf {
                val amt = newTransactionAmount.toDoubleOrNull() ?: 0.0
                newTransactionCategory != null && amt > 0
            }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Text("Budget App", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
                    NavigationDrawerItem(
                        label = { Text("Dashboard") },
                        selected = navigator.lastItem is DashboardScreen,
                        onClick = {
                            navigator.replace(DashboardScreen())
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Categories") },
                        selected = navigator.lastItem is CategoryManagerScreen,
                        onClick = {
                            navigator.replace(CategoryManagerScreen())
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("Settings") },
                        selected = navigator.lastItem is SettingsScreen,
                        onClick = {
                            navigator.replace(SettingsScreen())
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    val currentScreen = navigator.lastItem
                    if (currentScreen is AddTransactionScreen) {
                        TopAppBar(
                            title = { Text("Add Transaction") },
                            navigationIcon = {
                                IconButton(onClick = { navigator.pop() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                                }
                            },
                            actions = {
                                IconButton(
                                    onClick = {
                                        val tx = Transaction(
                                            id = UUID.randomUUID().toString(),
                                            description = newTransactionDescription,
                                            amount = newTransactionAmount.toDouble(),
                                            categoryId = newTransactionCategory!!.id,
                                            type = newTransactionCategory!!.type,
                                            date = newTransactionDate.toString(),
                                            createdAt = System.currentTimeMillis()
                                        )
                                        viewModel.addTransaction(tx)
                                        navigator.pop()
                                    },
                                    enabled = isAddTransactionSaveEnabled
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Save")
                                }
                            }
                        )
                    } else {
                        TopAppBar(
                            title = { Text("Budget App") },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                },
                floatingActionButton = {
                    if (navigator.lastItem is DashboardScreen) {
                        ExtendedFloatingActionButton(
                            onClick = { navigator.push(AddTransactionScreen()) },
                            icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                            text = { Text("Add Transaction") }
                        )
                    }
                }
            ) { innerPadding ->
                Box(Modifier.padding(innerPadding)) {
                    val currentScreen = navigator.lastItem
                    if (currentScreen is AddTransactionScreen) {
                        AddTransactionContent(
                            description = newTransactionDescription,
                            onDescriptionChange = { newTransactionDescription = it },
                            amount = newTransactionAmount,
                            onAmountChange = { newTransactionAmount = it },
                            category = newTransactionCategory,
                            onCategoryChange = { newTransactionCategory = it },
                            date = newTransactionDate,
                            onDateChange = { newTransactionDate = it }
                        )
                    } else {
                        CurrentScreen()
                    }
                }
            }
        }
    }
}
