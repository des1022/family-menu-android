package com.family.menu.data.repository

import com.family.menu.data.local.DishEntity
import com.family.menu.data.local.dao.DishDao
import kotlinx.coroutines.flow.Flow

class DishRepository(private val dishDao: DishDao) {

    fun observeAll(): Flow<List<DishEntity>> = dishDao.observeAll()
    fun observeOnSale(): Flow<List<DishEntity>> = dishDao.observeOnSale()
    suspend fun getById(id: Long): DishEntity? = dishDao.getById(id)
    suspend fun add(dish: DishEntity): Long = dishDao.insert(dish)
    suspend fun update(dish: DishEntity) = dishDao.update(dish)
    suspend fun setStatus(id: Long, status: Int) = dishDao.updateStatus(id, status)
    suspend fun setFavorite(id: Long, favorite: Int) = dishDao.updateFavorite(id, favorite)
    suspend fun setStatusBatch(ids: List<Long>, status: Int) = dishDao.updateStatusBatch(ids, status)
    suspend fun delete(id: Long) = dishDao.delete(id)
    suspend fun deleteBatch(ids: List<Long>) = dishDao.deleteBatch(ids)
    suspend fun moveCategory(from: String, to: String): Int = dishDao.moveCategory(from, to)
    suspend fun moveCategoryBatch(ids: List<Long>, to: String): Int = dishDao.updateCategoryBatch(ids, to)
    suspend fun countOnSale(): Int = dishDao.countOnSale()
    suspend fun countByCategory(category: String): Int = dishDao.countByCategory(category)
}