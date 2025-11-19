package com.projects.shinku443.budgetapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import com.projects.shinku443.budgetapp.ui.utils.AppSection
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootScreen() {
    Navigator(AppSection.Dashboard()) { navigator ->

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

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
                            selected = navigator.lastItem::class == section::class,
                            onClick = {
                                navigator.replace(section)
                                scope.launch { drawerState.close() }
                            }
                        )
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
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
                    CurrentScreen() // renders whichever AppSection or AddTransactionScreen is active
                }
            }
        }
    }
}
