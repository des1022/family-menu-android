package com.family.menu.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.family.menu.data.local.CategoryEntity
import com.family.menu.data.local.DishEntity
import com.family.menu.data.local.DishFreq
import com.family.menu.data.repository.CategoryRepository
import com.family.menu.data.repository.DishRepository
import com.family.menu.data.repository.LayoutMode
import com.family.menu.data.repository.RecordRepository
import com.family.menu.data.repository.SettingsRepository
import com.family.menu.data.repository.SortMode
import com.family.menu.util.todayDateString
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
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

    /** 仅上架菜（首页展示） */
    val dishes: StateFlow<List<DishEntity>> = dishRepository.observeOnSale()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 每道菜累计点单份数 */
    val dishFreq: StateFlow<List<DishFreq>> = recordRepository.observeDishFreq()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayDate: String = todayDateString()

    val todayRecords = recordRepository.observeByDate(todayDate)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var keyword by mutableStateOf("")
        private set
    var selectedCategory by mutableStateOf<String?>(null)
        private set
    var sortMode by mutableStateOf(SortMode.TIME)
        private set
    var layoutMode by mutableStateOf(LayoutMode.LIST)
        private set

    init {
        viewModelScope.launch {
            sortMode = settingsRepository.sortMode.first()
            layoutMode = settingsRepository.layoutMode.first()
        }
    }

    /** 首页汇总视图：分类/关键字过滤 + 「常吃」分组 + 时间/频次排序。在组合期调用可正确订阅状态。 */
    fun buildVisible(dishes: List<DishEntity>): List<DishEntity> {
        val kw = keyword.trim()
        val freq = dishFreq.value.associate { it.dishId to it.total }
        val isFav = selectedCategory == CAT_FAV

        val filtered = dishes.filter { dish ->
            val inKeyword = kw.isBlank() || dish.name.contains(kw, ignoreCase = true)
            if (isFav) {
                inKeyword && (dish.favorite == 1 || (freq[dish.id] ?: 0L) > 0L)
            } else {
                val inCategory = selectedCategory == null || dish.category == selectedCategory
                inCategory && inKeyword
            }
        }

        if (isFav) {
            // 常吃分组：手动标记的常吃最前，其余按累计点单份数降序
            return filtered.sortedWith(
                compareByDescending<DishEntity> { if (it.favorite == 1) Long.MAX_VALUE else (freq[it.id] ?: 0L) }
                    .thenByDescending { it.createTime }
            )
        }
        if (sortMode != SortMode.FREQUENCY) return filtered
        return filtered.sortedWith(
            compareByDescending<DishEntity> { freq[it.id] ?: 0L }
                .thenByDescending { it.createTime }
        )
    }

    val todayCount: Int get() = todayRecords.value.size

    /** 今日总份数（悬浮球显示用） */
    val todayTotalNum: Int get() = todayRecords.value.sumOf { it.num }

    /** 某菜今日已选份数，0 = 未选 */
    fun numOf(dishId: Long): Int =
        todayRecords.value.firstOrNull { it.dishId == dishId }?.num ?: 0

    fun selectCategory(name: String?) { selectedCategory = name }
    fun updateKeyword(value: String) { keyword = value }

    fun updateSortMode(mode: Int) {
        sortMode = mode
        viewModelScope.launch { settingsRepository.setSortMode(mode) }
    }

    fun toggleSortMode() {
        updateSortMode(if (sortMode == SortMode.TIME) SortMode.FREQUENCY else SortMode.TIME)
    }

    fun toggleLayoutMode() {
        val next = if (layoutMode == LayoutMode.LIST) LayoutMode.GRID else LayoutMode.LIST
        layoutMode = next
        viewModelScope.launch { settingsRepository.setLayoutMode(next) }
    }

    fun addToCart(dishId: Long) = viewModelScope.launch {
        recordRepository.upsert(todayDate, dishId, delta = 1)
    }

    fun decFromCart(dishId: Long) = viewModelScope.launch {
        recordRepository.upsert(todayDate, dishId, delta = -1)
    }

    companion object {
        /** 虚拟分类：「常吃」（手动收藏 ∪ 历史点过） */
        const val CAT_FAV = "__favorite__"
    }
}
