package com.family.menu.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [DishEntity::class, CategoryEntity::class, RecordEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dishDao(): com.family.menu.data.local.dao.DishDao
    abstract fun categoryDao(): com.family.menu.data.local.dao.CategoryDao
    abstract fun recordDao(): com.family.menu.data.local.dao.RecordDao

    companion object {
        fun build(context: Context): AppDatabase = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "family_menu.db"
        ).fallbackToToDestructiveMigration().build()
    }
}