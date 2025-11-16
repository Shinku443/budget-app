package com.projects.shinku443.budget_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.projects.shinku443.budget_app.ui.screens.AddTransactionScreen
import com.projects.shinku443.budget_app.ui.screens.DashboardScreen
import com.projects.shinku443.budget_app.ui.theme.AppTheme
import com.projects.shinku443.budget_app.viewmodel.BudgetViewModel
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Assume Koin is started in Application class
        val viewModel: BudgetViewModel by inject()

        setContent {
            AppTheme {
                var showAdd by remember { mutableStateOf(false) }

                if (showAdd) {
                    AddTransactionScreen(viewModel) { showAdd = false }
                } else {
                    DashboardScreen(viewModel, onAddTransaction = { showAdd = true })
                }
            }
        }
    }
}


@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
