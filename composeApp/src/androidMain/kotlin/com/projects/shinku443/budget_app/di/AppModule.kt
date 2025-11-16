// shared/src/androidMain/kotlin/com/projects/shinku443/budget_app/di/AppModule.kt
package com.projects.shinku443.budget_app.di

import com.projects.shinku443.budget_app.api.ApiClient
import com.projects.shinku443.budget_app.repository.BudgetRepository
import com.projects.shinku443.budget_app.repository.CategoryRepository
import com.projects.shinku443.budget_app.viewmodel.BudgetViewModel
import com.projects.shinku443.budget_app.viewmodel.CategoryViewModel
import com.projects.shinku443.budget_app.viewmodel.SettingsViewModel
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val appModule = module {
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
    single { ApiClient("http://10.0.2.2:8080", get()) }
    single { BudgetRepository(get()) }
    single { BudgetViewModel(get()) }

    single { CategoryRepository(get()) }
    single { CategoryViewModel(get()) }

//    single { SettingsRepository(get()) }
    single { SettingsViewModel() }

}