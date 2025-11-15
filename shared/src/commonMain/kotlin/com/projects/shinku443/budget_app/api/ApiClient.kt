package com.projects.shinku443.budget_app.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ApiClient(
    val baseUrl: String,
    val client: HttpClient
) {
    suspend inline fun <reified T> get(path: String): T =
        client.get("$baseUrl$path").body()

    suspend inline fun <reified T> post(path: String, bodyObj: Any): T =
        client.post("$baseUrl$path") {
            contentType(ContentType.Application.Json)
            setBody(bodyObj)
        }.body()
}
