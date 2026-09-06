package com.family.menu.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.family.menu.data.local.RecordEntity
import com.family.menu.data.repository.RecordRepository
import com.family.menu.util.todayDateString
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OrderViewModel(
    private val recordRepository: RecordRepository
) : ViewModel() {

    val date: String = todayDateString()

    val records: StateFlow<List<RecordEntity>> = recordRepository.observeByDate(date)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var submitting by mutableStateOf(false)
        private set

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

    fun confirm() = viewModelScope.launch {
        submitting = true
        submitting = false
    }

    fun clearToday(onDone: () -> Unit = {}) = viewModelScope.launch {
        recordRepository.clearByDate(date)
        onDone()
    }
}