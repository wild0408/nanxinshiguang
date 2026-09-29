package com.wild0408.nanxinshiguang.ui.miuix.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.vibrancy
import com.kyant.capsule.ContinuousRoundedRectangle
import com.wild0408.nanxinshiguang.ui.miuix.hyper.effects.edgelight.edgeLight
import com.wild0408.nanxinshiguang.ui.miuix.hyper.effects.edgelight.rememberDefaultEdgeLight
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

data class MiuixCourseAction(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val enabled: Boolean = true,
    val onClick: () -> Unit,
)

@Composable
fun MiuixCourseActionMenu(
    anchor: Rect,
    backdrop: Backdrop,
    actions: List<MiuixCourseAction>,
    focusRect: Rect? = null,
    focusContent: (@Composable () -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    BackHandler(onBack = onDismiss)
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            1f,
            animationSpec = spring(
                dampingRatio = 0.78f,
                stiffness = 420f,
                visibilityThreshold = 0.001f,
            ),
        )
    }
    val progress = animationProgress.value
    // Keep the surface translucent so the backdrop blur remains visible while
    // the menu content stays sharp. A full-surface Modifier.blur would blur
    // icons and labels too, so the only blur here is the backdrop effect.
    val menuSurfaceColor = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.58f)
    val focusSurfaceColor = MiuixTheme.colorScheme.surface.copy(alpha = 0.14f)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(40f),
    ) {
        val density = androidx.compose.ui.platform.LocalDensity.current
        val itemWidth = 76.dp
        val menuWidth = itemWidth * actions.size
        val menuHeight = 74.dp
        val gapPx = with(density) { 8.dp.toPx() }
        val horizontalPaddingPx = with(density) { 8.dp.toPx() }
        val menuWidthPx = with(density) { menuWidth.toPx() }
        val menuHeightPx = with(density) { menuHeight.toPx() }
        val maxWidthPx = with(density) { maxWidth.toPx() }
        val maxHeightPx = with(density) { maxHeight.toPx() }
        val x = (anchor.center.x - menuWidthPx / 2f)
            .coerceIn(horizontalPaddingPx, (maxWidthPx - menuWidthPx - horizontalPaddingPx).coerceAtLeast(horizontalPaddingPx))
        val aboveY = anchor.top - menuHeightPx - gapPx
        val y = if (aboveY >= horizontalPaddingPx) aboveY else (anchor.bottom + gapPx).coerceAtMost(maxHeightPx - menuHeightPx - horizontalPaddingPx)

        // Blur the page behind the action menu, but cut a hole around the
        // selected course so the selected block remains crisp and readable.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = progress }
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { ContinuousRoundedRectangle(0.dp) },
                    effects = {
                        vibrancy()
                        blur(18.dp.toPx() * progress)
                    },
                    highlight = null,
                    shadow = null,
                    onDrawSurface = {
                        drawRect(focusSurfaceColor.copy(alpha = focusSurfaceColor.alpha * progress))
                    },
                )
                .clickable(onClick = onDismiss),
        )

        // The backdrop effect is intentionally full-screen. Re-rendering the
        // selected course above it is more reliable than trying to punch an
        // even-odd hole through the backdrop implementation, and keeps the
        // course's exact colors, text, and rounded shape sharp.
        if (focusRect != null && focusContent != null) {
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(focusRect.left.toInt(), focusRect.top.toInt())
                    }
                    .size(
                        with(density) { focusRect.width.toDp() },
                        with(density) { focusRect.height.toDp() },
                    )
                    .graphicsLayer { alpha = progress }
                    .zIndex(1f),
            ) {
                focusContent()
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(x.toInt(), y.toInt()) }
                .width(menuWidth)
                .graphicsLayer {
                    val scale = 0.86f + 0.14f * progress
                    scaleX = scale
                    scaleY = scale
                    alpha = progress
                    translationY = (1f - progress) * 10.dp.toPx()
                }
                .clip(ContinuousRoundedRectangle(24.dp))
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { ContinuousRoundedRectangle(24.dp) },
                    effects = {
                        vibrancy()
                        blur(26.dp.toPx())
                    },
                    highlight = null,
                    shadow = null,
                    onDrawSurface = { drawRect(menuSurfaceColor) },
                )
                .edgeLight(
                    shape = ContinuousRoundedRectangle(24.dp),
                    edgeLight = rememberDefaultEdgeLight(),
                )
                .clickable(enabled = false, onClick = {}),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                actions.forEachIndexed { index, action ->
                    val itemProgress = ((progress - index * 0.08f) / 0.92f).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .width(itemWidth)
                            .graphicsLayer {
                                alpha = itemProgress
                                translationY = (1f - itemProgress) * 8.dp.toPx()
                            }
                            .clip(ContinuousRoundedRectangle(18.dp))
                            .clickable(enabled = action.enabled) {
                                onDismiss()
                                action.onClick()
                            }
                            .padding(horizontal = 3.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        androidx.compose.foundation.layout.Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = action.icon,
                                contentDescription = action.label,
                                tint = if (action.enabled) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantActions.copy(alpha = 0.45f),
                                modifier = Modifier.size(22.dp),
                            )
                            Text(
                                text = action.label,
                                fontSize = 11.sp,
                                color = if (action.enabled) MiuixTheme.colorScheme.onSurface else MiuixTheme.colorScheme.onSurfaceVariantActions.copy(alpha = 0.45f),
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}
