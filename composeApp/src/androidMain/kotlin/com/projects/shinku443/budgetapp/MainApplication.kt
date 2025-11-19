package com.projects.shinku443.budgetapp

import android.app.Application
import com.projects.shinku443.budgetapp.di.appModule
import com.projects.shinku443.budgetapp.di.settingsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MainApplication)
            modules(listOf(appModule, settingsModule))

        }
    }
}
