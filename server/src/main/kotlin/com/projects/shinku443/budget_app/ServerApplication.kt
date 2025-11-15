package com.projects.shinku443.budget_app

import Category
import Transaction
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import com.projects.shinku443.budget_app.model.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

import java.util.UUID

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json(Json { prettyPrint = true; ignoreUnknownKeys = true })
        }
        routing {
            get("/") {
                call.respondText("Budget App API is running 🚀")
            }

            val categories = mutableListOf(
                Category(UUID.randomUUID().toString(), "Groceries", CategoryType.EXPENSE),
                Category(UUID.randomUUID().toString(), "Salary", CategoryType.INCOME)
            )
            val transactions = mutableListOf<Transaction>()

            route("/categories") {
                get { call.respond(categories) }
                post {
                    val newCat = call.receive<Category>()
                    categories.add(newCat)
                    call.respond(newCat)
                }
            }

            route("/transactions") {
                get {
                    val month = call.request.queryParameters["month"]
                    val filtered = if (month != null) {
                        transactions.filter { it.date.startsWith(month) }
                    } else transactions
                    call.respond(filtered)
                }
                post {
                    val tx = call.receive<Transaction>()
                    transactions.add(tx)
                    call.respond(tx)
                }
            }
        }
    }.start(wait = true)
}
