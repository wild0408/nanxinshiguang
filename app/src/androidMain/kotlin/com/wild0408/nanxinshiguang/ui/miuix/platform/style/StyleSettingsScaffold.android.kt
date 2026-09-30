package com.wild0408.nanxinshiguang.ui.material.settings.style

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.HyperLiquidTopBarButton
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.rememberSharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperGlassTopBar
import com.wild0408.nanxinshiguang.ui.miuix.hyper.utils.overScrollVertical
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
actual fun MiuixStyleSettingsScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val background = MiuixTheme.colorScheme.surface
    val scrollBehavior = rememberSharedScrollBehavior()
    val backdrop = rememberLayerBackdrop { drawRect(background); drawContent() }
    Scaffold(
        containerColor = background,
        topBar = {
            HyperGlassTopBar(
                title = title,
                backdrop = backdrop,
                scrollBehavior = scrollBehavior,
                startAction = { alpha, shadow ->
                    HyperLiquidTopBarButton(
                        onClick = onBack,
                        backdrop = backdrop,
                        icon = MiuixIcons.ChevronBackward,
                        contentDescription = "返回",
                        backdropAlpha = alpha,
                        shadowAlpha = shadow,
                    )
                },
            )
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(background)
                .layerBackdrop(backdrop)
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
        ) {
            content(padding)
        }
    }
}
