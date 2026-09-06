package com.family.menu.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.family.menu.FamilyMenuApp
import com.family.menu.data.local.DishEntity
import com.family.menu.ui.components.AppTopBar
import com.family.menu.ui.components.ConfirmDialog
import com.family.menu.ui.components.EmptyState
import com.family.menu.ui.components.LocalImage
import com.family.menu.util.formatPrice
import com.family.menu.viewmodel.DishDetailViewModel
import kotlinx.coroutines.launch

/** 菜品详情（Task 2-06）：大图 / 信息 / 加入点单 / 编辑 / 删除 */
@Composable
fun DishDetailScreen(
    dishId: Long,
    onEdit: (Long) -> Unit,
    onBack: () -> Unit,
    onAdded: () -> Unit
) {
    val app = LocalContext.current.applicationContext as FamilyMenuApp
    val vm = viewModel<DishDetailViewModel>(factory = app.container.viewModelFactory)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDelete by remember { mutableStateOf(false) }

    LaunchedEffect(dishId) { vm.load(dishId) }

    val dish = vm.dish

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = "菜品详情",
                onBack = onBack,
                actions = {
                    if (dish != null) {
                        IconButton(onClick = { showDelete = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (dish == null) {
            if (vm.loaded) {
                EmptyState("菜品不存在或已删除")
            }
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 大图
            LocalImage(path = dish.imagePath, modifier = Modifier.fillMaxWidth().height(300.dp), corner = 18.dp)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    dish.name,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                if (dish.status == DishEntity.STATUS_ON) {
                    Text(
                        "上架中",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        "已下架",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 分类 / 价格
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "分类：${dish.category}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                if (dish.price > 0) {
                    Text(
                        "¥${formatPrice(dish.price)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 说明
            if (dish.desc.isNotBlank()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        dish.desc,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // 加入点单
            Button(
                onClick = { vm.addToToday { onAdded() } },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(27.dp)
            ) {
                Text("＋ 加入今日点单", style = MaterialTheme.typography.titleMedium)
            }

            // 编辑 / 上下架
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { onEdit(dish.id) }, modifier = Modifier.weight(1f).height(48.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("编辑")
                }
                OutlinedButton(onClick = { scope.launch { vm.toggleStatus() } }, modifier = Modifier.weight(1f).height(48.dp)) {
                    Text(if (dish.status == DishEntity.STATUS_ON) "下架" else "上架")
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    if (showDelete) {
        ConfirmDialog(
            title = "删除菜品",
            message = "确定删除「${dish?.name}」？删除后不可恢复。",
            confirmText = "删除",
            onConfirm = {
                showDelete = false
                dish?.let { d -> scope.launch { vm.delete { onBack() } } }
            },
            onDismiss = { showDelete = false }
        )
    }
}