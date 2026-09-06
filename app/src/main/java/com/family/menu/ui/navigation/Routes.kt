package com.family.menu.ui.navigation

object Routes {
    const val HOME = "home"
    const val CALENDAR = "calendar"
    const val MINE = "mine"

    // Tab 之外的页面
    const val DISH_EDIT = "dishEdit?dishId={dishId}"
    fun dishEdit(dishId: Long? = null) = "dishEdit?dishId=${dishId ?: -1L}"

    const val DISH_DETAIL = "dishDetail/{dishId}"
    fun dishDetail(dishId: Long) = "dishDetail/$dishId"

    const val ORDER = "order"
    const val CATEGORY = "category"
    const val SETTINGS = "settings"
    const val DISH_MANAGE = "dishManage"
    const val STATS = "stats"

    const val CALENDAR_DETAIL = "calendarDetail/{date}"
    fun calendarDetail(date: String) = "calendarDetail/$date"

    const val POSTER = "poster/{date}"
    fun poster(date: String) = "poster/$date"
}