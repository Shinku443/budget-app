package com.projects.shinku443.budgetapp.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.projects.shinku443.budgetapp.api.ApiClient
import com.projects.shinku443.budgetapp.db.BudgetDatabase
import com.projects.shinku443.budgetapp.repository.BudgetRepository
import com.projects.shinku443.budgetapp.repository.CategoryRepository
import com.projects.shinku443.budgetapp.repository.SettingsRepository
import com.projects.shinku443.budgetapp.repository.TransactionRepository
import com.projects.shinku443.budgetapp.sync.CategorySyncManager
import com.projects.shinku443.budgetapp.sync.SyncService
import com.projects.shinku443.budgetapp.sync.TransactionSyncManager
import com.projects.shinku443.budgetapp.viewmodel.BudgetViewModel
import com.projects.shinku443.budgetapp.viewmodel.CategoryViewModel
import com.projects.shinku443.budgetapp.viewmodel.SettingsViewModel
import com.projects.shinku443.budgetapp.viewmodel.TransactionViewModel
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val Context.settingsDataStore by preferencesDataStore(name = "settings")

val appModule = module {
    // HttpClient
    single {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = false
                    isLenient = true
                })
            }
        }
    }

    // API
    single { ApiClient("http://10.0.2.2:8080", get()) }

    // SQLDelight DB
    single<SqlDriver> {
        AndroidSqliteDriver(
            schema = BudgetDatabase.Schema,
            context = androidContext(),
            name = "budget.db"
        )
    }
    single { BudgetDatabase(get()) }

    // Repositories
    single { CategoryRepository(get(), get()) }
    single { TransactionRepository(get(), get()) }
    single { BudgetRepository(get(), get()) }

    // Sync Managers & Service
    single { TransactionSyncManager(get(), get()) }
    single { CategorySyncManager(get(), get()) }
    single { SyncService(get(), get()) }

    // Settings
    single { androidContext().settingsDataStore }
    single { SettingsRepository(get()) }

    // ViewModels
    viewModel { CategoryViewModel(get(), get()) }
    viewModel { TransactionViewModel(get(), get()) }
    viewModel {
        BudgetViewModel(
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModel { SettingsViewModel(get(), get()) }
}
