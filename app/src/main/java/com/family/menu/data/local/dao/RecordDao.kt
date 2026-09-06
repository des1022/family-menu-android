package com.family.menu.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.family.menu.data.local.DailySummary
import com.family.menu.data.local.RecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {

    @Query("SELECT * FROM records WHERE date = :date ORDER BY createTime ASC")
    fun observeByDate(date: String): Flow<List<RecordEntity>>

    /** 该日总道数与总份数，用于日历标记 */
    @Query(
        """
        SELECT date AS date,
               COUNT(*) AS dishCount,
               SUM(num) AS totalNum
        FROM records
        WHERE date = :date
        GROUP BY date
        """
    )
    fun observeSummaryByDate(date: String): Flow<DailySummary?>

    /** 月汇总：日历月视图每天一个标记 */
    @Query(
        """
        SELECT date AS date,
               COUNT(*) AS dishCount,
               SUM(num) AS totalNum
        FROM records
        WHERE date LIKE :monthPrefix || '%'
        GROUP BY date
        ORDER BY date ASC
        """
    )
    fun observeMonthSummaries(monthPrefix: String): Flow<List<DailySummary>>

    /** 全部记录的最近 N 条（"我的点单"列表备用） */
    @Query("SELECT * FROM records ORDER BY createTime DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE id = :id")
    suspend fun getById(id: Long): RecordEntity?

    /** 同一日同一菜品累加份数（Task 2-16 自动合并） */
    @Insert
    suspend fun insert(record: RecordEntity): Long

    @Transaction
    suspend fun upsert(date: String, dishId: Long, delta: Int, remark: String?) {
        val existing = findRaw(date, dishId)
        if (existing == null) {
            insert(
                RecordEntity(
                    date = date,
                    dishId = dishId,
                    num = delta.coerceAtLeast(1),
                    remark = remark.orEmpty(),
                    createTime = System.currentTimeMillis()
                )
            )
        } else {
            updateNum(existing.id, (existing.num + delta).coerceAtLeast(0))
            if (!remark.isNullOrEmpty()) updateRemark(existing.id, remark)
        }
    }

    @Query("SELECT * FROM records WHERE date = :date AND dishId = :dishId LIMIT 1")
    suspend fun findRaw(date: String, dishId: Long): RecordEntity?

    @Query("UPDATE records SET num = :num WHERE id = :id")
    suspend fun updateNum(id: Long, num: Int)

    @Query("UPDATE records SET remark = :remark WHERE id = :id")
    suspend fun updateRemark(id: Long, remark: String)

    @Query("DELETE FROM records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM records WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("DELETE FROM records WHERE date = :date AND dishId = :dishId")
    suspend fun deleteByDateAndDish(date: String, dishId: Long)
}