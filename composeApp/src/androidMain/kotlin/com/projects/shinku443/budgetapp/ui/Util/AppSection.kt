package com.projects.shinku443.budgetapp.ui.Util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import cafe.adriel.voyager.core.screen.Screen
import com.projects.shinku443.budgetapp.ui.reports.ReportsScreen
import com.projects.shinku443.budgetapp.ui.screens.*

sealed class AppSection(val label: String, val icon: ImageVector) : Screen {

    class Dashboard : AppSection("Dashboard", Icons.Default.Home) {
        @Composable
        override fun Content() {
            DashboardScreen().Content()
        }
    }

    class Reports : AppSection("Reports", Icons.Default.BarChart) {
        @Composable
        override fun Content() {
            ReportsScreen().Content()
        }
    }

    class Goals : AppSection("Goals", Icons.Default.Flag) {
        @Composable
        override fun Content() {
            BudgetGoalsScreen().Content()
        }
    }

    class Categories : AppSection("Categories", Icons.Default.Category) {
        @Composable
        override fun Content() {
            CategoryManagerScreen().Content()
        }
    }

    class Investments : AppSection("Investments", Icons.Default.TrendingUp) {
        @Composable
        override fun Content() {
            AddTransactionScreen().Content()
        } // placeholder
    }

    class Settings : AppSection("Settings", Icons.Default.Settings) {
        @Composable
        override fun Content() {
            SettingsScreen().Content()
        }
    }
}


enum class ReportView {
    OVERVIEW, CASH_FLOW, INCOME, SPENDING
}
