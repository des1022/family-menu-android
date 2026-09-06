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
import com.family.menu.data.repository.RecordRepository
import com.family.menu.data.repository.SettingsRepository
import com.family.menu.util.todayDateString
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val dishRepository: DishRepository,
    private val categoryRepository: CategoryRepository,
    private val recordRepository: RecordRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dishes: StateFlow<List<DishEntity>> = dishRepository.observeOnSale()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayDate: String = todayDateString()

    val todayRecords = recordRepository.observeByDate(todayDate)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var keyword by mutableStateOf("")
        private set
    var selectedCategory by mutableStateOf<String?>(null)
        private set
    var sortMode by mutableStateOf(0)
        private set

    val visibleDishes: List<DishEntity>
        get() {
            val kw = keyword.trim()
            return dishes.value.filter { dish ->
                val inCategory = selectedCategory == null || dish.category == selectedCategory
                val inKeyword = kw.isBlank() || dish.name.contains(kw, ignoreCase = true)
                inCategory && inKeyword
            }
        }

    val todayCount: Int get() = todayRecords.value.size

    fun selectCategory(name: String?) { selectedCategory = name }
    fun updateKeyword(value: String) { keyword = value }
    fun updateSortMode(mode: Int) {
        sortMode = mode
        viewModelScope.launch { settingsRepository.setSortMode(mode) }
    }

    fun addToCart(dishId: Long) = viewModelScope.launch {
        recordRepository.upsert(todayDate, dishId, delta = 1)
    }
}