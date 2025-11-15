package com.projects.shinku443.budget_app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform