package com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome

import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.CollapsibleTopAppBarDefaults
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.ProgressiveBlurTopBar
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.SharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.components.HyperLiquidTab
import com.wild0408.nanxinshiguang.ui.miuix.hyper.components.HyperLiquidTabs
import com.wild0408.nanxinshiguang.ui.miuix.hyper.utils.isAppDarkTheme
import com.kyant.backdrop.Backdrop
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Creates a live Scaffold padding view. The source [PaddingValues] is intentionally delegated
 * instead of sampled during composition, so Miuix Scaffold can update its top-bar height during
 * measurement and Lazy content will consume the new top padding on the same layout pass.
 */
fun hyperScaffoldPadding(
    source: PaddingValues,
    start: Dp = 0.dp,
    top: Dp = 0.dp,
    end: Dp = 0.dp,
    bottom: Dp = 0.dp,
): PaddingValues = object : PaddingValues {
    override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp =
        source.calculateLeftPadding(layoutDirection) + start

    override fun calculateTopPadding(): Dp = source.calculateTopPadding() + top

    override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp =
        source.calculateRightPadding(layoutDirection) + end

    override fun calculateBottomPadding(): Dp = source.calculateBottomPadding() + bottom
}

/** Responsive placement used by the reusable app chrome. */
enum class HyperNavigationLayout {
    Bottom,
    TopRail,
}

/** App-owned destination data. The chrome deliberately knows nothing about routing. */
@Immutable
data class HyperNavigationItem(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String = label,
)

/** Stable dimensions shared by the top and floating navigation surfaces. */
object HyperAppChromeDefaults {
    val CompactBreakpoint = 600.dp
    val FloatingBarHeight = 56.dp
    val FloatingBarBottomSpacing = 28.dp
    val NavigationIconSize = 24.dp
    val NavigationLabelSize = 11.sp
    val TopRailHeight = 48.dp
    val TopRailItemWidth = 80.dp
    val TopRailTopSpacing = 4.dp
    val SupplementaryTopBarHeight = 40.dp

    fun bottomBarWidthFraction(itemCount: Int): Float =
        (itemCount.coerceIn(1, 4) * 0.21f).coerceAtMost(0.84f)
}

@Composable
fun rememberHyperNavigationLayout(): HyperNavigationLayout {
    val density = LocalDensity.current
    val width = with(density) { LocalWindowInfo.current.containerSize.width.toDp() }
    return remember(width) {
        if (width >= HyperAppChromeDefaults.CompactBreakpoint) {
            HyperNavigationLayout.TopRail
        } else {
            HyperNavigationLayout.Bottom
        }
    }
}

/**
 * Ready-to-use page shell for apps that want the complete Hyper chrome contract.
 * The caller still owns the backdrop capture layer, routing, and page state.
 *
 * [miuixOverlayContent] is composed inside Miuix [Scaffold], so official Overlay components
 * can obtain the popup host and dialog CompositionLocals. [customOverlay] is composed outside
 * the Scaffold and is reserved for Hyper full-window layers that do not depend on those locals.
 */
@Composable
fun HyperAppScaffold(
    items: List<HyperNavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    layout: HyperNavigationLayout = rememberHyperNavigationLayout(),
    topBar: @Composable () -> Unit = {},
    miuixOverlayContent: @Composable () -> Unit = {},
    customOverlay: @Composable BoxScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = topBar,
            bottomBar = {
                if (layout == HyperNavigationLayout.Bottom) {
                    HyperFloatingNavigation(
                        items = items,
                        selectedIndex = selectedIndex,
                        onItemSelected = onItemSelected,
                        backdrop = backdrop,
                        layout = layout,
                    )
                }
            },
            content = { paddingValues ->
                content(paddingValues)
                miuixOverlayContent()
            },
        )
        if (layout == HyperNavigationLayout.TopRail) {
            HyperFloatingNavigation(
                items = items,
                selectedIndex = selectedIndex,
                onItemSelected = onItemSelected,
                backdrop = backdrop,
                layout = layout,
            )
        }
        customOverlay()
    }
}

/**
 * Reusable liquid-glass navigation surface.
 *
 * The parent must install [Backdrop] on the content layer before drawing this component.
 * Navigation state stays hoisted so it can be backed by Navigation 3, a pager, or simple state.
 */
@Composable
fun HyperFloatingNavigation(
    items: List<HyperNavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    layout: HyperNavigationLayout = HyperNavigationLayout.Bottom,
) {
    validateHyperNavigation(items.map(HyperNavigationItem::key), selectedIndex)

    val hapticFeedback = LocalHapticFeedback.current
    val selectItem: (Int) -> Unit = { index ->
        hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
        onItemSelected(index)
    }
    when (layout) {
        HyperNavigationLayout.TopRail -> HyperTopNavigationRail(
            items = items,
            selectedIndex = selectedIndex,
            onItemSelected = selectItem,
            backdrop = backdrop,
            modifier = modifier,
        )

        HyperNavigationLayout.Bottom -> HyperBottomNavigationBar(
            items = items,
            selectedIndex = selectedIndex,
            onItemSelected = selectItem,
            backdrop = backdrop,
            modifier = modifier,
        )
    }
}

@Composable
private fun HyperBottomNavigationBar(
    items: List<HyperNavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier,
) {
    val iconTint = MiuixTheme.colorScheme.onSurfaceContainer.copy(alpha = 0.8f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = HyperAppChromeDefaults.FloatingBarBottomSpacing),
        contentAlignment = Alignment.Center,
    ) {
        HyperLiquidTabs(
            selectedTabIndex = selectedIndex,
            onTabSelected = onItemSelected,
            backdrop = backdrop,
            tabsCount = items.size,
            modifier = Modifier
                .fillMaxWidth(HyperAppChromeDefaults.bottomBarWidthFraction(items.size))
                .height(HyperAppChromeDefaults.FloatingBarHeight),
        ) {
            items.forEachIndexed { index, item ->
                HyperLiquidTab(
                    selected = index == selectedIndex,
                    onClick = { onItemSelected(index) },
                ) {
                    Image(
                        modifier = Modifier.size(HyperAppChromeDefaults.NavigationIconSize),
                        imageVector = item.icon,
                        contentDescription = item.contentDescription,
                        colorFilter = ColorFilter.tint(iconTint),
                    )
                    Text(
                        text = item.label,
                        fontSize = HyperAppChromeDefaults.NavigationLabelSize,
                        color = iconTint,
                    )
                }
            }
        }
    }
}

@Composable
private fun HyperTopNavigationRail(
    items: List<HyperNavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier,
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topPadding = if (statusBarPadding > 0.dp) statusBarPadding else 36.dp
    val textColor = if (!isAppDarkTheme()) {
        androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.8f)
    } else {
        androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        HyperLiquidTabs(
            selectedTabIndex = selectedIndex,
            onTabSelected = onItemSelected,
            backdrop = backdrop,
            tabsCount = items.size,
            modifier = Modifier
                .padding(top = topPadding + HyperAppChromeDefaults.TopRailTopSpacing)
                .width(HyperAppChromeDefaults.TopRailItemWidth * items.size.toFloat())
                .height(HyperAppChromeDefaults.TopRailHeight),
            containerHeight = 400.dp,
            highlightHeight = 34.dp,
            selectorHeight = 34.dp,
        ) {
            items.forEachIndexed { index, item ->
                HyperLiquidTab(
                    selected = index == selectedIndex,
                    onClick = { onItemSelected(index) },
                ) {
                    Text(
                        text = item.label,
                        fontSize = 15.sp,
                        color = textColor,
                    )
                }
            }
        }
    }
}

/**
 * Reusable progressive-blur/collapsing top app bar.
 * [supplementaryContent] is useful for tabs, dates, filters, or other pinned secondary rows.
 */
@Composable
fun HyperGlassTopBar(
    title: String,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    titleModifier: Modifier = Modifier,
    titleOnClick: (() -> Unit)? = null,
    titleOnLongClick: (() -> Unit)? = null,
    titleOnClickLabel: String? = null,
    titleOnLongClickLabel: String? = null,
    largeTitle: String = title,
    showLargeTitle: Boolean? = null,
    showSmallTitle: Boolean? = null,
    showShadow: Boolean? = null,
    showGradientOverlay: Boolean = true,
    scrollBehavior: SharedScrollBehavior? = null,
    blurHeight: Dp? = null,
    tintIntensity: Float = 0.2f,
    tintColor: Color = MiuixTheme.colorScheme.surface,
    gradientMaskHeight: Dp = CollapsibleTopAppBarDefaults.CollapsedHeight + 110.dp,
    startAction: @Composable ((backdropAlpha: Float, shadowAlpha: Float) -> Unit)? = null,
    endAction: @Composable ((backdropAlpha: Float, shadowAlpha: Float) -> Unit)? = null,
    onAlphaChanged: (backdropAlpha: Float, shadowAlpha: Float) -> Unit = { _, _ -> },
    supplementaryContent: @Composable BoxScope.(topBarBottom: Dp) -> Unit = {},
) {
    val topBarBottom = with(androidx.compose.ui.platform.LocalDensity.current) {
        (scrollBehavior?.currentHeightPx ?: 0f).toDp()
    }

    val nativeState = scrollBehavior?.nativeState
    val collapsedFraction = nativeState?.collapsedFraction ?: 0f
    val contentOffset = nativeState?.contentOffset ?: 0f
    val actionVisible = showShadow ?: (collapsedFraction >= 0.45f || contentOffset < -10f)
    val actionAlpha = if (actionVisible) 1f else 0f
    LaunchedEffect(actionAlpha) {
        onAlphaChanged(actionAlpha, actionAlpha)
    }
    val measuredModifier = modifier.onSizeChanged { size ->
        scrollBehavior?.currentHeightPx = size.height.toFloat()
    }

    ProgressiveBlurTopBar(
        backdrop = backdrop,
        height = blurHeight ?: Dp.Unspecified,
        tintIntensity = tintIntensity,
        tintColor = tintColor,
    ) {
        val actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {
            endAction?.invoke(actionAlpha, actionAlpha)
        }
        val navigation: @Composable () -> Unit = {
            startAction?.invoke(actionAlpha, actionAlpha)
        }
        if (showLargeTitle == false) {
            Box {
            SmallTopAppBar(
                title = if (titleOnClick != null || titleOnLongClick != null) "" else title,
                color = Color.Transparent,
                modifier = measuredModifier,
                scrollBehavior = scrollBehavior?.nativeBehavior,
                navigationIcon = navigation,
                actions = actions,
            )
                if (titleOnClick != null || titleOnLongClick != null) {
                    InteractiveTopBarTitle(
                        title = title,
                        modifier = titleModifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding(),
                        onClick = titleOnClick,
                        onLongClick = titleOnLongClick,
                        onClickLabel = titleOnClickLabel,
                        onLongClickLabel = titleOnLongClickLabel,
                    )
                }
            }
        } else {
            TopAppBar(
                title = title,
                largeTitle = largeTitle,
                color = Color.Transparent,
                modifier = measuredModifier,
                scrollBehavior = scrollBehavior?.nativeBehavior,
                navigationIcon = navigation,
                actions = actions,
            )
        }
        supplementaryContent(topBarBottom)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InteractiveTopBarTitle(
    title: String,
    modifier: Modifier,
    onClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
    onClickLabel: String?,
    onLongClickLabel: String?,
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(CollapsibleTopAppBarDefaults.CollapsedHeight)
            .padding(horizontal = 112.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            modifier = Modifier.combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick ?: {},
                onLongClick = onLongClick,
                onClickLabel = onClickLabel,
                onLongClickLabel = onLongClickLabel,
            ),
            color = MiuixTheme.colorScheme.onSurface,
            fontSize = MiuixTheme.textStyles.title3.fontSize,
            maxLines = 1,
        )
    }
}
