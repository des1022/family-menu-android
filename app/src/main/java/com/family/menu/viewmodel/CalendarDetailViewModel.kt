package com.family.menu.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.family.menu.data.model.OrderLine
import com.family.menu.data.repository.DishRepository
import com.family.menu.data.repository.RecordRepository
import com.family.menu.util.todayDateString
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** 历史菜单详情：某一天的点单清单 + 一键复用到今日 / 单菜复制到今日。 */
class CalendarDetailViewModel(
    val date: String,
    private val recordRepository: RecordRepository,
    private val dishRepository: DishRepository
) : ViewModel() {

    var lines by mutableStateOf<List<OrderLine>>(emptyList())
        private set

    var loaded by mutableStateOf(false)
        private set

    val isToday: Boolean get() = date == todayDateString()

    fun load() = viewModelScope.launch {
        refresh()
        loaded = true
    }

    private suspend fun refresh() {
        val recs = recordRepository.observeByDate(date).first()
        val dishMap = dishRepository.observeAll().first().associateBy { it.id }
        lines = recs.mapNotNull { r ->
            dishMap[r.dishId]?.let { OrderLine(r.id, it, r.num, r.remark, r.createTime) }
        }
    }

    /** 单菜复制到今日点单（份数累加） */
    fun copyOneToToday(line: OrderLine) = viewModelScope.launch {
        recordRepository.upsert(todayDateString(), line.dish.id, delta = line.num, remark = line.remark)
    }

    /** 一键复用整日到今日 */
    fun copyAllToToday(onDone: () -> Unit = {}) = viewModelScope.launch {
        lines.forEach { line ->
            recordRepository.upsert(todayDateString(), line.dish.id, delta = line.num, remark = line.remark)
        }
        onDone()
    }
}
