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
import com.family.menu.util.ImageStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DishEditViewModel(
    private val dishRepository: DishRepository,
    private val categoryRepository: CategoryRepository,
    private val imageStore: ImageStore
) : ViewModel() {

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var name by mutableStateOf("")
        private set
    var category by mutableStateOf("")
        private set
    var imagePath by mutableStateOf("")
        private set
    var priceText by mutableStateOf("")
        private set
    var desc by mutableStateOf("")
        private set
    var status by mutableStateOf(DishEntity.STATUS_ON)
        private set

    var loaded by mutableStateOf(false)
        private set

    var pickError by mutableStateOf<String?>(null)
        private set

    fun load(id: Long?) = viewModelScope.launch {
        if (id == null) { loaded = true; return@launch }
        dishRepository.getById(id)?.let {
            name = it.name
            category = it.category
            imagePath = it.imagePath
            priceText = if (it.price > 0) formatPriceForEdit(it.price) else ""
            desc = it.desc
            status = it.status
        }
        loaded = true
    }

    fun setName(v: String) { name = v.take(20) }
    fun setCategory(v: String) { category = v }
    fun setImagePath(v: String) { imagePath = v }
    fun setPriceText(v: String) { priceText = v.filter { it.isDigit() || it == '.' } }
    fun setDesc(v: String) { desc = v.take(100) }
    fun setStatus(v: Int) { status = v }
    fun clearPickError() { pickError = null }

    fun onPickImage(uri: android.net.Uri) = viewModelScope.launch {
        pickError = null
        val path = try { imageStore.saveFromUri(uri) } catch (e: Exception) { null }
        if (path != null) imagePath = path
        else pickError = "无法读取所选图片，请换一张试试"
    }

    fun validate(): String? = when {
        name.isBlank() -> "请填写菜品名称"
        category.isBlank() -> "请选择或填写分类"
        imagePath.isBlank() -> "请选择菜品图片"
        else -> null
    }

    fun save(onSaved: () -> Unit) = viewModelScope.launch {
        val price = priceText.toDoubleOrNull() ?: 0.0
        dishRepository.add(
            DishEntity(
                name = name.trim(),
                category = category.trim(),
                imagePath = imagePath,
                price = price,
                desc = desc.trim(),
                status = status
            )
        )
        onSaved()
    }

    private fun formatPriceForEdit(v: Double): String =
        if (v % 1.0 == 0.0) v.toLong().toString()
        else String.format(java.util.Locale.getDefault(), "%.2f", v)
}