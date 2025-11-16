package com.projects.shinku443.budget_app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.projects.shinku443.budget_app.Screen
import com.projects.shinku443.budget_app.viewmodel.BudgetViewModel
import com.projects.shinku443.budget_app.viewmodel.CategoryViewModel
import com.projects.shinku443.budget_app.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootScreen(
    budgetViewModel: BudgetViewModel,
    categoryViewModel: CategoryViewModel,
    settingsViewModel: SettingsViewModel
) {
    var currentScreen by remember { mutableStateOf(Screen.Dashboard) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text("Budget App", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
                NavigationDrawerItem(
                    label = { Text("Dashboard") },
                    selected = currentScreen == Screen.Dashboard,
                    onClick = {
                        currentScreen = Screen.Dashboard
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Categories") },
                    selected = currentScreen == Screen.Categories,
                    onClick = {
                        currentScreen = Screen.Categories
                        scope.launch { drawerState.close() }
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = currentScreen == Screen.Settings,
                    onClick = {
                        currentScreen = Screen.Settings
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentScreen.name) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                when (currentScreen) {
                    Screen.Dashboard -> DashboardScreen(
                        viewModel = budgetViewModel,
                        onAddTransaction = { currentScreen = Screen.AddTransaction },
                        onNavigateToCategories = { currentScreen = Screen.Categories },
                        onNavigateToSettings = { currentScreen = Screen.Settings }
                    )

                    Screen.AddTransaction -> AddTransactionScreen(
                        viewModel = budgetViewModel,
                        onBack = { currentScreen = Screen.Dashboard }
                    )

                    Screen.Categories -> CategoryCreationScreen(viewModel = categoryViewModel)
                    Screen.Settings -> SettingsScreen(
                        viewModel = settingsViewModel,
                        onBack = { currentScreen = Screen.Dashboard })
                }
            }
        }
    }
}
