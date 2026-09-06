package com.family.menu.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.menuDataStore: DataStore<Preferences> by preferencesDataStore(name = "family_menu_settings")

/** 轻量本地偏好：昵称（阶段二引入）、当前选中分类、排序方式等。 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val NICKNAME = stringPreferencesKey("nickname")
        val LAST_CATEGORY = stringPreferencesKey("last_category")
        val SORT_MODE = intPreferencesKey("sort_mode")
    }

    private val safeData = context.menuDataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }

    val nickname: Flow<String> = safeData.map { it[Keys.NICKNAME].orEmpty() }
    val lastCategory: Flow<String?> = safeData.map { it[Keys.LAST_CATEGORY] }
    val sortMode: Flow<Int> = safeData.map { it[Keys.SORT_MODE] ?: 0 }

    suspend fun setNickname(value: String) {
        context.menuDataStore.edit { it[Keys.NICKNAME] = value.trim() }
    }

    suspend fun setLastCategory(name: String?) {
        context.menuDataStore.edit {
            if (name.isNullOrEmpty()) it.remove(Keys.LAST_CATEGORY) else it[Keys.LAST_CATEGORY] = name
        }
    }

    suspend fun setSortMode(mode: Int) {
        context.menuDataStore.edit { it[Keys.SORT_MODE] = mode }
    }

    /** 清空全部设置（不影响菜品与记录数据） */
    suspend fun clearAll() {
        context.menuDataStore.edit { it.clear() }
    }
}

/** 排序模式：0 = 按添加时间倒序；1 = 按点单频次。 */
object SortMode {
    const val TIME = 0
    const val FREQUENCY = 1
}