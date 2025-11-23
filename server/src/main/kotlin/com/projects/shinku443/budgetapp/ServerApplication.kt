package com.projects.shinku443.budgetapp

import com.projects.shinku443.budgetapp.model.*
import io.ktor.http.*
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
import java.util.concurrent.ConcurrentHashMap

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json(Json { prettyPrint = true; ignoreUnknownKeys = true })
        }
        routing {
            get("/") {
                call.respondText("Budget App API is running 🚀")
            }

            // Use ConcurrentHashMap for thread-safe in-memory storage
            val categories = ConcurrentHashMap<String, Category>()
            val transactions = ConcurrentHashMap<String, Transaction>()

            // Seed initial data
            listOf(
                Category("default_category_groceries", "Groceries", CategoryType.EXPENSE, true, System.currentTimeMillis()),
                Category("default_category_salary", "Salary", CategoryType.INCOME, true, System.currentTimeMillis()),
                Category("default_category_savings", "Savings", CategoryType.SAVINGS, true, System.currentTimeMillis()),
                Category("default_category_rent", "Rent", CategoryType.EXPENSE, true, System.currentTimeMillis()),
                Category("default_category_entertainment", "Entertainment", CategoryType.EXPENSE, true, System.currentTimeMillis())
            ).forEach { categories[it.id] = it }

//            val categories = mutableListOf(
//                Category(
//                    "default_category_groceries",
//                    "Groceries",
//                    CategoryType.EXPENSE,
//                    isActive = true,
//                    updatedAt = System.currentTimeMillis()
//                ),
//                Category(
//                    "default_category_salary",
//                    "Salary",
//                    CategoryType.INCOME,
//                    isActive = true,
//                    updatedAt = System.currentTimeMillis()
//                ),
//                Category(
//                    "default_category_savings",
//                    "Savings",
//                    CategoryType.SAVINGS,
//                    isActive = true,
//                    updatedAt = System.currentTimeMillis()
//                ),
//                Category(
//                    "default_category_rent",
//                    "Rent",
//                    CategoryType.EXPENSE,
//                    isActive = true,
//                    updatedAt = System.currentTimeMillis()
//                ),
//                Category(
//                    "default_category_entertainment",
//                    "Entertainment",
//                    CategoryType.EXPENSE,
//                    isActive = true,
//                    updatedAt = System.currentTimeMillis()
//                ),
//                Category(
//                    "default_category_medical",
//                    "Medical",
//                    CategoryType.EXPENSE,
//                    isActive = true,
//                    updatedAt = System.currentTimeMillis()
//                ),
//                Category(
//                    "default_category_utilities",
//                    "Utilities",
//                    CategoryType.EXPENSE,
//                    isActive = true,
//                    updatedAt = System.currentTimeMillis()
//                ),
//                Category(
//                    "default_category_childcare",
//                    "Childcare",
//                    CategoryType.EXPENSE,
//                    isActive = true,
//                    updatedAt = System.currentTimeMillis()
//                ),
//                Category(
//                    "default_category_gas",
//                    "Gas",
//                    CategoryType.EXPENSE,
//                    isActive = true,
//                    updatedAt = System.currentTimeMillis()
//                ),
//                Category(
//                    "default_category_restaurants",
//                    "Restaurants",
//                    CategoryType.EXPENSE,
//                    isActive = true,
//                    updatedAt = System.currentTimeMillis()
//                ),
//                Category(
//                    "default_category_gym",
//                    "Gym",
//                    CategoryType.EXPENSE,
//                    isActive = true,
//                    updatedAt = System.currentTimeMillis()
//                )
//                // Add more categories as needed
//            )

            route("/categories") {
                get { call.respond(categories.values.toList()) }

                post {
                    val req = call.receive<Category>() // Receive the full Category object
                    val newCat = req.copy(updatedAt = System.currentTimeMillis())
                    categories[newCat.id] = newCat
                    call.respond(newCat)
                }

                post("/batch") {
                    val reqs = call.receive<List<Category>>()
                    val newCats = reqs.map { it.copy(updatedAt = System.currentTimeMillis()) }
                    newCats.forEach { categories[it.id] = it }
                    call.respond(newCats)
                }

                put("/{id}") {
                    val id = call.parameters["id"]
                    if (id != null && categories.containsKey(id)) {
                        val req = call.receive<Category>()
                        val updated = req.copy(updatedAt = System.currentTimeMillis())
                        categories[id] = updated
                        call.respond(updated)
                    } else {
                        call.respondText("Category not found", status = HttpStatusCode.NotFound)
                    }
                }

                delete("/{id}") {
                    val id = call.parameters["id"]
                    if (id != null && categories.remove(id) != null) {
                        call.respondText("Category deleted")
                    } else {
                        call.respondText("Category not found", status = HttpStatusCode.NotFound)
                    }
                }

                delete("/batch") {
                    val ids = call.receive<List<String>>()
                    var count = 0
                    ids.forEach { if (categories.remove(it) != null) count++ }
                    call.respondText("Deleted $count categories")
                }
            }

            route("/transactions") {
                get {
                    val month = call.request.queryParameters["month"]
                    val filtered = if (month != null) {
                        transactions.values.filter { it.date.startsWith(month) }
                    } else {
                        transactions.values.toList()
                    }
                    call.respond(filtered)
                }

                post {
                    val tx = call.receive<Transaction>()
                    val newTx = tx.copy(createdAt = System.currentTimeMillis())
                    transactions[newTx.id] = newTx
                    call.respond(newTx)
                }

                post("/batch") {
                    val reqs = call.receive<List<Transaction>>()
                    val newTxs = reqs.map { it.copy(createdAt = System.currentTimeMillis()) }
                    newTxs.forEach { transactions[it.id] = it }
                    call.respond(HttpStatusCode.OK) // Respond with OK, no need to return the list
                }

                put("/{id}") {
                    val id = call.parameters["id"]
                    if (id != null && transactions.containsKey(id)) {
                        val tx = call.receive<Transaction>()
                        val updatedTx = tx.copy(createdAt = transactions[id]!!.createdAt) // Keep original creation time
                        transactions[id] = updatedTx
                        call.respond(updatedTx)
                    } else {
                        call.respondText("Transaction not found", status = HttpStatusCode.NotFound)
                    }
                }

                delete("/clear") {
                    transactions.clear()
                    call.respondText("All transactions deleted")
                }

                delete("/batch") {
                    val ids = call.receive<List<String>>()
                    var count = 0
                    ids.forEach { if (transactions.remove(it) != null) count++ }
                    call.respondText("Deleted $count transactions")
                }

                delete("/{id}") {
                    val id = call.parameters["id"]
                    if (id != null && transactions.remove(id) != null) {
                        call.respondText("Transaction deleted")
                    } else {
                        call.respondText("Transaction not found", status = HttpStatusCode.NotFound)
                    }
                }
            }
        }
    }.start(wait = true)
}
