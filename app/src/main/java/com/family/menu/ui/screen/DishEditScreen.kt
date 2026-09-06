package com.family.menu.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.family.menu.FamilyMenuApp
import com.family.menu.data.local.DishEntity
import com.family.menu.ui.components.AppTopBar
import com.family.menu.ui.components.LoadingIndicator
import com.family.menu.ui.components.LocalImage
import com.family.menu.viewmodel.DishEditViewModel
import com.family.menu.viewmodel.TagOptionGroups
import kotlinx.coroutines.launch
import java.io.File

/**
 * 菜品上传 / 编辑（Task 2-03 / 2-04 / 2-07 骨架实现）：
 * 相册或拍照 → 方形中心裁切压缩 → 表单（名称/分类/价格/描述/上架）→ 保存。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishEditScreen(navController: androidx.navigation.NavHostController) {
    val app = LocalContext.current.applicationContext as FamilyMenuApp
    val vm = viewModel<DishEditViewModel>(factory = app.container.viewModelFactory)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // dishEdit 路由始终带 dishId 参数；<=0 表示「新增」
    val dishIdArg = navController.currentBackStackEntry
        ?.arguments?.getString("dishId")?.toLongOrNull()?.takeIf { it > 0L }

    val categories by vm.categories.collectAsStateWithLifecycle()

    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(dishIdArg) { vm.load(dishIdArg) }

    LaunchedEffect(vm.pickError) {
        vm.pickError?.let { msg ->
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
            vm.clearPickError()
        }
    }

    // 相册选择
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { vm.onPickImage(it) } }

    // 拍照：输出到 FileProvider Uri
    var cameraOutput by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok: Boolean -> if (ok) cameraOutput?.let { vm.onPickImage(it) } }

    val launchCamera = {
        val dir = File(context.cacheDir, "camera").apply { mkdirs() }
        val file = File(dir, "cam_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        cameraOutput = uri
        cameraLauncher.launch(uri)
    }

    if (!vm.loaded) {
        com.family.menu.ui.components.LoadingIndicator(Modifier.fillMaxSize())
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = if (dishIdArg == null) "上传菜品" else "编辑菜品", onBack = { navController.popBackStack() }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // —— 方形图片选择区 ——
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { galleryLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (vm.imagePath.isNotBlank()) {
                    LocalImage(path = vm.imagePath, modifier = Modifier.fillMaxSize(), corner = 18.dp)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "点击选择菜品图片（方形裁剪）",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 图片来源按钮
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("从相册选")
                }
                OutlinedButton(onClick = { launchCamera() }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("拍照")
                }
            }

            // —— 名称 ——
            OutlinedTextField(
                value = vm.name,
                onValueChange = vm::updateName,
                label = { Text("菜品名称（最多 20 字）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // —— 分类（可下拉选择或手输新分类） ——
            ExposedDropdownMenuBox(
                expanded = menuExpanded,
                onExpandedChange = { menuExpanded = it }
            ) {
                OutlinedTextField(
                    value = vm.category,
                    onValueChange = vm::updateCategory,
                    label = { Text("分类") },
                    singleLine = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = {
                                vm.updateCategory(cat.name)
                                menuExpanded = false
                            }
                        )
                    }
                }
            }

            // —— 价格 ——
            OutlinedTextField(
                value = vm.priceText,
                onValueChange = vm::updatePriceText,
                label = { Text("价格（选填）") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            // —— 描述 ——
            OutlinedTextField(
                value = vm.desc,
                onValueChange = vm::updateDesc,
                label = { Text("菜品说明（选填，最多 100 字）") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            // —— 食材（用于生成买菜清单）——
            OutlinedTextField(
                value = vm.ingredients,
                onValueChange = vm::updateIngredients,
                label = { Text("食材清单（选填）") },
                placeholder = { Text("如：西红柿,鸡蛋,葱  —— 逗号/顿号分隔") },
                minLines = 1,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            // —— 标签（多选，便于筛选）——
            Text(
                "标签（选填，可选多个）",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (vm.selectedTags.isNotEmpty()) {
                Text(
                    "已选：${vm.selectedTags.joinToString("、")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            TagOptionGroups.groups.forEach { (group, options) ->
                Text(
                    group,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(options, key = { it }) { option ->
                        val selected = option in vm.selectedTags
                        FilterChip(
                            selected = selected,
                            onClick = { vm.toggleTag(option) },
                            label = { Text(option) }
                        )
                    }
                }
            }

            // —— 上架开关 ——
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("上架（首页可见）", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(
                    checked = vm.status == DishEntity.STATUS_ON,
                    onCheckedChange = { vm.updateStatus(if (it) DishEntity.STATUS_ON else DishEntity.STATUS_OFF) }
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val err = vm.validate()
                    if (err != null) {
                        android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    scope.launch { vm.save { navController.popBackStack() } }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(27.dp)
            ) {
                Text("保存菜品", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}