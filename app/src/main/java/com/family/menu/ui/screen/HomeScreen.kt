package com.family.menu.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items as staggeredItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.family.menu.FamilyMenuApp
import com.family.menu.data.local.DishEntity
import com.family.menu.data.repository.LayoutMode
import com.family.menu.data.repository.SortMode
import com.family.menu.ui.components.EmptyState
import com.family.menu.ui.components.LocalImage
import com.family.menu.ui.theme.PriceColor
import com.family.menu.util.formatPrice
import com.family.menu.viewmodel.HomeViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onOpenDish: (Long) -> Unit = {},
    onOpenOrder: () -> Unit = {}
) {
    val app = LocalContext.current.applicationContext as FamilyMenuApp
    val vm = viewModel<HomeViewModel>(factory = app.container.viewModelFactory)
    val categories by vm.categories.collectAsStateWithLifecycle()
    val dishes by vm.dishes.collectAsStateWithLifecycle()
    val todayRecords by vm.todayRecords.collectAsStateWithLifecycle()
    val totalNum = todayRecords.sumOf { it.num }

    val visible = vm.buildVisible(dishes)
    val chips = listOf<String?>(null) + categories.map { it.name }
    val isGrid = vm.layoutMode == LayoutMode.GRID

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (totalNum > 0) {
                ExtendedFloatingActionButton(
                    onClick = onOpenOrder,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.ShoppingCart, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("今日点单 · $totalNum")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // 头部：标题 + 排序/布局切换
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 16.dp, bottom = 0.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "今天吃什么？",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (vm.sortMode == SortMode.FREQUENCY) {
                        Text(
                            "正在按「常点」排序",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton(onClick = { vm.toggleSortMode() }) {
                    Icon(
                        Icons.Filled.SwapVert,
                        contentDescription = "切换排序",
                        tint = if (vm.sortMode == SortMode.FREQUENCY) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { vm.toggleLayoutMode() }) {
                    Icon(
                        if (isGrid) Icons.Filled.ViewList else Icons.Filled.GridView,
                        contentDescription = if (isGrid) "切换为列表" else "切换为网格",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 搜索
            OutlinedTextField(
                value = vm.keyword,
                onValueChange = vm::updateKeyword,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = { Text("搜索菜品", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
                )
            )

            // 分类 Tab
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                items(chips, key = { it ?: "__all__" }) { cat ->
                    val label = cat ?: "全部"
                    FilterChip(
                        selected = vm.selectedCategory == cat,
                        onClick = { vm.selectCategory(cat) },
                        label = { Text(label) }
                    )
                }
            }

            // 内容：空态 / 列表 / 网格
            if (visible.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    EmptyState("还没有上架的菜品\n到「我的 → 上传菜品」添加，并保持上架")
                }
            } else if (isGrid) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    staggeredItems(visible, key = { it.id }) { dish ->
                        val num = todayRecords.firstOrNull { it.dishId == dish.id }?.num ?: 0
                        DishGridCard(
                            dish = dish,
                            num = num,
                            onClick = { onOpenDish(dish.id) },
                            onAdd = { vm.addToCart(dish.id) },
                            onDec = { vm.decFromCart(dish.id) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(visible, key = { it.id }) { dish ->
                        val num = todayRecords.firstOrNull { it.dishId == dish.id }?.num ?: 0
                        DishCard(
                            dish = dish,
                            num = num,
                            onClick = { onOpenDish(dish.id) },
                            onAdd = { vm.addToCart(dish.id) },
                            onDec = { vm.decFromCart(dish.id) }
                        )
                    }
                    item { Spacer(Modifier.height(88.dp)) }
                }
            }
        }
    }
}

/** 加入/份数控件：两种布局共用 */
@Composable
private fun QuantityControl(
    num: Int,
    onAdd: () -> Unit,
    onDec: () -> Unit
) {
    if (num <= 0) {
        OutlinedButton(
            onClick = onAdd,
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            modifier = Modifier.height(30.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(2.dp))
            Text("加入", style = MaterialTheme.typography.bodySmall)
        }
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDec, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Filled.Remove,
                    contentDescription = "减一份",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                num.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(20.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            IconButton(onClick = onAdd, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "加一份",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/** 列表布局卡片 */
@Composable
private fun DishCard(
    dish: DishEntity,
    num: Int,
    onClick: () -> Unit,
    onAdd: () -> Unit,
    onDec: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            LocalImage(
                path = dish.imagePath,
                modifier = Modifier.size(88.dp).clip(RoundedCornerShape(12.dp)),
                corner = 12.dp
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    dish.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = dish.desc.ifBlank { dish.category }.take(24),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (dish.price > 0) "¥${formatPrice(dish.price)}" else dish.category,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (dish.price > 0) PriceColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    QuantityControl(num = num, onAdd = onAdd, onDec = onDec)
                }
            }
        }
    }
}

/** 网格布局卡片（方形大图 + 信息） */
@Composable
private fun DishGridCard(
    dish: DishEntity,
    num: Int,
    onClick: () -> Unit,
    onAdd: () -> Unit,
    onDec: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            LocalImage(
                path = dish.imagePath,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                corner = 0.dp
            )
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    dish.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (dish.price > 0) "¥${formatPrice(dish.price)}" else dish.category,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (dish.price > 0) PriceColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    QuantityControl(num = num, onAdd = onAdd, onDec = onDec)
                }
            }
        }
    }
}
