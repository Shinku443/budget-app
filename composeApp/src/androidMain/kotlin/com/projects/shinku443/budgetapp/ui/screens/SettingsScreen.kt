package com.projects.shinku443.budgetapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.projects.shinku443.budgetapp.settings.Settings.Theme
import com.projects.shinku443.budgetapp.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

//Can use later for generic list render
//sealed class SettingItem(val title: String) {
//    object Theme : SettingItem("Theme")
//    object Currency : SettingItem("Currency")
//    object Notifications : SettingItem("Notifications")
//    object Sync : SettingItem("Sync")
//    object Login : SettingItem("Login")
//    object Logout : SettingItem("Logout")
//}

class SettingsScreen : Screen {
    @Composable
    override fun Content() {
        SettingsContent()
    }
}

@Composable
fun SettingsContent(viewModel: SettingsViewModel = koinViewModel()) {
    val theme by viewModel.theme.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val language by viewModel.language.collectAsState()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Theme toggle
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dark Theme")
            Switch(
                checked = theme == Theme.DARK,
                onCheckedChange = { enabled ->
                    viewModel.setTheme(if (enabled) Theme.DARK else Theme.LIGHT)
                }
            )
        }

        // Language selector (simple dropdown)
        LanguageSelector(
            current = language,
            onSelect = { selected -> viewModel.setLanguage(selected) }
        )

        // Notifications toggle
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Notifications")
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { viewModel.setNotificationsEnabled(it) }
            )
        }

        Spacer(Modifier.height(16.dp))

        // Auth stubs
        Button(onClick = { println("Stub: Login flow") }, modifier = Modifier.fillMaxWidth()) {
            Text("Login")
        }
        Button(onClick = { println("Stub: Logout flow") }, modifier = Modifier.fillMaxWidth()) {
            Text("Logout")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelector(
    current: String,
    onSelect: (String) -> Unit
) {
    val options = listOf("USD", "EUR", "JPY")
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(current) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Currency") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        selected = option
                        expanded = false
                        onSelect(option)
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelector(current: String, onSelect: (String) -> Unit) {
    val options = listOf("en", "es", "fr", "de", "kr", "cn")
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(current) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Language") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        selected = option
                        expanded = false
                        onSelect(option)
                    }
                )
            }
        }
    }
}
