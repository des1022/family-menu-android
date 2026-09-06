package com.family.menu.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.family.menu.FamilyMenuApp
import com.family.menu.data.local.DishEntity
import com.family.menu.ui.components.AppTopBar
import com.family.menu.ui.components.ConfirmDialog
import com.family.menu.ui.components.EmptyState
import com.family.menu.ui.components.LocalImage
import com.family.menu.ui.navigation.Routes
import com.family.menu.util.formatPrice
import com.family.menu.viewmodel.DishManageViewModel

/** 菜品管理：全部菜品（含下架）+ 批量上架/下架/删除/改分类（Task 2-08） */
@Composable
fun DishManageScreen(navController: NavHostController) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as FamilyMenuApp
    val vm = viewModel<DishManageViewModel>(factory = app.container.viewModelFactory)
    val dishes by vm.dishes.collectAsStateWithLifecycle()
    val categories by vm.categories.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showMovePicker by remember { mutableStateOf(false) }

    val onCount = dishes.count { it.status == DishEntity.STATUS_ON }
    val offCount = dishes.size - onCount

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = if (vm.selecting) "已选 ${vm.selectedIds.size} 道" else "菜品管理",
                onBack = {
                    if (vm.selecting) vm.setSelecting(false) else navController.popBackStack()
                }
            )
        },
        bottomBar = {
            if (vm.selecting) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BatchAction("上架", enabled = vm.hasSelection) { vm.batchSetStatus(DishEntity.STATUS_ON) }
                    BatchAction("下架", enabled = vm.hasSelection) { vm.batchSetStatus(DishEntity.STATUS_OFF) }
                    BatchAction("改分类", enabled = vm.hasSelection) { showMovePicker = true }
                    BatchAction("删除", enabled = vm.hasSelection, danger = true) { showDeleteConfirm = true }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // 工具行
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (vm.selecting) "长按已选可取消，点行切换选择" else "共 ${dishes.size} 道 · 上架 $onCount · 下架 $offCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                if (vm.selecting) {
                    TextButton(onClick = { vm.toggleSelectAll() }) {
                        Text(if (vm.selectedIds.size == dishes.size && dishes.isNotEmpty()) "取消全选" else "全选")
                    }
                } else {
                    TextButton(onClick = { vm.setSelecting(true) }) { Text("批量管理") }
                }
            }

            if (dishes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState("还没有菜品\n到「我的 → 上传菜品」添加第一道菜")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(dishes, key = { it.id }) { dish ->
                        val checked = dish.id in vm.selectedIds
                        ManageRow(
                            dish = dish,
                            selecting = vm.selecting,
                            checked = checked,
                            onClick = {
                                if (vm.selecting) vm.toggleSelect(dish.id)
                                else navController.navigate(Routes.dishEdit(dish.id))
                            },
                            onLongClick = {
                                if (!vm.selecting) vm.setSelecting(true)
                                vm.toggleSelect(dish.id)
                            }
                        )
                    }
                }
            }
        }
    }

    // 批量改分类
    if (showMovePicker) {
        AlertDialog(
            onDismissRequest = { showMovePicker = false },
            title = { Text("把已选 ${vm.selectedIds.size} 道菜移到哪个分类？") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    categories.forEach { cat ->
                        TextButton(
                            onClick = {
                                showMovePicker = false
                                vm.batchMoveTo(cat.name)
                                Toast.makeText(context, "已移到「${cat.name}」", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(cat.name) }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showMovePicker = false }) { Text("取消") } }
        )
    }

    // 批量删除确认
    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "删除 ${vm.selectedIds.size} 道菜",
            message = "删除后不可恢复（图片一并删除，历史点单中的记录会同步移除）。确定继续吗？",
            confirmText = "删除",
            onConfirm = {
                showDeleteConfirm = false
                vm.batchDelete {
                    Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show()
                }
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@Composable
private fun RowScope.BatchAction(
    label: String,
    enabled: Boolean,
    danger: Boolean = false,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .weight(1f)
            .height(46.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (enabled && danger) MaterialTheme.colorScheme.errorContainer
                else if (enabled) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                !enabled -> MaterialTheme.colorScheme.onSurfaceVariant
                danger -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.primary
            }
        )
    }
}

@Composable
private fun ManageRow(
    dish: DishEntity,
    selecting: Boolean,
    checked: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (checked) 2.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LocalImage(
                path = dish.imagePath,
                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)),
                corner = 8.dp
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        dish.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(8.dp))
                    StatusTag(on = dish.status == DishEntity.STATUS_ON)
                }
                Text(
                    text = "${dish.category}" + (if (dish.price > 0) " · ¥${formatPrice(dish.price)}" else ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            if (selecting) {
                Checkbox(checked = checked, onCheckedChange = { onClick() })
            } else {
                Text(
                    "编辑",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusTag(on: Boolean) {
    val (bg, fg, label) = if (on) {
        Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary, "上架")
    } else {
        Triple(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant, "下架")
    }
    Box(modifier = Modifier.background(bg, RoundedCornerShape(6.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = fg)
    }
}
