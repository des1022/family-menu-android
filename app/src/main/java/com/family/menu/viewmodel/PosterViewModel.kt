package com.family.menu.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.family.menu.data.model.OrderLine
import com.family.menu.data.repository.DishRepository
import com.family.menu.data.repository.RecordRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** 分享海报：加载某日点单数据 + 标题/底部文案编辑。 */
class PosterViewModel(
    val date: String,
    recordRepository: RecordRepository,
    dishRepository: DishRepository
) : ViewModel() {

    var lines by mutableStateOf<List<OrderLine>>(emptyList())
        private set

    var loaded by mutableStateOf(false)
        private set

    var title by mutableStateOf("今日家庭菜单")
        private set
    var footer by mutableStateOf("今天也要好好吃饭呀")
        private set

    init {
        viewModelScope.launch {
            val recs = recordRepository.observeByDate(date).first()
            val dishMap = dishRepository.observeAll().first().associateBy { it.id }
            lines = recs.mapNotNull { r ->
                dishMap[r.dishId]?.let { OrderLine(r.id, it, r.num, r.remark, r.createTime) }
            }
            loaded = true
        }
    }

    fun updateTitle(v: String) { title = v.take(12) }
    fun updateFooter(v: String) { footer = v.take(24) }
}
