package com.family.menu.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.family.menu.data.local.DailySummary
import com.family.menu.data.repository.RecordRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

class CalendarViewModel(
    private val recordRepository: RecordRepository
) : ViewModel() {

    private val monthFlow = MutableStateFlow(currentYear() to currentMonth())

    var year by mutableStateOf(currentYear())
        private set
    var month by mutableStateOf(currentMonth())
        private set

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthSummaries: StateFlow<List<DailySummary>> = monthFlow
        .flatMapLatest { (y, m) -> recordRepository.observeMonthSummaries(y, m) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun prevMonth() {
        val (ny, nm) = if (month == 1) Pair(year - 1, 12) else Pair(year, month - 1)
        applyMonth(ny, nm)
    }

    fun nextMonth() {
        val (ny, nm) = if (month == 12) Pair(year + 1, 1) else Pair(year, month + 1)
        applyMonth(ny, nm)
    }

    private fun applyMonth(ny: Int, nm: Int) {
        year = ny
        month = nm
        monthFlow.value = ny to nm
    }

    private fun currentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)
    private fun currentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1
}
