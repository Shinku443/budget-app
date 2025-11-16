package com.projects.shinku443.budget_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.projects.shinku443.budget_app.ui.screens.AddTransactionScreen
import com.projects.shinku443.budget_app.ui.screens.CategoryCreationScreen
import com.projects.shinku443.budget_app.ui.screens.DashboardScreen
import com.projects.shinku443.budget_app.ui.screens.RootScreen
import com.projects.shinku443.budget_app.ui.screens.SettingsScreen
import com.projects.shinku443.budget_app.ui.theme.AppTheme
import com.projects.shinku443.budget_app.viewmodel.BudgetViewModel
import com.projects.shinku443.budget_app.viewmodel.CategoryViewModel
import com.projects.shinku443.budget_app.viewmodel.SettingsViewModel
import org.koin.android.ext.android.inject

enum class Screen {
    Dashboard, AddTransaction, Categories, Settings
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val budgetViewModel: BudgetViewModel by inject()
        val categoryViewModel: CategoryViewModel by inject()
        val settingsViewModel: SettingsViewModel by inject()

        setContent {
            AppTheme {
                RootScreen(
                    budgetViewModel = budgetViewModel,
                    categoryViewModel = categoryViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}


@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
