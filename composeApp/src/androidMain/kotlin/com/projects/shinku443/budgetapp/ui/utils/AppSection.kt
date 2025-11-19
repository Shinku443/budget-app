package com.projects.shinku443.budgetapp.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import cafe.adriel.voyager.core.screen.Screen
import com.projects.shinku443.budgetapp.ui.reports.ReportsScreen
import com.projects.shinku443.budgetapp.ui.screens.*

sealed class AppSection(val label: String, val iconName: String) : Screen {

    class Dashboard : AppSection("Dashboard", "home") {
        @Composable
        override fun Content() {
            DashboardScreen().Content()
        }
    }

    class Reports : AppSection("Reports", "reports") {
        @Composable
        override fun Content() {
            ReportsScreen().Content()
        }
    }

    class Goals : AppSection("Goals", "goals") {
        @Composable
        override fun Content() {
            BudgetGoalsScreen().Content()
        }
    }

    class Categories : AppSection("Categories", "categories") {
        @Composable
        override fun Content() {
            CategoryManagerScreen().Content()
        }
    }

    class Investments : AppSection("Investments", "investments") {
        @Composable
        override fun Content() {
            AddTransactionScreen().Content()
        } // placeholder
    }

    class Settings : AppSection("Settings", "settings") {
        @Composable
        override fun Content() {
            SettingsScreen().Content()
        }
    }
}


enum class ReportView {
    OVERVIEW, CASH_FLOW, INCOME, SPENDING
}
