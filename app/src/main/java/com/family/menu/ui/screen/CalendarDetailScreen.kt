package com.family.menu.ui.screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.family.menu.FamilyMenuApp
import com.family.menu.data.model.OrderLine
import com.family.menu.ui.components.AppTopBar
import com.family.menu.ui.components.EmptyState
import com.family.menu.ui.components.LocalImage
import com.family.menu.ui.components.PrimaryButton
import com.family.menu.ui.theme.PriceColor
import com.family.menu.util.dateCN
import com.family.menu.util.formatPrice
import com.family.menu.util.formatTime
import com.family.menu.viewmodel.CalendarDetailViewModel

@Composable
fun CalendarDetailScreen(
    date: String,
    onBack: () -> Unit,
    onOpenDish: (Long) -> Unit,
    onGoOrder: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as FamilyMenuApp
    val vm: CalendarDetailViewModel = viewModel(key = date) {
        CalendarDetailViewModel(date, app.container.recordRepository, app.container.dishRepository)
    }
    LaunchedEffect(date) { vm.load() }

    val totalNum = vm.lines.sumOf { it.num }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = "${dateCN(date)} 菜单", onBack = onBack) },
        bottomBar = {
            if (vm.lines.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "共 ${vm.lines.size} 道 · $totalNum 份",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        if (vm.isToday) {
                            Text(
                                "这是今天的点单",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    if (vm.isToday) {
                        PrimaryButton(text = "去今日点单修改", onClick = onGoOrder)
                    } else {
                        PrimaryButton(
                            text = "一键复用到今日点单",
                            onClick = {
                                vm.copyAllToToday {
                                    Toast.makeText(context, "已全部复制到今日点单", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (vm.lines.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(100.dp))
                EmptyState(if (vm.isToday) "今天还没点单\n先到首页挑几道菜吧" else "这一天还没有点单记录")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(vm.lines, key = { it.recordId }) { line ->
                    HistoryLineCard(
                        line = line,
                        canCopy = !vm.isToday,
                        onOpen = { onOpenDish(line.dish.id) },
                        onCopyOne = {
                            vm.copyOneToToday(line)
                            Toast.makeText(context, "已复制「${line.dish.name}」到今日点单", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                item { Spacer(Modifier.height(120.dp)) }
            }
        }
    }
}

@Composable
private fun HistoryLineCard(
    line: OrderLine,
    canCopy: Boolean,
    onOpen: () -> Unit,
    onCopyOne: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LocalImage(
                path = line.dish.imagePath,
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)),
                corner = 10.dp
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        line.dish.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "×${line.num}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (line.remark.isNotBlank()) {
                    Text(
                        "备注：${line.remark}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                Text(
                    line.createTime.let { formatTime(it) } + (if (line.dish.price > 0) " · ¥${formatPrice(line.dish.price)}" else ""),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            if (canCopy) {
                Icon(
                    Icons.Filled.AddCircle,
                    contentDescription = "复制到今日",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(30.dp)
                        .clickable(onClick = onCopyOne)
                )
            }
        }
    }
}
