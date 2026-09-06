package com.family.menu.data.repository

import com.family.menu.data.local.DailySummary
import com.family.menu.data.local.DishFreq
import com.family.menu.data.local.RecordEntity
import com.family.menu.data.local.dao.RecordDao
import kotlinx.coroutines.flow.Flow
class RecordRepository(private val recordDao: RecordDao) {

    fun observeByDate(date: String): Flow<List<RecordEntity>> = recordDao.observeByDate(date)
    fun observeSummaryByDate(date: String): Flow<DailySummary?> = recordDao.observeSummaryByDate(date)
    fun observeMonthSummaries(year: Int, month: Int): Flow<List<DailySummary>> {
        // month 1-9 需要补零对齐 yyyy-MM-dd 前缀
        val mm = if (month < 10) "0$month" else "$month"
        return recordDao.observeMonthSummaries("$year-$mm")
    }
    fun observeRecent(limit: Int = 100): Flow<List<RecordEntity>> = recordDao.observeRecent(limit)
    fun observeDishFreq(): Flow<List<DishFreq>> = recordDao.observeDishFreq()
    suspend fun getAll(): List<RecordEntity> = recordDao.getAll()

    suspend fun getById(id: Long): RecordEntity? = recordDao.getById(id)

    /** Task 2-16：同一日同一菜品自动合并（份数累加；减到 0 自动删除） */
    suspend fun upsert(date: String, dishId: Long, delta: Int, remark: String? = null) {
        val existing = recordDao.findRaw(date, dishId)
        if (existing == null) {
            if (delta <= 0) return
            recordDao.insert(
                RecordEntity(
                    date = date,
                    dishId = dishId,
                    num = delta,
                    remark = remark.orEmpty()
                )
            )
        } else {
            val next = (existing.num + delta).coerceAtLeast(0)
            if (next <= 0) {
                recordDao.deleteById(existing.id)
            } else {
                recordDao.updateNum(existing.id, next)
                if (!remark.isNullOrEmpty()) recordDao.updateRemark(existing.id, remark)
            }
        }
    }

    suspend fun updateNum(id: Long, num: Int) = recordDao.updateNum(id, num)
    suspend fun updateRemark(id: Long, remark: String) = recordDao.updateRemark(id, remark)
    suspend fun deleteById(id: Long) = recordDao.deleteById(id)
    suspend fun clearByDate(date: String) = recordDao.deleteByDate(date)
    suspend fun deleteByDateAndDish(date: String, dishId: Long) = recordDao.deleteByDateAndDish(date, dishId)
    suspend fun deleteUnconfirmedByDate(date: String) = recordDao.deleteUnconfirmedByDate(date)
    suspend fun markConfirmed(date: String) = recordDao.markConfirmed(date)
    suspend fun countConfirmedByDate(date: String): Int = recordDao.countConfirmedByDate(date)
}