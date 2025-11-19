package com.projects.shinku443.budget_app.sync

import co.touchlab.kermit.Logger
import com.projects.shinku443.budget_app.api.ApiClient
import com.projects.shinku443.budget_app.db.BudgetDatabase
import com.projects.shinku443.budget_app.model.Category
import com.projects.shinku443.budget_app.util.YearMonth
import com.projects.shinku443.budget_app.util.mapper.toDb
import com.projects.shinku443.budget_app.util.mapper.toDomain

class CategorySyncManager(
    private val api: ApiClient,
    private val db: BudgetDatabase
) : SyncManager {
    private val categoryQueries = db.categoryQueries

    override suspend fun sync(month: YearMonth?) { // Month parameter is ignored for categories
        try {
            val remoteCats = api.get<List<Category>>("/categories")
            val remoteIds = remoteCats.map { it.id }.toSet()
            val localCatsIncludingDeleted = categoryQueries.selectAllIncludingDeleted().executeAsList().map { it.toDomain() }

            // 1. Sync offline deletions
            val deletedCats = localCatsIncludingDeleted.filter { it.isDeleted }
            deletedCats.forEach { deletedCat ->
                if (deletedCat.id in remoteIds) {
                    try {
                        api.delete<Unit>("/categories/${deletedCat.id}")
                        categoryQueries.deleteById(deletedCat.id)
                    } catch (e: Exception) {
                        Logger.e("CategorySyncManager") { "Failed to sync deleted category: ${e.message}" }
                    }
                } else {
                    // Was created and deleted offline, just remove locally
                    categoryQueries.deleteById(deletedCat.id)
                }
            }

            // 2. Sync offline creations and updates
            val localActiveCats = localCatsIncludingDeleted.filter { !it.isDeleted }
            localActiveCats.forEach { localCat ->
                if (localCat.id.startsWith("local_")) {
                    // New category created offline
                    try {
                        val syncedCat = api.post<Category>("/categories", localCat)
                        categoryQueries.deleteById(localCat.id) // Remove temporary local-only cat
                        val dbCat = syncedCat.toDb()
                        categoryQueries.insertOrReplace(
                            id = dbCat.id,
                            name = dbCat.name,
                            type = dbCat.type,
                            isActive = dbCat.isActive,
                            updatedAt = dbCat.updatedAt,
                            is_deleted = dbCat.is_deleted
                        )
                    } catch (e: Exception) {
                        Logger.e("CategorySyncManager") { "Failed to sync new category: ${e.message}" }
                    }
                } else if (localCat.id in remoteIds) {
                    // Category that exists on remote, check if it needs update
                    val remoteCat = remoteCats.find { it.id == localCat.id }
                    if (remoteCat != null && (localCat.updatedAt ?: 0L) > (remoteCat.updatedAt ?: 0L)) {
                        try {
                            val updatedCat = api.put<Category>("/categories/${localCat.id}", localCat)
                            val dbCat = updatedCat.toDb()
                            categoryQueries.insertOrReplace(
                                id = dbCat.id,
                                name = dbCat.name,
                                type = dbCat.type,
                                isActive = dbCat.isActive,
                                updatedAt = dbCat.updatedAt,
                                is_deleted = dbCat.is_deleted
                            )
                        } catch (e: Exception) {
                            Logger.e("CategorySyncManager") { "Failed to sync updated category: ${e.message}" }
                        }
                    }
                }
            }

            // 3. Final refresh from remote to get latest state
            val finalRemoteCats = api.get<List<Category>>("/categories")
            categoryQueries.transaction {
                finalRemoteCats.forEach { cat ->
                    val dbCat = cat.toDb()
                    categoryQueries.insertOrReplace(
                        id = dbCat.id,
                        name = dbCat.name,
                        type = dbCat.type,
                        isActive = dbCat.isActive,
                        updatedAt = dbCat.updatedAt,
                        is_deleted = dbCat.is_deleted
                    )
                }
            }
        } catch (e: Exception) {
            Logger.e("CategorySyncManager") { "Failed to sync categories: ${e.message}" }
        }
    }
}
