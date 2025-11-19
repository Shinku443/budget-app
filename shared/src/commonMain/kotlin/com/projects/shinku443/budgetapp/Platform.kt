package com.projects.shinku443.budgetapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform