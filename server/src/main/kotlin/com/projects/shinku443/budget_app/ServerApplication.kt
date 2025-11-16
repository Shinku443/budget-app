package com.projects.shinku443.budget_app

import Transaction
import com.projects.shinku443.budget_app.model.Category
import com.projects.shinku443.budget_app.model.CategoryRequest
import com.projects.shinku443.budget_app.model.CategoryType
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
                Category(UUID.randomUUID().toString(), "Groceries", CategoryType.EXPENSE),
                Category(UUID.randomUUID().toString(), "Salary", CategoryType.INCOME)
            )
            val transactions = mutableListOf<Transaction>()

            route("/categories") {
                get { call.respond(categories) }
                post {
                    val req = call.receive<CategoryRequest>()
                    val newCat = Category(
                        id = UUID.randomUUID().toString(),
                        name = req.name,
                        categoryType = req.categoryType
                    )
                    categories.add(newCat)
                    call.respond(newCat)
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
