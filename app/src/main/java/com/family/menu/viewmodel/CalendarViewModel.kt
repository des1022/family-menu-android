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

    var year by mutableStateOf(currentYear())
        private set
    var month by mutableStateOf(currentMonth())
        private set

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthSummaries: StateFlow<List<DailySummary>> = MutableStateFlow(year to month)
        .flatMapLatest { (y, m) -> recordRepository.observeMonthSummaries(y, m) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun prevMonth() {
        if (month == 1) { month = 12; year -= 1 } else month -= 1
    }

    fun nextMonth() {
        if (month == 12) { month = 1; year += 1 } else month += 1
    }

    private fun currentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)
    private fun currentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1
}