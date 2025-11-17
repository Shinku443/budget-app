package com.projects.shinku443.budget_app

import com.projects.shinku443.budget_app.model.Category
import com.projects.shinku443.budget_app.model.CategoryRequest
import com.projects.shinku443.budget_app.model.CategoryType
import com.projects.shinku443.budget_app.model.Transaction
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import java.util.*

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
                Category(
                    UUID.randomUUID().toString(),
                    "Groceries",
                    CategoryType.EXPENSE,
                    isActive = true,
                    updatedAt = System.currentTimeMillis()
                ),
                Category(
                    UUID.randomUUID().toString(),
                    "Salary",
                    CategoryType.INCOME,
                    isActive = true,
                    updatedAt = System.currentTimeMillis()
                )
            )

            val transactions = mutableListOf<Transaction>()

            route("/categories") {
                get { call.respond(categories) }

                post {
                    val req = call.receive<CategoryRequest>()
                    val newCat = Category(
                        id = UUID.randomUUID().toString(),
                        name = req.name,
                        type = req.type, // unified naming
                        isActive = req.isActive,
                        updatedAt = System.currentTimeMillis()
                    )
                    categories.add(newCat)
                    call.respond(newCat)
                }

                put("/{id}") {
                    val id = call.parameters["id"]
                    if (id != null) {
                        val req = call.receive<CategoryRequest>()
                        val updated = Category(
                            id = id,
                            name = req.name,
                            type = req.type,
                            isActive = req.isActive,
                            updatedAt = System.currentTimeMillis()
                        )
                        categories.removeAll { it.id == id }
                        categories.add(updated)
                        call.respond(updated)
                    } else {
                        call.respondText("Missing id", status = io.ktor.http.HttpStatusCode.BadRequest)
                    }
                }

                delete("/{id}") {
                    val id = call.parameters["id"]
                    if (id != null) {
                        categories.removeAll { it.id == id }
                        call.respondText("Category deleted")
                    } else {
                        call.respondText("Missing id", status = io.ktor.http.HttpStatusCode.BadRequest)
                    }
                }
            }

            route("/transactions") {
                get {
                    val month = call.request.queryParameters["month"]
                    val filtered = if (month != null) {
                        transactions.filter { tx ->
                            // naive filter: createdAt formatted as yyyy-MM
                            java.time.Instant.ofEpochMilli(tx.createdAt)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                                .toString()
                                .startsWith(month)
                        }
                    } else transactions
                    call.respond(filtered)
                }

                post {
                    val tx = call.receive<Transaction>().copy(
                        id = UUID.randomUUID().toString(),
                        createdAt = System.currentTimeMillis()
                    )
                    transactions.add(tx)
                    call.respond(tx)
                }
            }
        }
    }.start(wait = true)
}
