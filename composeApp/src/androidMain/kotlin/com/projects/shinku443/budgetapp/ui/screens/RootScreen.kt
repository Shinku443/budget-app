package com.projects.shinku443.budgetapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import com.projects.shinku443.budgetapp.model.Category
import com.projects.shinku443.budgetapp.model.Transaction
import com.projects.shinku443.budgetapp.ui.Util.AppSection
import com.projects.shinku443.budgetapp.viewmodel.BudgetViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootScreen() {
    Navigator(AppSection.Dashboard()) { navigator ->   // ✅ instantiate with ()

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val viewModel: BudgetViewModel = koinViewModel()

        // State for AddTransactionScreen
        var newTransactionDescription by remember { mutableStateOf("") }
        var newTransactionAmount by remember { mutableStateOf("") }
        var newTransactionCategory by remember { mutableStateOf<Category?>(null) }
        var newTransactionDate by remember { mutableStateOf(LocalDate.now()) }

        val isAddTransactionSaveEnabled by remember(newTransactionAmount, newTransactionCategory) {
            derivedStateOf {
                val amt = newTransactionAmount.toDoubleOrNull() ?: 0.0
                newTransactionCategory != null && amt > 0
            }
        }

        fun resetAddTransactionState() {
            newTransactionDescription = ""
            newTransactionAmount = ""
            newTransactionCategory = null
            newTransactionDate = LocalDate.now()
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Text(
                        "Budget App",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(16.dp)
                    )
                    listOf(
                        AppSection.Dashboard(),
                        AppSection.Reports(),
                        AppSection.Goals(),
                        AppSection.Categories(),
                        AppSection.Investments(),
                        AppSection.Settings()
                    ).forEach { section ->
                        NavigationDrawerItem(
                            label = { Text(section.label) },
                            icon = { Icon(section.icon, contentDescription = section.label) },
                            selected = navigator.lastItem::class == section::class,   // ✅ compare classes
                            onClick = {
                                navigator.replace(section)   // ✅ replace with new instance
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
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
                                IconButton(onClick = {
                                    resetAddTransactionState()
                                    navigator.pop()
                                }) {
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
                                        resetAddTransactionState()
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
                    if (navigator.lastItem is AppSection.Dashboard) {
                        ExtendedFloatingActionButton(
                            onClick = { navigator.push(AddTransactionScreen()) },
                            icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                            text = { Text("Add Transaction") }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    CurrentScreen()   // ✅ renders whichever AppSection is active
                }
            }
        }
    }
}
