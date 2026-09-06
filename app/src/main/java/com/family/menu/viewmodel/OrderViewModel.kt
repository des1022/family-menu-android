package com.family.menu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.family.menu.data.model.OrderLine
import com.family.menu.data.repository.DishRepository
import com.family.menu.data.repository.RecordRepository
import com.family.menu.util.todayDateString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 今日点单清单：当日 records × 菜品实时组合（份数加减、备注、删除、清空、确认）。 */
class OrderViewModel(
    private val recordRepository: RecordRepository,
    dishRepository: DishRepository
) : ViewModel() {

    /** 自然日变化自动切换（跨午夜驻留后操作即翻新一天） */
    private val dateFlow = MutableStateFlow(todayDateString())

    /** 当前日期（写操作时校验并翻页） */
    val currentDate: String get() {
        val d = todayDateString()
        if (dateFlow.value != d) dateFlow.value = d
        return d
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val records = dateFlow
        .flatMapLatest { recordRepository.observeByDate(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val dishAll = dishRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 每条点单 + 对应菜品（菜品被删则剔除该行） */
    val lines: StateFlow<List<OrderLine>> =
        combine(records, dishAll) { recs, dishes ->
            val map = dishes.associateBy { it.id }
            recs.mapNotNull { r ->
                map[r.dishId]?.let { OrderLine(r.id, it, r.num, r.remark, r.createTime) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 合计金额（无价格菜品按 0 计） */
    val totalAmount: Double get() = lines.value.sumOf { it.dish.price * it.num }

    /** 今日食材汇总：跨所有已选菜按食材归并，值=涉及该食材的点单总份数（P2 4-01） */
    val ingredientSummary: List<Pair<String, Int>>
        get() {
            val map = LinkedHashMap<String, Int>()
            lines.value.forEach { line ->
                com.family.menu.util.parseIngredients(line.dish.ingredients).forEach { ig ->
                    map[ig] = (map[ig] ?: 0) + line.num
                }
            }
            return map.entries.sortedByDescending { it.value }.map { it.key to it.value }
        }

    fun inc(dishId: Long) = viewModelScope.launch {
        recordRepository.upsert(currentDate, dishId, delta = 1)
    }

    fun dec(dishId: Long) = viewModelScope.launch {
        recordRepository.upsert(currentDate, dishId, delta = -1)
    }

    fun updateRemark(recordId: Long, remark: String) = viewModelScope.launch {
        recordRepository.updateRemark(recordId, remark)
    }

    fun remove(recordId: Long) = viewModelScope.launch {
        recordRepository.deleteById(recordId)
    }

    /** 确认点单：把今日草稿标记为「已确认」锁定进日历（之后清空/重置不会再删掉它） */
    fun confirm() = viewModelScope.launch {
        recordRepository.markConfirmed(currentDate)
    }

    /**
     * 清空今日：
     * - 若当天已有「已确认」菜单，只清未确认的新选草稿，已确认记录保留；
     * - 否则整日清空。结果以文案回调，由界面 Toast 提示。
     */
    fun clearToday(onResult: (String) -> Unit) = viewModelScope.launch {
        val date = currentDate
        val kept = recordRepository.countConfirmedByDate(date)
        if (kept > 0) {
            recordRepository.deleteUnconfirmedByDate(date)
            onResult("已保留 $kept 道已确认菜单，本次未确认的新选已清空")
        } else {
            recordRepository.clearByDate(date)
            onResult("已清空今日点单")
        }
    }
}
