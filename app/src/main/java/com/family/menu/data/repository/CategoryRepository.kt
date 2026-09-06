package com.family.menu.data.repository

import com.family.menu.data.local.CategoryEntity
import com.family.menu.data.local.dao.CategoryDao
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {

    fun observeAll(): Flow<List<CategoryEntity>> = categoryDao.observeAll()
    suspend fun getById(id: Long): CategoryEntity? = categoryDao.getById(id)
    suspend fun findByName(name: String): CategoryEntity? = categoryDao.findByName(name)
    suspend fun countByName(name: String): Int = categoryDao.countByName(name)
    suspend fun add(category: CategoryEntity): Long = categoryDao.insert(category)
    suspend fun update(category: CategoryEntity) = categoryDao.update(category)
    suspend fun updateSort(id: Long, sort: Int) = categoryDao.updateSort(id, sort)
    suspend fun delete(id: Long) = categoryDao.delete(id)
}