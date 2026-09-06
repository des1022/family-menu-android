package com.family.menu.ui.screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.family.menu.FamilyMenuApp
import com.family.menu.data.model.OrderLine
import com.family.menu.ui.components.AppTopBar
import com.family.menu.ui.components.ConfirmDialog
import com.family.menu.ui.components.EmptyState
import com.family.menu.ui.components.LocalImage
import com.family.menu.ui.components.PrimaryButton
import com.family.menu.ui.components.SecondaryOutlineButton
import com.family.menu.ui.theme.PriceColor
import com.family.menu.util.dateCNWithWeek
import com.family.menu.util.formatPrice
import com.family.menu.viewmodel.OrderViewModel

@Composable
fun OrderScreen(
    onBack: () -> Unit,
    onGoHome: () -> Unit,
    onGoPoster: (String) -> Unit
) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as FamilyMenuApp
    val vm = viewModel<OrderViewModel>(factory = app.container.viewModelFactory)
    val lines by vm.lines.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    var showClearConfirm by remember { mutableStateOf(false) }
    var showDone by remember { mutableStateOf(false) }
    var remarkTarget by remember { mutableStateOf<OrderLine?>(null) }
    var remarkText by remember { mutableStateOf("") }

    val totalNum = lines.sumOf { it.num }
    val totalAmount = vm.totalAmount

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = "今日点单 · ${dateCNWithWeek(vm.date)}", onBack = onBack) },
        bottomBar = {
            if (lines.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "共 ${lines.size} 道 · $totalNum 份",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        if (totalAmount > 0) {
                            Text(
                                "合计 ¥${formatPrice(totalAmount)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = PriceColor
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SecondaryOutlineButton(
                            text = "清空全部",
                            onClick = { showClearConfirm = true },
                            modifier = Modifier.weight(1f)
                        )
                        PrimaryButton(
                            text = "确认点单",
                            onClick = {
                                vm.confirm()
                                showDone = true
                            },
                            modifier = Modifier.weight(1.3f)
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (lines.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(120.dp))
                EmptyState("今天还没点单\n去首页挑几道喜欢的菜吧")
                Spacer(Modifier.height(16.dp))
                SecondaryOutlineButton(text = "去首页点菜", onClick = onGoHome)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 140.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(lines, key = { it.recordId }) { line ->
                    OrderLineCard(
                        line = line,
                        onInc = { vm.inc(line.dish.id) },
                        onDec = { vm.dec(line.dish.id) },
                        onRemove = { vm.remove(line.recordId) },
                        onEditRemark = {
                            remarkText = line.remark
                            remarkTarget = line
                        }
                    )
                }
            }
        }
    }

    // 单菜备注编辑
    remarkTarget?.let { line ->
        AlertDialog(
            onDismissRequest = { remarkTarget = null },
            title = { Text("给「${line.dish.name}」加备注") },
            text = {
                OutlinedTextField(
                    value = remarkText,
                    onValueChange = { if (it.length <= 40) remarkText = it },
                    placeholder = { Text("例如：少放辣 / 不加香菜", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.updateRemark(line.recordId, remarkText.trim())
                    remarkTarget = null
                    Toast.makeText(context, "备注已保存", Toast.LENGTH_SHORT).show()
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { remarkTarget = null }) { Text("取消") }
            }
        )
    }

    // 清空今日二次确认（已确认过的菜单会保留在日历，仅清未确认的新选）
    if (showClearConfirm) {
        ConfirmDialog(
            title = "清空今日点单",
            message = "清空后，今天已「确认点单」的菜单会保留在日历中，仅移除本次未确认的新选。\n确定清空吗？",
            confirmText = "清空",
            onConfirm = {
                showClearConfirm = false
                vm.clearToday { msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                }
            },
            onDismiss = { showClearConfirm = false }
        )
    }

    // 确认点单成功 → 分享海报 / 返回首页
    if (showDone) {
        AlertDialog(
            onDismissRequest = { showDone = false },
            title = { Text("点单完成 🎉") },
            text = { Text("今天的菜单已记录到日历，想分享给家人吗？") },
            confirmButton = {
                TextButton(onClick = {
                    showDone = false
                    onGoPoster(vm.date)
                }) { Text("生成分享海报") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDone = false
                    onGoHome()
                }) { Text("返回首页") }
            }
        )
    }
}

@Composable
private fun OrderLineCard(
    line: OrderLine,
    onInc: () -> Unit,
    onDec: () -> Unit,
    onRemove: () -> Unit,
    onEditRemark: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LocalImage(
                path = line.dish.imagePath,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(10.dp)),
                corner = 10.dp
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    line.dish.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                if (line.dish.price > 0) {
                    Text(
                        "¥${formatPrice(line.dish.price)} /份",
                        style = MaterialTheme.typography.bodySmall,
                        color = PriceColor
                    )
                }
                // 备注预览（可点击编辑）
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onEditRemark)
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.EditNote,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = line.remark.ifBlank { "加备注（少辣/忌口…）" },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (line.remark.isBlank()) MaterialTheme.colorScheme.outline
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            Spacer(Modifier.width(6.dp))
            // 份数步进 + 删除
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDec, modifier = Modifier.size(30.dp)) {
                        Icon(
                            Icons.Filled.Remove,
                            contentDescription = "减少",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        "×${line.num}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(40.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    IconButton(onClick = onInc, modifier = Modifier.size(30.dp)) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "增加",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    "删除",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable(onClick = onRemove)
                        .padding(4.dp)
                )
            }
        }
    }
}
