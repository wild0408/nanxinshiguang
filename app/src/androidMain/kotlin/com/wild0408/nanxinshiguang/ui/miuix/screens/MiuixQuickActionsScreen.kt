package com.wild0408.nanxinshiguang.ui.miuix.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wild0408.nanxinshiguang.Destination
import com.wild0408.nanxinshiguang.ui.components.LocalNavigationHostPadding
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.HyperLiquidTopBarButton
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.rememberSharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperGlassTopBar
import com.wild0408.nanxinshiguang.ui.miuix.hyper.utils.overScrollVertical
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.chevron_right_24px
import nanxinshiguang.generated.resources.delete_24px
import nanxinshiguang.generated.resources.desc_schedule_tweak
import nanxinshiguang.generated.resources.item_quick_actions
import nanxinshiguang.generated.resources.item_quick_delete
import nanxinshiguang.generated.resources.item_schedule_tweak
import nanxinshiguang.generated.resources.label_quick_action_category_schedule
import nanxinshiguang.generated.resources.quick_delete_subtitle
import nanxinshiguang.generated.resources.swap_horiz_24px
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.theme.MiuixTheme
import nanxinshiguang.generated.resources.a11y_back

@Composable
internal fun MiuixQuickActionsScreen(
    onNavigate: (Destination) -> Unit,
    onBack: () -> Unit,
) {
    val background = MiuixTheme.colorScheme.surface
    val scrollBehavior = rememberSharedScrollBehavior()
    val backdrop = rememberLayerBackdrop { drawRect(background); drawContent() }
    val hostPadding = LocalNavigationHostPadding.current
    val direction = LocalLayoutDirection.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = background,
        topBar = {
            HyperGlassTopBar(
                title = stringResource(Res.string.item_quick_actions),
                backdrop = backdrop,
                scrollBehavior = scrollBehavior,
                startAction = { a, s ->
                    HyperLiquidTopBarButton(
                        onClick = onBack,
                        backdrop = backdrop,
                        icon = MiuixIcons.ChevronBackward,
                        contentDescription = stringResource(Res.string.a11y_back),
                        backdropAlpha = a,
                        shadowAlpha = s,
                    )
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().background(background).layerBackdrop(backdrop)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().overScrollVertical().nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(
                    start = padding.calculateLeftPadding(direction) + 20.dp,
                    top = padding.calculateTopPadding() + 12.dp,
                    end = padding.calculateRightPadding(direction) + 20.dp,
                    bottom = padding.calculateBottomPadding() + hostPadding.calculateBottomPadding() + 20.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    SmallTitle(
                        text = stringResource(Res.string.label_quick_action_category_schedule),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    )
                }
                item {
                    QuickActionComponent(
                        title = stringResource(Res.string.item_schedule_tweak),
                        summary = stringResource(Res.string.desc_schedule_tweak),
                        icon = Res.drawable.swap_horiz_24px,
                        onClick = { onNavigate(Destination.TweakSchedule) },
                    )
                }
                item {
                    QuickActionComponent(
                        title = stringResource(Res.string.item_quick_delete),
                        summary = stringResource(Res.string.quick_delete_subtitle),
                        icon = Res.drawable.delete_24px,
                        onClick = { onNavigate(Destination.QuickDelete) },
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionComponent(
    title: String,
    summary: String,
    icon: DrawableResource,
    onClick: () -> Unit,
) {
    BasicComponent(
        modifier = Modifier.fillMaxWidth(),
        title = title,
        summary = summary,
        onClick = onClick,
        startAction = {
            Icon(
                imageVector = vectorResource(icon),
                contentDescription = null,
                tint = MiuixTheme.colorScheme.primary,
            )
        },
        endActions = {
            Icon(
                imageVector = vectorResource(Res.drawable.chevron_right_24px),
                contentDescription = null,
                tint = MiuixTheme.colorScheme.onSurfaceVariantActions,
            )
        },
    )
}
