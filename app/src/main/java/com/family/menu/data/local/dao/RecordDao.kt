package com.family.menu.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.family.menu.data.local.DailySummary
import com.family.menu.data.local.DishFreq
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

    /** 同一日同一菜品累加份数（Task 2-16 自动合并）——合并逻辑在 Repository 层实现 */
    @Insert
    suspend fun insert(record: RecordEntity): Long

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

    /** 清空某日「未确认草稿」（保留已确认的菜单记录） */
    @Query("DELETE FROM records WHERE date = :date AND confirmed = 0")
    suspend fun deleteUnconfirmedByDate(date: String)

    /** 确认点单：把某日所有草稿标记为已确认 */
    @Query("UPDATE records SET confirmed = 1 WHERE date = :date")
    suspend fun markConfirmed(date: String)

    /** 某日已确认的道数 */
    @Query("SELECT COUNT(*) FROM records WHERE date = :date AND confirmed = 1")
    suspend fun countConfirmedByDate(date: String): Int

    @Query("DELETE FROM records WHERE date = :date AND dishId = :dishId")
    suspend fun deleteByDateAndDish(date: String, dishId: Long)

    /** 每道菜累计点单份数（含历史全部日期），供「常点」排序 */
    @Query("SELECT dishId AS dishId, SUM(num) AS total FROM records GROUP BY dishId")
    fun observeDishFreq(): Flow<List<DishFreq>>
}