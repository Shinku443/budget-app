package com.projects.shinku443.budget_app.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ApiClient(val baseUrl: String) {
    val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    // Generic GET
    suspend inline fun <reified T> get(path: String): T =
        client.get("$baseUrl$path").body()

    // Generic POST
    suspend inline fun <reified T> post(path: String, bodyObj: Any): T =
        client.post("$baseUrl$path") {
            setBody(bodyObj)
        }.body()
}
