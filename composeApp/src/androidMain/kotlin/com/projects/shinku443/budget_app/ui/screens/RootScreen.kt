package com.projects.shinku443.budget_app.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootScreen() {
    Navigator(DashboardScreen()) { navigator ->

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

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
                    TopAppBar(
                        title = { Text("Budget App") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Box(Modifier.padding(innerPadding)) {
                    CurrentScreen() // Voyager renders the active screen here
                }
            }
        }
    }
}
