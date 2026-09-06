package com.family.menu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.family.menu.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MineViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val nickname: StateFlow<String> = settingsRepository.nickname
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    suspend fun saveNickname(value: String): Boolean = runCatching {
        settingsRepository.setNickname(value)
    }.isSuccess

    fun clearCache() = viewModelScope.launch { settingsRepository.clearAll() }
}