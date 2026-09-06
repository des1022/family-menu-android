package com.family.menu.ui.screen

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.family.menu.FamilyMenuApp
import com.family.menu.ui.components.AppTopBar
import com.family.menu.ui.components.EmptyState
import com.family.menu.ui.components.PrimaryButton
import com.family.menu.ui.components.SecondaryOutlineButton
import com.family.menu.util.PosterGenerator
import com.family.menu.util.dateCNFull
import com.family.menu.viewmodel.PosterViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun PosterScreen(date: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as FamilyMenuApp
    val vm: PosterViewModel = viewModel(key = date) {
        PosterViewModel(date, app.container.recordRepository, app.container.dishRepository)
    }
    val scope = rememberCoroutineScope()

    var poster by remember(date) { mutableStateOf<Bitmap?>(null) }
    var generating by remember(date) { mutableStateOf(true) }
    var busy by remember { mutableStateOf(false) }

    // 标题/文案修改后（防抖 200ms）重新渲染预览
    LaunchedEffect(vm.title, vm.footer, vm.loaded) {
        if (!vm.loaded) return@LaunchedEffect
        if (vm.lines.isEmpty()) {
            generating = false
            poster = null
            return@LaunchedEffect
        }
        generating = true
        poster = withContext(Dispatchers.Default) {
            PosterGenerator.generate(vm.lines, vm.title, vm.footer, dateCNFull(vm.date))
        }
        generating = false
    }

    val currentPoster = poster

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar(title = "生成分享海报", onBack = onBack) },
        bottomBar = {
            if (currentPoster != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryOutlineButton(
                        text = "保存到相册",
                        enabled = !busy,
                        onClick = {
                            busy = true
                            scope.launch(Dispatchers.IO) {
                                val ok = saveToGallery(context, currentPoster)
                                withContext(Dispatchers.Main) {
                                    busy = false
                                    Toast.makeText(
                                        context,
                                        if (ok) "已保存到相册" else "保存失败\n请用「系统分享」保存到相册",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryButton(
                        text = "系统分享",
                        enabled = !busy,
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                val ok = sharePoster(context, currentPoster)
                                withContext(Dispatchers.Main) {
                                    if (!ok) Toast.makeText(context, "分享失败", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 编辑区
            OutlinedTextField(
                value = vm.title,
                onValueChange = vm::updateTitle,
                label = { Text("海报标题") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = vm.footer,
                onValueChange = vm::updateFooter,
                label = { Text("底部文案") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (vm.loaded && vm.lines.isEmpty()) {
                EmptyState("今天还没有点单\n先去首页点几道菜，再来生成海报")
            } else if (currentPoster != null) {
                // 预览
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = currentPoster.asImageBitmap(),
                            contentDescription = "海报预览",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Text(
                    "长按海报或点「系统分享」可发给家人；「保存到相册」需 Android 10+",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (generating) {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

/** Android 10+ 直接写入公共相册（无需权限）；低版本返回 false 引导系统分享保存 */
private fun saveToGallery(context: android.content.Context, bmp: Bitmap): Boolean {
    return try {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "家庭菜单_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/FamilyMenu")
        }
        val resolver = context.contentResolver
        val uri: Uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
        resolver.openOutputStream(uri)?.use { out ->
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out)
        } ?: return false
        true
    } catch (e: Exception) {
        false
    }
}

/** 写 cacheDir/share/ 并调起系统分享面板 */
private fun sharePoster(context: android.content.Context, bmp: Bitmap): Boolean {
    return try {
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(dir, "menu_${System.currentTimeMillis()}.png")
        file.outputStream().use { out -> bmp.compress(Bitmap.CompressFormat.PNG, 100, out) }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, "分享点单海报"))
        true
    } catch (e: Exception) {
        false
    }
}
