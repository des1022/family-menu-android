package com.family.menu.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.family.menu.data.local.CategoryEntity
import com.family.menu.data.local.DishEntity
import com.family.menu.data.repository.CategoryRepository
import com.family.menu.data.repository.DishRepository
import com.family.menu.util.ImageStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 菜品管理（Task 2-08 批量操作）：
 * - 列出全部菜品（含下架），普通模式点击进入编辑；
 * - 「批量管理」进入多选模式：批量上架/下架/删除（连图）/改分类。
 */
class DishManageViewModel(
    private val dishRepository: DishRepository,
    private val categoryRepository: CategoryRepository,
    private val imageStore: ImageStore
) : ViewModel() {

    val dishes: StateFlow<List<DishEntity>> = dishRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var selecting by mutableStateOf(false)
        private set
    var selectedIds by mutableStateOf<Set<Long>>(emptySet())
        private set

    val hasSelection: Boolean get() = selectedIds.isNotEmpty()

    fun updateSelecting(v: Boolean) {
        selecting = v
        if (!v) selectedIds = emptySet()
    }

    fun toggleSelect(id: Long) {
        val s = selectedIds.toMutableSet()
        if (!s.add(id)) s.remove(id)
        selectedIds = s
    }

    fun toggleSelectAll() {
        selectedIds =
            if (selectedIds.size == dishes.value.size) emptySet()
            else dishes.value.map { it.id }.toSet()
    }

    fun batchSetStatus(status: Int) {
        val ids = selectedIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            dishRepository.setStatusBatch(ids, status)
            selectedIds = emptySet()
        }
    }

    fun batchMoveTo(category: String) {
        val ids = selectedIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            dishRepository.moveCategoryBatch(ids, category)
            selectedIds = emptySet()
        }
    }

    fun batchDelete(onDone: () -> Unit) {
        val ids = selectedIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            dishes.value.filter { it.id in ids }.forEach { dish ->
                runCatching { imageStore.delete(dish.imagePath) }
            }
            dishRepository.deleteBatch(ids)
            selectedIds = emptySet()
            onDone()
        }
    }
}
