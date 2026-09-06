package com.family.menu.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.family.menu.FamilyMenuApp
import com.family.menu.ui.components.AppTopBar
import com.family.menu.ui.components.EmptyState
import com.family.menu.viewmodel.StatsViewModel
import kotlin.math.roundToInt

private val RingColors = listOf(
    Color(0xFFD9582B), Color(0xFF6E8B5A), Color(0xFFC8A05C),
    Color(0xFFE88A4D), Color(0xFF7FA8B5), Color(0xFFB389A8),
    Color(0xFFD5B8A0), Color(0xFF9BB56E)
)

/** 饮食统计（P2 4-04）：本周/本月频次排行 + 分类占比 + 建议 */
@Composable
fun StatsScreen(onBack: () -> Unit) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as FamilyMenuApp
    val vm = viewModel<StatsViewModel>(factory = app.container.viewModelFactory)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = "饮食统计", onBack = onBack) }
    ) { padding ->
        if (!vm.loaded) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                androidx.compose.material3.CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
            return@Scaffold
        }
        val stats = vm.data
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 周/月切换
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = vm.period == StatsViewModel.PERIOD_WEEK,
                    onClick = { vm.selectPeriod(StatsViewModel.PERIOD_WEEK) },
                    label = { Text("本周") }
                )
                FilterChip(
                    selected = vm.period == StatsViewModel.PERIOD_MONTH,
                    onClick = { vm.selectPeriod(StatsViewModel.PERIOD_MONTH) },
                    label = { Text("本月") }
                )
            }

            if (stats.empty) {
                EmptyState("这段时间还没有点单记录\n去首页点几道菜，这里就会有统计啦")
                return@Column
            }

            Text(
                stats.rangeText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // 概览卡
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("${stats.dayCount}", "点单天", Modifier.weight(1f))
                StatTile("${stats.dishCount}", "吃过菜", Modifier.weight(1f))
                StatTile("${stats.totalNum}", "总份数", Modifier.weight(1f))
            }

            // 频次排行
            if (stats.rank.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("🍽 最爱排行", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        val max = stats.rank.first().num.coerceAtLeast(1)
                        stats.rank.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    row.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1
                                )
                                Text(
                                    "${row.num}份",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth((row.num.toFloat() / max).coerceIn(0.05f, 1f))
                                    .height(6.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
            }

            // 分类占比
            if (stats.cats.isNotEmpty()) {
                CategoryShareCard(stats)
            }

            // 小建议
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("💡 小建议", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))
                    Text(buildTip(stats), style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CategoryShareCard(stats: com.family.menu.viewmodel.StatsData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("🥘 分类占比", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 环形图
                val total = stats.totalNum.coerceAtLeast(1)
                Canvas(modifier = Modifier.size(120.dp)) {
                    val stroke = 18.dp.toPx()
                    var start = -90f
                    stats.cats.forEachIndexed { i, cat ->
                        val sweep = 360f * cat.num / total
                        drawArc(
                            color = RingColors[i % RingColors.size],
                            startAngle = start,
                            sweepAngle = sweep - if (sweep < 360f) 1f else 0f,
                            useCenter = false,
                            topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2),
                            size = androidx.compose.ui.geometry.Size(size.width - stroke, size.height - stroke),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
                        )
                        start += sweep
                    }
                }
                Spacer(Modifier.size(16.dp))
                // 图例
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    stats.cats.forEachIndexed { i, cat ->
                        val pct = (cat.num * 100.0 / total).roundToInt()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(RingColors[i % RingColors.size], RoundedCornerShape(3.dp))
                            )
                            Spacer(Modifier.size(6.dp))
                            Text(
                                "${cat.name} ${pct}%",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun buildTip(stats: com.family.menu.viewmodel.StatsData): String {
    if (stats.rank.isEmpty()) return "这段时间还没有点单记录，先好好吃一顿吧。"
    val top = stats.topName
    val names = stats.cats.map { it.name }
    val hasSoup = names.any { it.contains("汤") || it.contains("羹") }
    val hasStaple = names.any { it.contains("主食") || it.contains("饭") || it.contains("面") }
    val base = "这段时间最爱吃的是「$top」（${stats.topNum} 份）。"
    return base + when {
        !hasSoup && !hasStaple -> " 主食和汤水有点少，记得来碗饭、配道汤，吃得更舒服。"
        !hasSoup -> " 可以试着加一道汤，润一润更养胃。"
        !hasStaple -> " 主食别落下，米饭面条安排上。"
        else -> " 荤素搭配不错，继续保持～"
    }
}
