package com.family.menu.ui.screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.family.menu.FamilyMenuApp
import com.family.menu.ui.components.AppTopBar
import com.family.menu.ui.navigation.Routes
import com.family.menu.viewmodel.MineViewModel
import kotlinx.coroutines.launch

@Composable
fun MineScreen(navController: NavHostController) {
    val app = LocalContext.current.applicationContext as FamilyMenuApp
    val vm = viewModel<MineViewModel>(factory = app.container.viewModelFactory)
    val nickname by vm.nickname.collectAsStateWithLifecycle()
    val themeMode by vm.themeMode.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showNickname by remember { mutableStateOf(false) }
    var nickInput by remember { mutableStateOf("") }
    var showTheme by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = "我的") }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 用户卡（点击修改昵称）
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        nickInput = nickname
                        showNickname = true
                    },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text("点击设置昵称", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(
                            nickname.ifBlank { "未设置" },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            // 入口：上传菜品
            EntryRow(
                icon = { Icon(Icons.Filled.AddCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                title = "上传菜品",
                subtitle = "拍照或从相册选图，添加新菜",
                onClick = { navController.navigate(Routes.dishEdit()) }
            )
            // 入口：管理菜品
            EntryRow(
                icon = { Icon(Icons.Filled.List, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                title = "管理菜品",
                subtitle = "全部菜品：编辑、上/下架，长按进入批量操作",
                onClick = { navController.navigate(Routes.DISH_MANAGE) }
            )
            // 入口：分类管理
            EntryRow(
                icon = { Icon(Icons.Filled.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                title = "分类管理",
                subtitle = "增删分类（热菜/主食/汤品默认已有）",
                onClick = { navController.navigate(Routes.CATEGORY) }
            )
            // 入口：外观
            EntryRow(
                icon = { Icon(Icons.Filled.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                title = "外观",
                subtitle = when (themeMode) {
                    1 -> "浅色模式"
                    2 -> "深色模式"
                    else -> "跟随系统"
                },
                onClick = { showTheme = true }
            )

            Text(
                "使用说明：\n1. 「上传菜品」添加你家的拿手菜，可拍照或选相册图\n" +
                    "2. 首页按分类点菜，点右下角「今日点单」进入点单清单\n" +
                    "3. 确认点单后可在「日历」查看每天吃了什么，还能一键复用到今天\n" +
                    "4. 点单完成可一键生成家庭海报，分享到家人群\n" +
                    "5. 常点那道菜？在详情页点 ☆ 收藏进「常吃」",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    // 昵称编辑
    if (showNickname) {
        AlertDialog(
            onDismissRequest = { showNickname = false },
            title = { Text("设置昵称") },
            text = {
                OutlinedTextField(
                    value = nickInput,
                    onValueChange = { if (it.length <= 10) nickInput = it },
                    label = { Text("昵称（最多 10 字，留空清除）") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showNickname = false
                    scope.launch {
                        val ok = vm.saveNickname(nickInput.trim())
                        Toast.makeText(
                            context,
                            if (ok) "昵称已保存" else "保存失败，请重试",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { showNickname = false }) { Text("取消") } }
        )
    }

    // 外观模式
    if (showTheme) {
        AlertDialog(
            onDismissRequest = { showTheme = false },
            title = { Text("外观") },
            text = {
                Column {
                    listOf(
                        Triple(0, "跟随系统", "随手机自动切换深浅色"),
                        Triple(1, "浅色模式", "米白暖色系"),
                        Triple(2, "深色模式", "夜间护眼暖棕系")
                    ).forEach { (mode, name, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showTheme = false
                                    vm.setThemeMode(mode)
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (themeMode == mode) "● " else "○ ",
                                color = if (themeMode == mode) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Column {
                                Text(name, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showTheme = false }) { Text("取消") } }
        )
    }
}

@Composable
private fun EntryRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
