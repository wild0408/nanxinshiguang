package com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.HyperLiquidTopBarButton
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.SharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.rememberSharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.utils.overScrollVertical
import com.wild0408.nanxinshiguang.ui.components.LocalNavigationHostPadding
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Hyper page contract used by every Android Miuix screen.
 *
 * Official Miuix overlays belong in [miuixOverlayContent]. Hyper full-window
 * effects belong in [customOverlay]. Keeping these hosts separate prevents
 * dialogs and dropdowns from losing the Miuix popup locals.
 */
@Composable
internal fun HyperPageScaffold(
    title: String,
    modifier: Modifier = Modifier,
    largeTitle: String = title,
    showLargeTitle: Boolean = true,
    titleModifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    scrollBehavior: SharedScrollBehavior = rememberSharedScrollBehavior(),
    startAction: @Composable ((Backdrop, Float, Float) -> Unit)? = null,
    endAction: @Composable ((Backdrop, Float, Float) -> Unit)? = null,
    supplementaryContent: @Composable BoxScope.(Dp) -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    miuixOverlayContent: @Composable () -> Unit = {},
    customOverlay: @Composable BoxScope.(Backdrop) -> Unit = {},
    content: @Composable (PaddingValues, SharedScrollBehavior, Backdrop) -> Unit,
) {
    val background = MiuixTheme.colorScheme.surface
    val hostPadding = LocalNavigationHostPadding.current
    val layoutDirection = LocalLayoutDirection.current
    val backdrop = rememberLayerBackdrop {
        drawRect(background)
        drawContent()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = background,
            topBar = {
                HyperGlassTopBar(
                    title = title,
                    largeTitle = largeTitle,
                    showLargeTitle = showLargeTitle,
                    titleModifier = titleModifier,
                    backdrop = backdrop,
                    scrollBehavior = scrollBehavior,
                    startAction = startAction?.let { action ->
                        { backdropAlpha, shadowAlpha -> action(backdrop, backdropAlpha, shadowAlpha) }
                    } ?: onBack?.let { action ->
                        { backdropAlpha, shadowAlpha ->
                            HyperLiquidTopBarButton(
                                onClick = action,
                                backdrop = backdrop,
                                icon = MiuixIcons.ChevronBackward,
                                contentDescription = "返回",
                                backdropAlpha = backdropAlpha,
                                shadowAlpha = shadowAlpha,
                            )
                        }
                    },
                    endAction = endAction?.let { action ->
                        { backdropAlpha, shadowAlpha ->
                            action(backdrop, backdropAlpha, shadowAlpha)
                        }
                    },
                    supplementaryContent = supplementaryContent,
                )
            },
            bottomBar = bottomBar,
        ) { padding ->
            val mergedPadding = PaddingValues(
                start = padding.calculateLeftPadding(layoutDirection),
                top = padding.calculateTopPadding(),
                end = padding.calculateRightPadding(layoutDirection),
                bottom = padding.calculateBottomPadding() + hostPadding.calculateBottomPadding(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(background)
                    .layerBackdrop(backdrop),
            ) {
                content(mergedPadding, scrollBehavior, backdrop)
            }
            miuixOverlayContent()
        }
        customOverlay(backdrop)
    }
}

/** Applies the framework's overscroll and collapsing-topbar ownership in one order. */
internal fun Modifier.hyperPageScroll(scrollBehavior: SharedScrollBehavior): Modifier =
    overScrollVertical().nestedScroll(scrollBehavior.nestedScrollConnection)
