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

    /** null = 新增；非 null = 编辑该菜品 */
    var editId by mutableStateOf<Long?>(null)
        private set

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

    private var originalImagePath: String = ""

    fun load(id: Long?) = viewModelScope.launch {
        editId = id
        if (id == null) {
            // 新增：默认选中第一个分类，便于快速录入
            category = categories.value.firstOrNull()?.name ?: ""
            loaded = true
            return@launch
        }
        dishRepository.getById(id)?.let {
            name = it.name
            category = it.category
            imagePath = it.imagePath
            originalImagePath = it.imagePath
            priceText = if (it.price > 0) formatPriceForEdit(it.price) else ""
            desc = it.desc
            status = it.status
        }
        loaded = true
    }

    fun updateName(v: String) { name = v.take(20) }
    fun updateCategory(v: String) { category = v }
    fun updateImagePath(v: String) { imagePath = v }
    fun updatePriceText(v: String) { priceText = v.filter { it.isDigit() || it == '.' } }
    fun updateDesc(v: String) { desc = v.take(100) }
    fun updateStatus(v: Int) { status = v }
    fun clearPickError() { pickError = null }

    /** 从相册/相机 Uri 读取并方形裁切压缩保存 */
    fun onPickImage(uri: android.net.Uri) = viewModelScope.launch {
        pickError = null
        val path = try { imageStore.saveSquareFromUri(uri) } catch (e: Exception) { null }
        if (path != null) {
            // 换了图：编辑模式下旧图留到保存成功后统一清理，这里只记录新路径
            imagePath = path
        } else {
            pickError = "无法读取所选图片，请换一张试试"
        }
    }

    fun validate(): String? = when {
        name.isBlank() -> "请填写菜品名称"
        category.isBlank() -> "请选择或填写分类"
        imagePath.isBlank() -> "请选择菜品图片"
        else -> null
    }

    /** 保存：新增或更新；若更换了图片则清理旧文件 */
    fun save(onSaved: () -> Unit) = viewModelScope.launch {
        val price = priceText.toDoubleOrNull() ?: 0.0
        val id = editId
        if (id == null) {
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
        } else {
            val changedImage = originalImagePath.isNotBlank() && originalImagePath != imagePath
            dishRepository.update(
                DishEntity(
                    id = id,
                    name = name.trim(),
                    category = category.trim(),
                    imagePath = imagePath,
                    price = price,
                    desc = desc.trim(),
                    status = status
                )
            )
            if (changedImage) imageStore.delete(originalImagePath)
        }
        onSaved()
    }

    private fun formatPriceForEdit(v: Double): String =
        if (v % 1.0 == 0.0) v.toLong().toString()
        else String.format(java.util.Locale.getDefault(), "%.2f", v)
}