package com.projects.shinku443.budgetapp

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import co.touchlab.kermit.Logger
import com.projects.shinku443.budgetapp.ui.screens.BudgetAppRoot
import com.projects.shinku443.budgetapp.ui.theme.AppTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
//                != PackageManager.PERMISSION_GRANTED
//            ) {
//                ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
//                    1001 // your request code
//                )
//            }
//        }


        setContent {
            AppTheme {
                BudgetAppRoot()
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
