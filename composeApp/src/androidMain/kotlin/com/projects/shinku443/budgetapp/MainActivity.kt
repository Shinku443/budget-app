package com.projects.shinku443.budgetapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.projects.shinku443.budgetapp.ui.screens.BudgetAppRoot
import com.projects.shinku443.budgetapp.ui.screens.StagingScreen
import com.projects.shinku443.budgetapp.ui.theme.AppTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        setContent {
            AppTheme {
                var loggedIn by remember { mutableStateOf(false) }

                if (!loggedIn) {
                    StagingScreen(onLoginSuccess = { loggedIn = true })
                } else {
                    BudgetAppRoot()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "budget_channel",
            "Budget Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily reminders to log transactions"
        }
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}


@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
