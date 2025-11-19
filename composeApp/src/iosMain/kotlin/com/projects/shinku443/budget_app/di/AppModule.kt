package com.projects.shinku443.budget_app.di

import com.projects.shinku443.budget_app.api.ApiClient
import com.projects.shinku443.budget_app.repository.BudgetRepository
import io.ktor.client.*
import io.ktor.client.engine.darwin.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val appModule = module {
    single {
        HttpClient(Darwin) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
    single { ApiClient("http://127.0.0.1:8080", get()) }
    single { BudgetRepository(get(), get()) }
}
