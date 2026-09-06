package com.family.menu.data.repository

import com.family.menu.data.local.DailySummary
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

    suspend fun getById(id: Long): RecordEntity? = recordDao.getById(id)

    /** Task 2-16：同一日同一菜品累加份数 */
    suspend fun upsert(date: String, dishId: Long, delta: Int, remark: String? = null) =
        recordDao.upsert(date, dishId, delta, remark)

    suspend fun updateNum(id: Long, num: Int) = recordDao.updateNum(id, num)
    suspend fun updateRemark(id: Long, remark: String) = recordDao.updateRemark(id, remark)
    suspend fun deleteById(id: Long) = recordDao.deleteById(id)
    suspend fun clearByDate(date: String) = recordDao.deleteByDate(date)
    suspend fun deleteByDateAndDish(date: String, dishId: Long) = recordDao.deleteByDateAndDish(date, dishId)
}