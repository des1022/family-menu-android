package com.family.menu.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Reorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.family.menu.FamilyMenuApp
import com.family.menu.data.local.CategoryEntity
import com.family.menu.ui.components.AppTopBar
import com.family.menu.ui.components.ConfirmDialog
import com.family.menu.ui.components.EmptyState
import com.family.menu.viewmodel.CategoryViewModel

/**
 * 分类管理（Task 2-01）：
 * - 长按拖拽排序为 Task 2-02，本版先做增删改；
 * - 删除含菜品的分类时二次确认（移动分类阶段二补）。
 */
@Composable
fun CategoryScreen(onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as FamilyMenuApp
    val vm = viewModel<CategoryViewModel>(factory = app.container.viewModelFactory)
    val categories by vm.categories.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<CategoryEntity?>(null) }
    var deleting by remember { mutableStateOf<CategoryEntity?>(null) }
    var input by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = "分类管理", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                input = ""
                showAdd = true
            }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Filled.Add, contentDescription = "新增分类", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { padding ->
        if (categories.isEmpty()) {
            EmptyState("还没有分类，点右下角 ＋ 新增")
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(categories, key = { it.id }) { cat ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Reorder,
                            contentDescription = "排序（拖拽排序后续版本支持）",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            cat.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            input = cat.name
                            editing = cat
                        }) {
                            Icon(Icons.Filled.Edit, contentDescription = "重命名", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { deleting = cat }) {
                            Icon(Icons.Filled.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(72.dp)) }
        }
    }

    // 新增分类
    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text("新增分类") },
            text = {
                OutlinedTextField(
                    value = input,
                    onValueChange = { if (it.length <= 8) input = it },
                    label = { Text("分类名称（最多 8 字）") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showAdd = false
                    vm.add(input) // 内部有 8 字与空名校验
                }) { Text("新增") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("取消") } }
        )
    }

    // 重命名
    editing?.let { cat ->
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("重命名分类") },
            text = {
                OutlinedTextField(
                    value = input,
                    onValueChange = { if (it.length <= 8) input = it },
                    label = { Text("分类名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    editing = null
                    if (input.isNotBlank()) vm.rename(cat, input)
                }) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("取消") } }
        )
    }

    // 删除（含菜品的分类直接提示一并删除）
    deleting?.let { cat ->
        ConfirmDialog(
            title = "删除分类",
            message = "确定删除分类「${cat.name}」？\n该分类下的菜品也会一并删除，此操作不可恢复。",
            confirmText = "删除",
            onConfirm = {
                deleting = null
                vm.delete(cat, moveTo = null)
            },
            onDismiss = { deleting = null }
        )
    }
}