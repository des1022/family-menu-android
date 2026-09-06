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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import kotlinx.coroutines.launch

/**
 * 分类管理（Task 2-01 / 2-02）：
 * - 默认分类已内置（热菜/主食/汤品），支持增/改/删；
 * - 行尾 ↑↓ 调整顺序（实时保存，首页 Tab 同步按 sort 排序）；
 * - 删除含菜品的分类时可选「把菜品移到其他分类」或「一并删除」。
 */
@Composable
fun CategoryScreen(onBack: () -> Unit) {
    val app = LocalContext.current.applicationContext as FamilyMenuApp
    val vm = viewModel<CategoryViewModel>(factory = app.container.viewModelFactory)
    val categories by vm.categories.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<CategoryEntity?>(null) }
    var input by remember { mutableStateOf("") }

    // 删除流程：先查该分类下是否有菜
    var deleting by remember { mutableStateOf<CategoryEntity?>(null) }
    var deleteCount by remember { mutableIntStateOf(-1) }
    var movingTo by remember { mutableStateOf<CategoryEntity?>(null) }

    LaunchedEffect(deleting) {
        val cat = deleting ?: return@LaunchedEffect
        deleteCount = -1
        deleteCount = app.container.dishRepository.countByCategory(cat.name)
    }

    val others = remember(categories, deleting) {
        categories.filter { it.id != deleting?.id }
    }

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
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EmptyState("还没有分类，点右下角 ＋ 新增")
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "提示：点行尾 ↑↓ 调整分类顺序（首页分类 Tab 会同步）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            items(categories, key = { it.id }) { cat ->
                val index = categories.indexOfFirst { c -> c.id == cat.id }
                CategoryRow(
                    cat = cat,
                    index = index,
                    total = categories.size,
                    onRename = {
                        input = cat.name
                        editing = cat
                    },
                    onMoveUp = { vm.move(cat.id, -1) },
                    onMoveDown = { vm.move(cat.id, 1) },
                    onDelete = { deleting = cat }
                )
            }
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
                    vm.add(input)
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

    // 删除分类
    deleting?.let { cat ->
        when {
            deleteCount < 0 -> Unit // 查询中
            deleteCount == 0 -> ConfirmDialog(
                title = "删除分类",
                message = "确定删除分类「${cat.name}」吗？",
                confirmText = "删除",
                onConfirm = {
                    deleting = null
                    vm.delete(cat, moveTo = null)
                },
                onDismiss = { deleting = null }
            )
            else -> AlertDialog(
                onDismissRequest = { deleting = null },
                title = { Text("「${cat.name}」下有 $deleteCount 道菜") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "删除前可以先把它下面的菜移到其他分类：",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (others.isEmpty()) {
                            Text("（没有其他分类可选）", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        deleting = null
                        vm.delete(cat, moveTo = null)
                    }) { Text("连菜一并删除", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = { deleting = null }) { Text("取消") }
                        if (others.isNotEmpty()) {
                            TextButton(onClick = {
                                deleting = null
                                movingTo = cat
                            }) { Text("选择移动分类") }
                        }
                    }
                }
            )
        }
    }

    // 选择「移动到哪个分类」
    movingTo?.let { cat ->
        AlertDialog(
            onDismissRequest = { movingTo = null },
            title = { Text("把菜品移到哪个分类？") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    others.forEach { target ->
                        TextButton(
                            onClick = {
                                movingTo = null
                                vm.delete(cat, moveTo = target.name)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("移动到「${target.name}」") }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { movingTo = null }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun CategoryRow(
    cat: CategoryEntity,
    index: Int,
    total: Int,
    onRename: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 4.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.Reorder,
                contentDescription = "拖动排序",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                cat.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            // 上移/下移
            IconButton(onClick = onMoveUp, enabled = index > 0, modifier = Modifier.size(34.dp)) {
                Icon(
                    Icons.Filled.KeyboardArrowUp,
                    contentDescription = "上移",
                    tint = if (index > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                )
            }
            IconButton(onClick = onMoveDown, enabled = index < total - 1, modifier = Modifier.size(34.dp)) {
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = "下移",
                    tint = if (index < total - 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                )
            }
            IconButton(onClick = onRename, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Filled.Edit, contentDescription = "重命名", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Filled.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
            }
        }
    }
}
