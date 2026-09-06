package com.family.menu.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ChevronLeft
import androidx.compose.material.icons.automirrored.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.family.menu.FamilyMenuApp
import com.family.menu.data.local.DailySummary
import com.family.menu.ui.components.AppTopBar
import com.family.menu.ui.theme.CalendarMarkBg
import com.family.menu.ui.theme.CalendarMarkText
import com.family.menu.ui.theme.CalendarTodayBg
import com.family.menu.ui.theme.CalendarTodayText
import com.family.menu.util.todayDateString
import com.family.menu.viewmodel.CalendarViewModel
import java.util.Calendar
import java.util.Locale

private data class CalCell(val date: String?, val day: Int)

@Composable
fun CalendarScreen(onOpenDate: (String) -> Unit = {}) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as FamilyMenuApp
    val vm = viewModel<CalendarViewModel>(factory = app.container.viewModelFactory)
    val summaries by vm.monthSummaries.collectAsStateWithLifecycle()
    val byDate = remember(summaries) { summaries.associateBy { it.date } }
    val today = todayDateString()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = "菜单日历") }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // 月份切换条
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { vm.prevMonth() }) {
                    Icon(Icons.AutoMirrored.Filled.ChevronLeft, contentDescription = "上月")
                }
                Text(
                    "${vm.year}年${vm.month}月",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                IconButton(onClick = { vm.nextMonth() }) {
                    Icon(Icons.AutoMirrored.Filled.ChevronRight, contentDescription = "下月")
                }
            }

            // 星期表头（周一开头）
            val weekNames = listOf("一", "二", "三", "四", "五", "六", "日")
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                weekNames.forEach { name ->
                    Text(
                        name,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 日期网格
            val cells = remember(vm.year, vm.month) { buildMonthCells(vm.year, vm.month) }
            cells.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    week.forEach { cell ->
                        DayCell(
                            cell = cell,
                            summary = cell.date?.let { byDate[it] },
                            isToday = cell.date == today,
                            enabled = cell.date != null && (cell.date == today || byDate[cell.date] != null),
                            onClick = { cell.date?.let(onOpenDate) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            Text(
                "· 橙色圆点表示当天点过菜，点击可查看菜单并复用到今天 ·",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun DayCell(
    cell: CalCell,
    summary: DailySummary?,
    isToday: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .alpha(if (enabled || cell.date == null) 1f else 0.45f)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (cell.date != null) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(if (isToday) CalendarTodayBg else Color.Transparent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${cell.day}",
                        color = if (isToday) CalendarTodayText else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                    )
                }
                if (summary != null && summary.totalNum > 0) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .background(CalendarMarkBg, CircleShape)
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            "${summary.totalNum}份",
                            fontSize = 9.sp,
                            color = CalendarMarkText,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

private fun buildMonthCells(year: Int, month: Int): List<CalCell> {
    val c = Calendar.getInstance().apply { set(year, month - 1, 1) }
    val firstDow = c.get(Calendar.DAY_OF_WEEK)   // 1=周日
    val offset = (firstDow + 5) % 7              // 周一开头偏移
    val days = c.getActualMaximum(Calendar.DAY_OF_MONTH)

    val cells = mutableListOf<CalCell>()
    repeat(offset) { cells += CalCell(null, 0) }
    for (d in 1..days) {
        val date = String.format(Locale.US, "%04d-%02d-%02d", year, month, d)
        cells += CalCell(date, d)
    }
    while (cells.size % 7 != 0) cells += CalCell(null, 0)
    return cells
}
