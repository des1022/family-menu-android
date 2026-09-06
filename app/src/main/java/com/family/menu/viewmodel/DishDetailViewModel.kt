package com.family.menu.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.family.menu.data.local.DishEntity
import com.family.menu.data.repository.DishRepository
import com.family.menu.util.ImageStore
import kotlinx.coroutines.launch

class DishDetailViewModel(
    private val dishRepository: DishRepository,
    private val imageStore: ImageStore
) : ViewModel() {

    var dish by mutableStateOf<DishEntity?>(null)
        private set
    var loaded by mutableStateOf(false)
        private set

    fun load(id: Long) = viewModelScope.launch {
        dish = dishRepository.getById(id)
        loaded = true
    }

    fun toggleStatus() = viewModelScope.launch {
        val d = dish ?: return@launch
        val next = if (d.status == DishEntity.STATUS_ON) DishEntity.STATUS_OFF else DishEntity.STATUS_ON
        dishRepository.setStatus(d.id, next)
        dish = d.copy(status = next)
    }

    fun delete(onDone: () -> Unit) = viewModelScope.launch {
        val d = dish ?: return@launch
        imageStore.delete(d.imagePath)
        dishRepository.delete(d.id)
        onDone()
    }
}