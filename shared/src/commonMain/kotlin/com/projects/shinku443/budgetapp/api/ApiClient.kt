package com.projects.shinku443.budgetapp.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

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

    suspend inline fun <reified T> put(path: String, bodyObj: Any): T =
        client.put("$baseUrl$path") {
            contentType(ContentType.Application.Json)
            setBody(bodyObj)
        }.body()

    suspend inline fun <reified T> delete(path: String): T =
        client.delete("$baseUrl$path").body()

    suspend inline fun <reified T> delete(path: String, bodyObj: Any): T =
        client.delete("$baseUrl$path") {
            contentType(ContentType.Application.Json)
            setBody(bodyObj)
        }.body()

    suspend inline fun <reified T> patch(path: String, bodyObj: Any): T =
        client.patch("$baseUrl$path") {
            contentType(ContentType.Application.Json)
            setBody(bodyObj)
        }.body()
}
