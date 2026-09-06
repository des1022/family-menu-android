package com.family.menu.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.family.menu.data.local.DishEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DishDao {

    @Query("SELECT * FROM dishes ORDER BY sortOrder ASC, createTime DESC")
    fun observeAll(): Flow<List<DishEntity>>

    @Query("SELECT * FROM dishes WHERE status = 1 ORDER BY sortOrder ASC, createTime DESC")
    fun observeOnSale(): Flow<List<DishEntity>>

    @Query("SELECT * FROM dishes WHERE id = :id")
    suspend fun getById(id: Long): DishEntity?

    @Insert
    suspend fun insert(dish: DishEntity): Long

    @Update
    suspend fun update(dish: DishEntity)

    @Query("UPDATE dishes SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: Int)

    @Query("UPDATE dishes SET status = :status WHERE id IN (:ids)")
    suspend fun updateStatusBatch(ids: List<Long>, status: Int)

    @Query("UPDATE dishes SET category = :to WHERE category = :from")
    suspend fun moveCategory(from: String, to: String): Int

    @Query("UPDATE dishes SET category = :category WHERE id IN (:ids)")
    suspend fun updateCategoryBatch(ids: List<Long>, category: String): Int

    @Query("DELETE FROM dishes WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM dishes WHERE id IN (:ids)")
    suspend fun deleteBatch(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM dishes WHERE id = :id")
    suspend fun exists(id: Long): Int

    @Query("SELECT COUNT(*) FROM dishes WHERE status = 1")
    suspend fun countOnSale(): Int

    @Query("SELECT COUNT(*) FROM dishes WHERE category = :category")
    suspend fun countByCategory(category: String): Int
}