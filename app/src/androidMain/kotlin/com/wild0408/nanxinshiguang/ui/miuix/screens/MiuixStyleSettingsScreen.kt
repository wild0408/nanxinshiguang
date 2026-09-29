package com.wild0408.nanxinshiguang.ui.miuix.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wild0408.nanxinshiguang.tool.FileManagerCallbacks
import com.wild0408.nanxinshiguang.tool.rememberFileManager
import com.wild0408.nanxinshiguang.ui.components.AdvancedColorPicker
import com.wild0408.nanxinshiguang.ui.components.ColorPickerConfig
import com.wild0408.nanxinshiguang.ui.components.ImageCropper
import com.wild0408.nanxinshiguang.ui.settings.style.ColorPreviewBox
import com.wild0408.nanxinshiguang.ui.settings.style.MiuixStylePreview
import com.wild0408.nanxinshiguang.ui.settings.style.MiuixStyleSettingsScaffold
import com.wild0408.nanxinshiguang.ui.settings.style.StyleSettingsViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.a11y_back
import nanxinshiguang.generated.resources.arrow_back_24px
import nanxinshiguang.generated.resources.item_personalization
import nanxinshiguang.generated.resources.title_dark_color_pool
import nanxinshiguang.generated.resources.title_light_color_pool
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun MiuixStyleSettingsScreen(
    onBack: () -> Unit,
    viewModel: StyleSettingsViewModel = koinViewModel()
) {
    val styleState by viewModel.styleState.collectAsStateWithLifecycle()
    val demoUiState by viewModel.demoUiState.collectAsStateWithLifecycle()

    val containerSize = LocalWindowInfo.current.containerSize
    val isLandscape = containerSize.width > containerSize.height

    var showColorPicker by remember { mutableStateOf(false) }
    var isDarkTarget by remember { mutableStateOf(false) }
    var selectedColorIndex by remember { mutableIntStateOf(0) }

    var loadedBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var showCropper by remember { mutableStateOf(false) }

    // 1. 对接全局统一的平台资源管理器
    val fileManager = rememberFileManager(
        callbacks = FileManagerCallbacks(
            onImagePicked = { bitmap ->
                if (bitmap != null) {
                    loadedBitmap = bitmap
                    showCropper = true
                }
            }
        )
    )

    // 2. 挂载裁切组件（内部自动判断当前平台是否需要展示裁切 UI 或静默处理）
    if (showCropper && loadedBitmap != null) {
        val screenAspectRatio = if (containerSize.height > 0) {
            containerSize.width.toFloat() / containerSize.height.toFloat()
        } else {
            1f
        }

        ImageCropper(
            imageBitmap = loadedBitmap,
            aspectRatio = screenAspectRatio,
            onCropConfirmed = { bytes ->
                // ImageCropper 已直接输出压缩好的 ByteArray，直接存入 ViewModel
                viewModel.saveCroppedWallpaper(bytes)
                showCropper = false
                loadedBitmap = null
            },
            onDismiss = {
                showCropper = false
                loadedBitmap = null
            }
        )
    }

        val currentStyle = styleState
        if (currentStyle == null) {
            Box(Modifier.fillMaxSize().background(MiuixTheme.colorScheme.surface), contentAlignment = Alignment.Center) {
                top.yukonga.miuix.kmp.basic.CircularProgressIndicator()
            }
            return
        }
        val previewContent = @Composable { modifier: Modifier ->
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MiuixTheme.colorScheme.surfaceContainerHigh),
            ) {
                Box(Modifier.fillMaxWidth()) {
                    MiuixStylePreview(currentStyle, demoUiState)
                }
            }
        }

        MiuixStyleSettingsScaffold(
            title = stringResource(Res.string.item_personalization),
            onBack = onBack,
        ) { paddingValues ->
            val contentModifier = Modifier.padding(paddingValues).fillMaxSize()
            if (isLandscape) {
                Row(contentModifier) {
                    previewContent(
                        Modifier
                            .fillMaxHeight()
                            .weight(0.44f)
                            .padding(start = 12.dp, top = 12.dp, bottom = 12.dp),
                    )
                    Column(
                        Modifier
                            .fillMaxHeight()
                            .weight(0.56f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        MiuixSettingsListContent(
                            currentStyle,
                            viewModel,
                            { fileManager.pickImage() },
                            modifier = Modifier.fillMaxWidth().padding(end = 12.dp),
                        ) { dark, index ->
                            isDarkTarget = dark
                            selectedColorIndex = index
                            showColorPicker = true
                        }
                    }
                }
            } else {
                Column(contentModifier) {
                    previewContent(
                        Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                    )
                    Box(Modifier.fillMaxWidth().weight(1f)) {
                        MiuixSettingsListContent(
                            currentStyle,
                            viewModel,
                            { fileManager.pickImage() },
                            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        ) { dark, index ->
                            isDarkTarget = dark
                            selectedColorIndex = index
                            showColorPicker = true
                        }
                    }
                }
            }

            if (showColorPicker) {
                val initialColor = currentStyle.courseColorMaps.getOrNull(selectedColorIndex)?.let { if (isDarkTarget) it.dark else it.light } ?: Color.Gray
                top.yukonga.miuix.kmp.overlay.OverlayDialog(
                    title = if (isDarkTarget) stringResource(Res.string.title_dark_color_pool) else stringResource(Res.string.title_light_color_pool),
                    show = true,
                    onDismissRequest = { showColorPicker = false },
                ) {
                    Column(Modifier.fillMaxWidth()) {
                        AdvancedColorPicker(
                            initialColor = initialColor,
                            config = ColorPickerConfig(showAlpha = false),
                            onColorChanged = { viewModel.updatePrimaryColor(selectedColorIndex, it, isDarkTarget) },
                            previewContent = { ColorPreviewBox(initialColor, !isDarkTarget) }
                        )
                    }
                }
            }
        }
}
