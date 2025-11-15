package com.projects.shinku443.budget_app.di

import com.projects.shinku443.budget_app.api.ApiClient
import com.projects.shinku443.budget_app.repository.BudgetRepository
import com.projects.shinku443.budget_app.viewmodel.BudgetViewModel
import org.koin.dsl.module

val appModule = module {
    single { ApiClient("http://10.0.2.2:8080") }   // singleton ApiClient
    single { BudgetRepository(get()) }             // inject ApiClient
    single { BudgetViewModel(get()) }              // inject BudgetRepository
}
