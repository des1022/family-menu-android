package com.family.menu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.family.menu.data.local.CategoryEntity
import com.family.menu.data.repository.CategoryRepository
import com.family.menu.data.repository.DishRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val categoryRepository: CategoryRepository,
    private val dishRepository: DishRepository
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return false
        if (categoryRepository.countByName(trimmed) > 0) return false
        viewModelScope.launch {
            val sort = (categories.value.maxOfOrNull { it.sort } ?: -1) + 1
            categoryRepository.add(CategoryEntity(name = trimmed, sort = sort))
        }
        return true
    }

    fun rename(category: CategoryEntity, newName: String) = viewModelScope.launch {
        categoryRepository.update(category.copy(name = newName.trim()))
    }

    fun delete(category: CategoryEntity, moveTo: String?) = viewModelScope.launch {
        if (moveTo != null) dishRepository.moveCategory(category.name, moveTo)
        categoryRepository.delete(category.id)
    }

    fun reorder(ordered: List<CategoryEntity>) = viewModelScope.launch {
        ordered.forEachIndexed { index, cat ->
            if (cat.sort != index) categoryRepository.updateSort(cat.id, index)
        }
    }
}