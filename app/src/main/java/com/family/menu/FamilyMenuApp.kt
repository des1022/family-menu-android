package com.family.menu

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.family.menu.data.local.AppDatabase
import com.family.menu.data.repository.CategoryRepository
import com.family.menu.data.repository.DishRepository
import com.family.menu.data.repository.RecordRepository
import com.family.menu.data.repository.SettingsRepository
import com.family.menu.util.ImageStore
import com.family.menu.viewmodel.CalendarViewModel
import com.family.menu.viewmodel.CategoryViewModel
import com.family.menu.viewmodel.DishDetailViewModel
import com.family.menu.viewmodel.DishEditViewModel
import com.family.menu.viewmodel.DishManageViewModel
import com.family.menu.viewmodel.HomeViewModel
import com.family.menu.viewmodel.MineViewModel
import com.family.menu.viewmodel.OrderViewModel
import com.family.menu.viewmodel.SettingsViewModel
import com.family.menu.viewmodel.StatsViewModel

/**
 * 极简依赖容器：手工装配，不引入 DI 框架。
 * 阶段二 P0 引入 ImageStore 与具体功能时再补充仓储构造。
 */
class AppContainer(applicationContext: android.content.Context) {

    private val database: AppDatabase = AppDatabase.build(applicationContext)
    val imageStore: ImageStore = ImageStore(applicationContext)

    val dishRepository: DishRepository = DishRepository(database.dishDao())
    val categoryRepository: CategoryRepository = CategoryRepository(database.categoryDao())
    val recordRepository: RecordRepository = RecordRepository(database.recordDao())
    val settingsRepository: SettingsRepository = SettingsRepository(applicationContext)

    val viewModelFactory: ViewModelProvider.Factory by lazy {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                        HomeViewModel(dishRepository, categoryRepository, recordRepository, settingsRepository) as T
                    modelClass.isAssignableFrom(CategoryViewModel::class.java) ->
                        CategoryViewModel(categoryRepository, dishRepository) as T
                    modelClass.isAssignableFrom(DishEditViewModel::class.java) ->
                        DishEditViewModel(dishRepository, categoryRepository, imageStore) as T
                    modelClass.isAssignableFrom(DishDetailViewModel::class.java) ->
                        DishDetailViewModel(dishRepository, recordRepository, imageStore) as T
                    modelClass.isAssignableFrom(DishManageViewModel::class.java) ->
                        DishManageViewModel(dishRepository, categoryRepository, imageStore) as T
                    modelClass.isAssignableFrom(OrderViewModel::class.java) ->
                        OrderViewModel(recordRepository, dishRepository) as T
                    modelClass.isAssignableFrom(CalendarViewModel::class.java) ->
                        CalendarViewModel(recordRepository) as T
                    modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                        SettingsViewModel(settingsRepository, categoryRepository, dishRepository) as T
                    modelClass.isAssignableFrom(StatsViewModel::class.java) ->
                        StatsViewModel(dishRepository, recordRepository) as T
                    modelClass.isAssignableFrom(MineViewModel::class.java) ->
                        MineViewModel(settingsRepository) as T
                    else -> throw IllegalArgumentException("未知 ViewModel：$modelClass")
                }
            }
        }
    }
}

class FamilyMenuApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(applicationContext)
    }
}