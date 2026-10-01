package com.wild0408.nanxinshiguang.ui.miuix.service

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wild0408.nanxinshiguang.ui.material.service.bus.BusMapBody
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.rememberSharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.HyperLiquidTopBarButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import org.jetbrains.compose.resources.vectorResource
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.refresh_24px
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun MiuixBusMapScreen(onBack: () -> Unit) {
    var refreshRequest by androidx.compose.runtime.remember { mutableIntStateOf(0) }
    val background = MiuixTheme.colorScheme.surface
    // Keep the backdrop independent from the layer that contains the buttons.
    // Capturing this same layer with drawContent() creates a RenderNode cycle on
    // some HyperOS devices and crashes RenderThread with SIGSEGV.
    val backdrop = rememberLayerBackdrop { drawRect(background) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = background,
    ) { padding ->
        Box(Modifier.fillMaxSize().background(background)) {
            BusMapBody(Modifier.fillMaxSize().padding(padding), onBack, refreshRequest, showStatusCard = false)
            Box(Modifier.statusBarsPadding().fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
                HyperLiquidTopBarButton(onBack, backdrop, MiuixIcons.ChevronBackward, "返回", modifier = Modifier.align(Alignment.TopStart), backdropAlpha = 1f, shadowAlpha = 1f)
                Box(
                    modifier = Modifier.height(48.dp).wrapContentWidth().align(Alignment.TopCenter),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "校园公交",
                        color = MiuixTheme.colorScheme.onSurface,
                        modifier = Modifier.clip(RoundedCornerShape(14.dp))
                            .background(MiuixTheme.colorScheme.surfaceContainer.copy(alpha = .92f))
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                    )
                }
                HyperLiquidTopBarButton({ refreshRequest++ }, backdrop, vectorResource(Res.drawable.refresh_24px), "刷新", modifier = Modifier.align(Alignment.TopEnd), backdropAlpha = 1f, shadowAlpha = 1f)
            }
        }
    }
}
