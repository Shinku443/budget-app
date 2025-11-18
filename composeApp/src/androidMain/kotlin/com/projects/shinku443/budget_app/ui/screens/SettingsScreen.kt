package com.projects.shinku443.budget_app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.projects.shinku443.budget_app.settings.Settings
import com.projects.shinku443.budget_app.viewmodel.SettingsViewModel
import org.koin.androidx.compose.getViewModel

class SettingsScreen : Screen {

    @Composable
    override fun Content() {
        // If you’re using Koin for DI:
        val viewModel: SettingsViewModel = getViewModel()

        val theme by viewModel.theme.collectAsState()
        val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Settings", style = MaterialTheme.typography.titleLarge)

            Spacer(Modifier.height(16.dp))

            // Example toggle for dark/light theme
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Dark Theme")
                Switch(
                    checked = theme == Settings.Theme.DARK,
                    onCheckedChange = { checked ->
                        viewModel.setTheme(if (checked) Settings.Theme.DARK else Settings.Theme.LIGHT)
                    }
                )
            }

            Spacer(Modifier.height(16.dp))

            // Example toggle for notifications
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Enable Notifications")
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { checked ->
                        viewModel.setNotificationsEnabled(checked)
                    }
                )
            }
        }
    }
}
