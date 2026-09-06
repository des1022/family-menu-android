package com.family.menu.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.family.menu.data.local.DishEntity
import com.family.menu.data.local.RecordEntity
import com.family.menu.data.repository.DishRepository
import com.family.menu.data.repository.RecordRepository
import com.family.menu.util.dateStringOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

/** 排行行 */
data class RankRow(val name: String, val num: Int)

/** 分类占比 */
data class CatShare(val name: String, val num: Int)

/** 一段时间的汇总统计 */
data class StatsData(
    val dayCount: Int,
    val dishCount: Int,
    val totalNum: Int,
    val rank: List<RankRow>,
    val cats: List<CatShare>,
    val rangeText: String
) {
    val topName: String get() = rank.firstOrNull()?.name.orEmpty()
    val topNum: Int get() = rank.firstOrNull()?.num ?: 0
    val empty: Boolean get() = rank.isEmpty()
}

/** 饮食统计（P2 4-04）：本周/本月 频次排行 + 分类占比 + 小建议 */
class StatsViewModel(
    private val dishRepository: DishRepository,
    private val recordRepository: RecordRepository
) : ViewModel() {

    companion object {
        const val PERIOD_WEEK = 0
        const val PERIOD_MONTH = 1
    }

    var loaded by mutableStateOf(false)
        private set
    var period by mutableStateOf(PERIOD_WEEK)
        private set

    private var week = StatsData(0, 0, 0, emptyList(), emptyList(), "")
    private var month = StatsData(0, 0, 0, emptyList(), emptyList(), "")

    val data: StatsData get() = if (period == PERIOD_WEEK) week else month

    init {
        viewModelScope.launch {
            val records = recordRepository.getAll()
            val dishMap = dishRepository.observeAll().first().associateBy { it.id }
            val now = Calendar.getInstance()

            week = buildStats(records, dishMap, weekRange(now), "本周")
            month = buildStats(records, dishMap, monthRange(now), "本月")
            loaded = true
        }
    }

    fun selectPeriod(p: Int) {
        if (p == PERIOD_WEEK || p == PERIOD_MONTH) period = p
    }

    private fun buildStats(
        records: List<RecordEntity>,
        dishMap: Map<Long, DishEntity>,
        range: Pair<String, String>,
        label: String
    ): StatsData {
        val (from, to) = range
        val inRange = records.filter { it.date in from..to }

        val daySet = inRange.map { it.date }.toSet()
        val numByDish = HashMap<Long, Int>()
        val catNum = HashMap<String, Int>()
        inRange.forEach { r ->
            val dish = dishMap[r.dishId] ?: return@forEach
            numByDish[r.dishId] = (numByDish[r.dishId] ?: 0) + r.num
            catNum[dish.category] = (catNum[dish.category] ?: 0) + r.num
        }

        val rank = numByDish.entries
            .mapNotNull { (id, n) -> dishMap[id]?.let { RankRow(it.name, n) } }
            .sortedByDescending { it.num }
            .take(10)

        val cats = catNum.entries
            .sortedByDescending { it.value }
            .map { CatShare(it.key, it.value) }

        return StatsData(
            dayCount = daySet.size,
            dishCount = rank.size,
            totalNum = inRange.sumOf { it.num },
            rank = rank,
            cats = cats,
            rangeText = "$label · ${from} ~ ${to}"
        )
    }

    /** 本周一 ~ 本周日 */
    private fun weekRange(now: Calendar): Pair<String, String> {
        val c = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val dow = c.get(Calendar.DAY_OF_WEEK) // 1=周日
        val back = (dow + 5) % 7 // 距周一偏移
        c.add(Calendar.DAY_OF_MONTH, -back)
        val start = dateStringOf(c.timeInMillis)
        c.add(Calendar.DAY_OF_MONTH, 6)
        return start to dateStringOf(c.timeInMillis)
    }

    /** 本月首日 ~ 末日 */
    private fun monthRange(now: Calendar): Pair<String, String> {
        val c = Calendar.getInstance().apply {
            set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val start = dateStringOf(c.timeInMillis)
        val last = c.getActualMaximum(Calendar.DAY_OF_MONTH)
        c.set(Calendar.DAY_OF_MONTH, last)
        return start to dateStringOf(c.timeInMillis)
    }
}
