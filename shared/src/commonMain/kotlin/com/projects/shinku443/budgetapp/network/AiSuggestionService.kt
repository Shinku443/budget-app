package com.projects.shinku443.budgetapp.network

import co.touchlab.kermit.Logger
import com.projects.shinku443.budgetapp.model.Transaction
import com.projects.shinku443.budgetapp.settings.SecureKeyProvider
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

// AiSuggestionService.kt
interface AiSuggestionService {
    suspend fun suggestBudgetTips(transactions: List<Transaction>): List<String>
}

// Implementation using OpenAI API
class AiSuggestionServiceImpl(
    private val httpClient: HttpClient,
    private val secureKeyProvider: SecureKeyProvider
) : AiSuggestionService {


    override suspend fun suggestBudgetTips(transactions: List<Transaction>): List<String> {
        val apiKey = secureKeyProvider.getApiKey()
        if (apiKey.isNullOrBlank()) {
            Logger.e("OpenAI API key is not set. Cannot get suggestions.")
            return emptyList()
        }

        Logger.d("Suggesting via transactions:: $transactions")
        val summary = buildSummary(transactions)

        val response: HttpResponse = httpClient.post("https://api.openai.com/v1/chat/completions") {
            header("Authorization", "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "model": "gpt-4o-mini",
                  "messages": [
                    {"role": "system", "content": "You are a budgeting assistant."},
                    {"role": "user", "content": "Give 3 short budgeting tips based on: $summary"}
                  ],
                  "max_tokens": 100
                }
                """.trimIndent()
            )
        }

        Logger.d("Response:: ${response.bodyAsText()}")
        val json = response.bodyAsText()
        val parsed = Json.parseToJsonElement(json)
        val content = parsed.jsonObject["choices"]
            ?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("message")
            ?.jsonObject?.get("content")
            ?.jsonPrimitive?.content ?: ""

        // Split into tips (basic parsing)
        return content.split("\n").filter { it.isNotBlank() }
    }

    private fun buildSummary(transactions: List<Transaction>): String {
        val total = transactions.sumOf { it.amount }
        val topCategory = transactions.groupBy { it.type.name }
            .maxByOrNull { it.value.sumOf { t -> t.amount } }?.key ?: "Misc"
        return "Total spending: $$total. Top category: $topCategory."
    }
}
