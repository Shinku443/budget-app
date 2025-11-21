package com.projects.shinku443.budgetapp.sync

import co.touchlab.kermit.Logger
import com.projects.shinku443.budgetapp.api.ApiClient
import com.projects.shinku443.budgetapp.db.BudgetDatabase
import com.projects.shinku443.budgetapp.model.Category
import com.projects.shinku443.budgetapp.util.YearMonth
import com.projects.shinku443.budgetapp.util.mapper.toDb
import com.projects.shinku443.budgetapp.util.mapper.toDomain
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CategorySyncManager(
    private val api: ApiClient,
    private val db: BudgetDatabase
) : SyncManager {

    private val categoryQueries = db.categoryQueries

    private val _status = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val status: StateFlow<SyncStatus> = _status.asStateFlow()

    override suspend fun sync(month: YearMonth?) {
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
                    api.post<Unit>("/categories/batchDelete", deletedCats.map { it.id })
                    deletedCats.forEach { categoryQueries.deleteById(it.id) }
                } catch (e: Exception) {
                    Logger.e("CategorySyncManager") { "Batch delete failed: ${e.message}" }
                }
            }

            // 2. Sync offline creations/updates
            val offlineNewCats = localCatsIncludingDeleted.filter { it.id.startsWith("local_") }
            if (offlineNewCats.isNotEmpty()) {
                try {
                    val syncedCats = api.post<List<Category>>("/categories/batch", offlineNewCats)
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
