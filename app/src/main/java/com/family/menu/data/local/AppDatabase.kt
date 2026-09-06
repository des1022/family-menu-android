package com.family.menu.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

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
        ).addCallback(SEED_CALLBACK).build()

        /** 首次建库时写入默认分类（Task 2-01：热菜/主食/汤品） */
        private val SEED_CALLBACK = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL(
                    "INSERT OR IGNORE INTO categories(name, sort, createTime) VALUES" +
                        "('热菜', 0, 0), ('主食', 1, 0), ('汤品', 2, 0)"
                )
            }
        }
    }
}