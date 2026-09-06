package com.family.menu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.family.menu.data.model.OrderLine
import com.family.menu.data.repository.DishRepository
import com.family.menu.data.repository.RecordRepository
import com.family.menu.util.todayDateString
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** 今日点单清单：当日 records × 菜品实时组合（份数加减、备注、删除、清空、确认）。 */
class OrderViewModel(
    private val recordRepository: RecordRepository,
    dishRepository: DishRepository
) : ViewModel() {

    val date: String = todayDateString()

    private val records = recordRepository.observeByDate(date)
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

    fun inc(dishId: Long) = viewModelScope.launch {
        recordRepository.upsert(date, dishId, delta = 1)
    }

    fun dec(dishId: Long) = viewModelScope.launch {
        recordRepository.upsert(date, dishId, delta = -1)
    }

    fun updateRemark(recordId: Long, remark: String) = viewModelScope.launch {
        recordRepository.updateRemark(recordId, remark)
    }

    fun remove(recordId: Long) = viewModelScope.launch {
        recordRepository.deleteById(recordId)
    }

    /** 清空当日全部（二次确认在 UI 层） */
    fun clearToday(onDone: () -> Unit = {}) = viewModelScope.launch {
        recordRepository.clearByDate(date)
        onDone()
    }
}
