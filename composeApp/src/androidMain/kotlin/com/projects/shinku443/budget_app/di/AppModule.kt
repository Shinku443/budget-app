package com.projects.shinku443.budget_app.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.projects.shinku443.budget_app.api.ApiClient
import com.projects.shinku443.budget_app.db.BudgetDatabase
import com.projects.shinku443.budget_app.repository.*
import com.projects.shinku443.budget_app.sync.CategorySyncManager
import com.projects.shinku443.budget_app.sync.SyncService
import com.projects.shinku443.budget_app.sync.TransactionSyncManager
import com.projects.shinku443.budget_app.viewmodel.*
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
    viewModel { BudgetViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(get()) }
}
