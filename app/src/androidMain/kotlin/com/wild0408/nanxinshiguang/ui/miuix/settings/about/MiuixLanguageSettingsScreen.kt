package com.wild0408.nanxinshiguang.ui.miuix.settings.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wild0408.nanxinshiguang.ui.components.LocalNavigationHostPadding
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.HyperLiquidTopBarButton
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.rememberSharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperGlassTopBar
import com.wild0408.nanxinshiguang.ui.miuix.hyper.utils.overScrollVertical
import com.wild0408.nanxinshiguang.ui.material.settings.about.PlatformLocaleManager
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.a11y_back
import nanxinshiguang.generated.resources.item_language_settings
import nanxinshiguang.generated.resources.language_follow_system
import nanxinshiguang.generated.resources.language_names
import nanxinshiguang.generated.resources.language_tags
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.RadioButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward

private data class MiuixLanguageItem(val name: String, val tag: String)

@Composable
internal fun MiuixLanguageSettingsScreen(onBack: () -> Unit) {
    var currentTag by remember { mutableStateOf(PlatformLocaleManager.getCurrentLanguageTag()) }
    val tags = stringArrayResource(Res.array.language_tags)
    val names = stringArrayResource(Res.array.language_names)
    val followSystem = stringResource(Res.string.language_follow_system)
    val languages = remember(tags, names, followSystem) {
        listOf(MiuixLanguageItem(followSystem, "")) + names.zip(tags, ::MiuixLanguageItem)
    }
    val background = top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme.surface
    val scrollBehavior = rememberSharedScrollBehavior()
    val backdrop = rememberLayerBackdrop { drawRect(background); drawContent() }
    val hostPadding = LocalNavigationHostPadding.current
    val direction = LocalLayoutDirection.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = background,
        topBar = {
            HyperGlassTopBar(
                title = stringResource(Res.string.item_language_settings),
                backdrop = backdrop,
                scrollBehavior = scrollBehavior,
                startAction = { a, s -> HyperLiquidTopBarButton(onBack, backdrop, MiuixIcons.ChevronBackward, stringResource(Res.string.a11y_back), backdropAlpha = a, shadowAlpha = s) },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().background(background).layerBackdrop(backdrop)) {
            LazyColumn(
                Modifier.fillMaxSize().overScrollVertical().nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(
                    start = padding.calculateLeftPadding(direction) + 20.dp,
                    top = padding.calculateTopPadding() + 12.dp,
                    end = padding.calculateRightPadding(direction) + 20.dp,
                    bottom = padding.calculateBottomPadding() + hostPadding.calculateBottomPadding() + 20.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(languages, key = { it.tag }) { item ->
                    val selected = if (item.tag.isEmpty()) currentTag.isEmpty() else currentTag.startsWith(item.tag)
                    BasicComponent(
                        modifier = Modifier.fillMaxWidth(),
                        title = item.name,
                        onClick = { if (!selected) { currentTag = item.tag; PlatformLocaleManager.setLanguageTag(item.tag) } },
                        startAction = { RadioButton(selected = selected, onClick = null) },
                    )
                }
            }
        }
    }
}
