package com.projects.shinku443.budget_app.viewmodel

import Category
import com.projects.shinku443.budget_app.model.CategoryType
import com.projects.shinku443.budget_app.repository.CategoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CategoryViewModel(private val repo: CategoryRepository) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    fun loadCategories() = scope.launch {
        _categories.value = repo.getCategories()
    }

    fun createCategory(name: String, type: CategoryType) = scope.launch {
        repo.createCategory(name, type)
        loadCategories()
    }
}
