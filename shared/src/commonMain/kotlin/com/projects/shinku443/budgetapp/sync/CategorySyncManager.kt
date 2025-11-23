package com.projects.shinku443.budgetapp.sync

import co.touchlab.kermit.Logger
import com.projects.shinku443.budgetapp.api.ApiClient
import com.projects.shinku443.budgetapp.db.BudgetDatabase
import com.projects.shinku443.budgetapp.model.Category
import com.projects.shinku443.budgetapp.model.toCategoryRequest
import com.projects.shinku443.budgetapp.util.YearMonth
import com.projects.shinku443.budgetapp.util.mapper.toDb
import com.projects.shinku443.budgetapp.util.mapper.toDomain
import io.ktor.util.date.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CategorySyncManager(
    private val api: ApiClient,
    private val db: BudgetDatabase
) : SyncManager {

    private val categoryQueries = db.categoryQueries

    private val _status = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    override val status: StateFlow<SyncStatus> = _status.asStateFlow()

    private suspend fun needsSync(): Boolean {
        // Check for local deletions
        val deletedLocally = categoryQueries.selectAllIncludingDeleted().executeAsList()
            .any { it.is_deleted == 1L }
        if (deletedLocally) {
            Logger.d("CategorySyncManager") { "Sync needed: Local deletions found." }
            return true
        }

        // Check for local creations
        val localCreations = categoryQueries.selectAll().executeAsList()
            .any { it.id.startsWith("local_") }
        if (localCreations) {
            Logger.d("CategorySyncManager") { "Sync needed: Local creations found." }
            return true
        }

        // Fetch remote and local categories
        val remoteCats = api.get<List<Category>>("/categories")
        val localCats = categoryQueries.selectAll().executeAsList().map { it.toDomain() }

        // Check for size difference
        if (remoteCats.size != localCats.size) {
            Logger.d("CategorySyncManager") { "Sync needed: Different number of categories." }
            return true
        }

        // Check for content difference
        val remoteSet = remoteCats.toSet()
        val localSet = localCats.toSet()
        if (remoteSet != localSet) {
            Logger.d("CategorySyncManager") { "Sync needed: Category content differs." }
            return true
        }

        Logger.d("CategorySyncManager") { "No sync needed for categories." }
        return false
    }

    override suspend fun sync(month: YearMonth?) {
        if (!needsSync()) {
            _status.value = SyncStatus.Success(getTimeMillis())
            return
        }

        _status.value = SyncStatus.Syncing
        try {
            val remoteCats = api.get<List<Category>>("/categories")
            val remoteIds = remoteCats.map { it.id }.toSet()
            val localCatsIncludingDeleted = categoryQueries.selectAllIncludingDeleted()
                .executeAsList()
                .map { it.toDomain() }

            // 1. Sync offline deletions
            val deletedCats = localCatsIncludingDeleted.filter { it.isDeleted }
            if (deletedCats.isNotEmpty()) {
                try {
                    Logger.d("trying")
                    val deleteResponse = api.post<String>("/categories/batch", deletedCats.map { it.id }) // Changed to String
                    Logger.d("CategorySyncManager") { "Batch delete response: $deleteResponse" }
                    categoryQueries.deleteByIds(deletedCats.map { it.id })
                } catch (e: Exception) {
                    Logger.e("CategorySyncManager") { "Batch delete failed: ${e.message}" }
                }
            }

            // 2. Sync offline creations/updates
            val offlineNewCats = localCatsIncludingDeleted.filter { it.id.startsWith("local_") }
            if (offlineNewCats.isNotEmpty()) {
                try {
                    val syncedCats =
                        api.post<List<Category>>("/categories/batch",  offlineNewCats.map { it.toCategoryRequest() })
                    categoryQueries.transaction {
                        offlineNewCats.forEach { categoryQueries.deleteById(it.id) }
                        syncedCats.forEach { cat ->
                            val dbCat = cat.toDb()
                            categoryQueries.insertOrReplace(
                                id = dbCat.id,
                                name = dbCat.name,
                                type = dbCat.type,
                                isActive = dbCat.isActive,
                                updatedAt = dbCat.updatedAt,
                                is_deleted = dbCat.is_deleted,
                                color = dbCat.color,
                                icon = dbCat.icon
                            )
                        }
                    }
                } catch (e: Exception) {
                    Logger.e("CategorySyncManager") { "Batch create failed: ${e.message}" }
                }
            }

            // 3. Final refresh from remote
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
                        is_deleted = dbCat.is_deleted,
                        color = dbCat.color,
                        icon = dbCat.icon
                    )
                }
            }

            _status.value = SyncStatus.Success(getTimeMillis())
        } catch (e: Exception) {
            _status.value = SyncStatus.Error("Failed to sync categories: ${e.message}")
            Logger.e("CategorySyncManager") { "Failed to sync categories: ${e.message}" }
        }
    }
}
