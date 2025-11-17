package com.projects.shinku443.budget_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projects.shinku443.budget_app.model.Category
import com.projects.shinku443.budget_app.model.CategoryType
import com.projects.shinku443.budget_app.repository.CategoryRepository
import com.projects.shinku443.budget_app.sync.SyncService
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
        refreshCategories()
    }

    fun refreshCategories() {
        viewModelScope.launch {
            syncService.syncCategories() // API → DB reconciliation
        }
    }

    fun createCategory(name: String, type: CategoryType) {
        viewModelScope.launch {
            repo.createCategory(name, type, isActive = true) // hits API + DB
            syncService.syncCategories()
        }
    }

    fun updateCategory(id: String, name: String, type: CategoryType, isActive: Boolean) {
        viewModelScope.launch {
            repo.updateCategory(id, name, type, isActive) // hits API + DB
            syncService.syncCategories()
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            repo.deleteCategory(id) // hits API + DB
            syncService.syncCategories()
        }
    }

    fun deleteCategories(list: List<String>) {
        viewModelScope.launch {
            for (cat in list) {
                repo.deleteCategory(cat)
            }
            syncService.syncCategories()
        }
    }
}
