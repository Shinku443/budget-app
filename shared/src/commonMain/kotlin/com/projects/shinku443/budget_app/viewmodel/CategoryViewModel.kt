package com.projects.shinku443.budget_app.viewmodel

import com.projects.shinku443.budget_app.model.Category
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.projects.shinku443.budget_app.model.CategoryType
import com.projects.shinku443.budget_app.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
class CategoryViewModel(
    private val repo: CategoryRepository
) : ViewModel() {
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    init { loadCategories() }

    fun loadCategories() {
        viewModelScope.launch {
            _categories.value = repo.getCategories()
        }
    }

    fun createCategory(name: String, type: CategoryType) {
        viewModelScope.launch {
            Logger.e("name: $name type: $type")
            repo.createCategory(name, type)
            loadCategories()
        }
    }

    fun updateCategory(id: String, name: String, type: CategoryType) {
        viewModelScope.launch {
            repo.updateCategory(id, name, type)
            loadCategories()
        }
    }

    fun deleteCategories(ids: List<String>) {
        viewModelScope.launch {
            repo.deleteCategories(ids)
            loadCategories()
        }
    }
}
