package com.family.menu.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [DishEntity::class, CategoryEntity::class, RecordEntity::class],
    version = 3,
    exportSchema = false
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
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).addCallback(SEED_CALLBACK).build()

        /** v1 -> v2：菜品加「常吃」标记、点单记录加「已确认」标记（保留旧数据平滑升级） */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE dishes ADD COLUMN favorite INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE records ADD COLUMN confirmed INTEGER NOT NULL DEFAULT 0")
            }
        }

        /** v2 -> v3：菜品加 食材清单 + 多标签 两列（P2 4-01/4-02） */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE dishes ADD COLUMN ingredients TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE dishes ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
            }
        }

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