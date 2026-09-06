package com.family.menu.data.model

import com.family.menu.data.local.DishEntity

/** 点单记录 × 菜品 的组合行（清单/日历详情/海报共用） */
data class OrderLine(
    val recordId: Long,
    val dish: DishEntity,
    val num: Int,
    val remark: String,
    val createTime: Long
)
