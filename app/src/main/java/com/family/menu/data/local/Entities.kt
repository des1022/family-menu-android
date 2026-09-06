package com.family.menu.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 菜品表。
 * imagePath 是本机 filesDir 下的相对路径（非网络 URL）。
 */
@Entity(tableName = "dishes")
data class DishEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val desc: String = "",
    val category: String,
    val imagePath: String,
    val price: Double = 0.0,
    /** 1 = 上架，0 = 下架 */
    val status: Int = STATUS_ON,
    /** 1 = 手动标记「常吃」（P1 3-01） */
    val favorite: Int = 0,
    /** 食材清单，顿号/逗号分隔，如「西红柿,鸡蛋,葱花」（P2 4-01） */
    val ingredients: String = "",
    /** 多标签，分号分隔，如「微辣;15分钟;家常」（P2 4-02） */
    val tags: String = "",
    val sortOrder: Long = 0L,
    val createTime: Long = System.currentTimeMillis()
) {
    companion object {
        const val STATUS_OFF = 0
        const val STATUS_ON = 1
    }
}

/** 分类表：与菜品通过 category 名称关联 */
@Entity(tableName = "categories", indices = [Index(value = ["name"], unique = true)])
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val sort: Int = 0,
    val createTime: Long = System.currentTimeMillis()
)

/**
 * 点单记录（按日汇总）：每天的点单记录。
 * 同一菜品在同一天多次点单时自动累加份数（用 unique(date, dishId) 索引在 DAO 层 upsert）。
 * 这是日历视图与历史详情的数据源。
 */
@Entity(
    tableName = "records",
    foreignKeys = [
        ForeignKey(
            entity = DishEntity::class,
            parentColumns = ["id"],
            childColumns = ["dishId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["date", "dishId"], unique = true), Index("date"), Index("dishId")]
)
data class RecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** yyyy-MM-dd 形式的本地日期字符串，便于日历按日聚合 */
    val date: String,
    val dishId: Long,
    val num: Int = 1,
    val remark: String = "",
    /** 1 = 已确认点单（锁定进日历，清空时保留）；0 = 草稿 */
    val confirmed: Int = 0,
    val createTime: Long = System.currentTimeMillis()
)

/** 日历每日汇总：date + 道数 + 总份数 */
data class DailySummary(
    val date: String,
    val dishCount: Int,
    val totalNum: Int
)

/** 菜品累计点单份数（频次排序「常点」用） */
data class DishFreq(
    val dishId: Long,
    val total: Long
)