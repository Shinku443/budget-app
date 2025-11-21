package com.projects.shinku443.budgetapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projects.shinku443.budgetapp.model.Category
import com.projects.shinku443.budgetapp.model.CategoryType
import com.projects.shinku443.budgetapp.repository.CategoryRepository
import com.projects.shinku443.budgetapp.sync.SyncService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val repo: CategoryRepository,
    private val syncService: SyncService
) : ViewModel() {

    val categories: StateFlow<List<Category>> = repo.observeCategories()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        syncAll()
    }

    fun syncAll() {
        viewModelScope.launch {
            syncService.syncAll() // Trigger full sync
        }
    }

    fun createCategory(name: String, type: CategoryType) {
        viewModelScope.launch {
//            repo.createCategory(name, type, isActive = true) // hits API + DB
            syncAll() // Re-sync after creation
        }
    }

    fun createCategory(name: String, type: CategoryType, color: Long, icon: String?) {
        viewModelScope.launch {
            repo.createCategory(name, type, isActive = true, color = color, icon = icon) // hits API + DB
            syncAll() // Re-sync after creation
        }
    }


    fun updateCategory(id: String, name: String, type: CategoryType, isActive: Boolean, color: Long, icon: String) {
        viewModelScope.launch {
            repo.updateCategory(id, name, type, isActive, color, icon) // hits API + DB
            syncAll() // Re-sync after update
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            repo.deleteCategory(id) // hits API + DB
            syncAll() // Re-sync after deletion
        }
    }

    fun deleteCategories(list: List<String>) {
        viewModelScope.launch {
            for (cat in list) {
                repo.deleteCategory(cat)
            }
            syncAll() // Re-sync after multiple deletions
        }
    }
}
