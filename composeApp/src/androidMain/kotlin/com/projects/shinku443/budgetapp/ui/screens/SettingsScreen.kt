package com.projects.shinku443.budgetapp.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import cafe.adriel.voyager.core.screen.Screen
import co.touchlab.kermit.Logger
import com.projects.shinku443.budgetapp.notifications.cancelDailyReminder
import com.projects.shinku443.budgetapp.notifications.scheduleDailyReminder
import com.projects.shinku443.budgetapp.notifications.scheduleTestReminder
import com.projects.shinku443.budgetapp.settings.Settings.Theme
import com.projects.shinku443.budgetapp.settings.SettingsManager
import com.projects.shinku443.budgetapp.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.getKoin

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
fun SettingsContent() {
    val context = LocalContext.current

    val viewModel: SettingsViewModel = koinViewModel()
    val settingsManager: SettingsManager = SettingsManager(context)
    val theme by viewModel.theme.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val language by viewModel.language.collectAsState()
    var apiKey by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                Logger.d("Notification permission granted")
                scheduleTestReminder(context) // or scheduleDailyReminder(context)
            } else {
                Logger.d("Notification permission denied")
            }
        }
    )

    val showPermissionButton = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

//    val shouldShowRationale = remember {
//        activity?.let {
//            ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.POST_NOTIFICATIONS)
//        } ?: false
//    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()


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
//            Switch(
//                checked = notificationsEnabled,
//                onCheckedChange = { viewModel.setNotificationsEnabled(it) }
//            )
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { enabled ->
                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.setNotificationsEnabled(enabled)
                        if (enabled) scheduleDailyReminder(context) else cancelDailyReminder(context)
                    }
                }
            )
        }

        Text("Enter your OpenAI API Key")
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text("API Key") },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                settingsManager.saveApiKey(apiKey)
                Toast.makeText(context, "API Key saved", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }

        Spacer(Modifier.height(16.dp))

        // Auth stubs

        Button(
            onClick = { viewModel.setLoggedIn(false) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log out")
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
