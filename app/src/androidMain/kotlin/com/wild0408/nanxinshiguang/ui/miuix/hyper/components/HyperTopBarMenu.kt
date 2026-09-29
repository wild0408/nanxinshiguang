package com.wild0408.nanxinshiguang.ui.miuix.hyper.components

import android.graphics.BlurMaskFilter
import android.graphics.Paint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.HyperLiquidTopBarButton
import com.wild0408.nanxinshiguang.ui.miuix.hyper.effects.edgelight.edgeLight
import com.wild0408.nanxinshiguang.ui.miuix.hyper.effects.edgelight.rememberDefaultEdgeLight
import com.wild0408.nanxinshiguang.ui.miuix.hyper.utils.isAppDarkTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.vibrancy
import com.kyant.capsule.ContinuousRoundedRectangle
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

private val MenuShadowPadding = 24.dp

@Immutable
data class HyperTopBarMenuItem(
    val key: String,
    val label: String,
    val icon: ImageVector,
)

/** The action rendered in [com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperGlassTopBar]'s endAction slot. */
@Composable
fun HyperTopBarMenuButton(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    backdrop: Backdrop,
    icon: ImageVector,
    contentDescription: String,
    backdropAlpha: Float,
    shadowAlpha: Float,
    modifier: Modifier = Modifier,
) {
    val buttonFraction = remember { Animatable(0f) }
    LaunchedEffect(expanded) {
        buttonFraction.animateTo(
            targetValue = if (expanded) 1f else 0f,
            animationSpec = if (expanded) {
                tween(340, easing = CubicBezierEasing(0.34f, 1f, 0.3f, 1f))
            } else {
                tween(420, easing = CubicBezierEasing(0.34f, 1.2f, 0.3f, 1f))
            },
        )
    }
    HyperLiquidTopBarButton(
        onClick = { onExpandedChange(!expanded) },
        backdrop = backdrop,
        icon = icon,
        contentDescription = contentDescription,
        backdropAlpha = backdropAlpha,
        shadowAlpha = shadowAlpha,
        modifier = modifier.offset {
            val fraction = buttonFraction.value
            IntOffset(
                x = (-100 * fraction).dp.roundToPx(),
                y = (45 * fraction).dp.roundToPx(),
            )
        },
    )
}

/**
 * Full-window companion for [HyperTopBarMenuButton]. Put this in
 * [com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperAppScaffold]'s overlay slot.
 */
@Composable
fun BoxScope.HyperTopBarMenuOverlay(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    items: List<HyperTopBarMenuItem>,
    onItemSelected: (HyperTopBarMenuItem) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
) {
    require(items.map(HyperTopBarMenuItem::key).distinct().size == items.size) {
        "HyperTopBarMenuOverlay item keys must be unique"
    }
    if (expanded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(9f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { onExpandedChange(false) }
                .clearAndSetSemantics {},
        )
    }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    LiquidGlassTopBarMenu(
        show = expanded,
        backdrop = backdrop,
        onDismiss = { onExpandedChange(false) },
        modifier = modifier
            .align(Alignment.TopEnd)
            .padding(top = (statusBarPadding - 16.dp).coerceAtLeast(0.dp))
            .offset(x = 8.dp)
            .zIndex(10f),
    ) {
        items.forEach { item ->
            LiquidGlassTopBarMenuItem(
                text = item.label,
                icon = item.icon,
                onClick = {
                    onExpandedChange(false)
                    onItemSelected(item)
                },
            )
        }
    }
}

@Composable
private fun LiquidGlassTopBarMenu(
    show: Boolean,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    fraction: Animatable<Float, *> = remember { Animatable(0f) },
    onDismiss: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    if (show && onDismiss != null) BackHandler { onDismiss() }

    val containerColor = MiuixTheme.colorScheme.surfaceContainer.copy(
        alpha = if (isAppDarkTheme()) 0.8f else 0.72f,
    )
    val menuAlpha = remember { Animatable(0f) }
    var contentAlpha by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        var previous = 0f
        snapshotFlow { fraction.value }.collect { current ->
            val entering = current >= previous
            previous = current
            contentAlpha = if (entering) 0.2f + 0.8f * current
            else if (current > 0.5f) 1f else current * 2f
        }
    }

    val shadowAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        var previous = 0f
        var visible = false
        var job: Job? = null
        snapshotFlow { fraction.value }.collect { current ->
            val entering = current >= previous
            previous = current
            val nextVisible = if (entering) current >= 0.78f else current >= 0.99f
            if (nextVisible != visible) {
                visible = nextVisible
                job?.cancel()
                job = launch {
                    if (nextVisible) shadowAlpha.animateTo(1f, tween(200))
                    else if (shadowAlpha.value >= 1f) shadowAlpha.animateTo(0f, tween(50))
                    else shadowAlpha.snapTo(0f)
                }
            }
        }
    }

    val originProgress = remember { Animatable(0f) }
    val cornerRadius = 25.dp
    val clipShape = remember {
        TopBarMenuClipShape(
            fractionProgress = { fraction.value },
            cornerRadius = cornerRadius,
            buttonDiameter = 42.dp,
        )
    }

    LaunchedEffect(show) {
        if (show) {
            launch {
                fraction.animateTo(
                    1f,
                    spring(dampingRatio = 0.78f, stiffness = 240f, visibilityThreshold = 0.0001f),
                )
            }
            launch {
                originProgress.animateTo(
                    1f,
                    spring(dampingRatio = 0.78f, stiffness = 500f, visibilityThreshold = 0.0001f),
                )
            }
            launch { menuAlpha.animateTo(1f, tween(120)) }
        } else {
            val exitEasing = CubicBezierEasing(0f, 0f, 0f, 1f)
            launch {
                fraction.animateTo(
                    0f,
                    spring(dampingRatio = 0.78f, stiffness = 400f, visibilityThreshold = 0.0001f),
                )
            }
            launch { originProgress.animateTo(0f, tween(450, easing = exitEasing)) }
            menuAlpha.animateTo(0f, tween(400))
            fraction.snapTo(0f)
            originProgress.snapTo(0f)
            menuAlpha.snapTo(0f)
        }
    }

    if (menuAlpha.value <= 0f && !show) return

    Box(
        modifier = modifier
            .width(224.dp + MenuShadowPadding * 2)
            .wrapContentHeight()
            .padding(MenuShadowPadding)
            .drawBehind {
                if (shadowAlpha.value <= 0f) return@drawBehind
                val paint = Paint().apply {
                    color = android.graphics.Color.argb(
                        (32 * shadowAlpha.value).toInt().coerceIn(0, 255), 0, 0, 0,
                    )
                    maskFilter = BlurMaskFilter(16f * density, BlurMaskFilter.Blur.NORMAL)
                }
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawRoundRect(
                        0f, 0f, size.width, size.height,
                        cornerRadius.toPx(), cornerRadius.toPx(), paint,
                    )
                }
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    val scale = 0.24f + 0.76f * fraction.value
                    scaleX = scale
                    scaleY = scale
                    alpha = menuAlpha.value
                    transformOrigin = TransformOrigin(
                        pivotFractionX = 1f - 0.5f * originProgress.value,
                        pivotFractionY = 0.5f * originProgress.value,
                    )
                }
                .clip(clipShape)
                .blur((8f * (1f - fraction.value)).dp)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = {
                        val scale = 0.24f + 0.76f * fraction.value.coerceIn(0f, 1f)
                        ContinuousRoundedRectangle(cornerRadius / scale)
                    },
                    effects = {
                        vibrancy()
                        blur(24.dp.toPx())
                    },
                    highlight = null,
                    shadow = null,
                    onDrawSurface = { drawRect(containerColor) },
                )
                .edgeLight(
                    shape = ContinuousRoundedRectangle(cornerRadius),
                    edgeLight = rememberDefaultEdgeLight(),
                ),
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .graphicsLayer { alpha = contentAlpha },
            ) { content() }
        }
    }
}

@Composable
private fun LiquidGlassTopBarMenuItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val textColor = MiuixTheme.colorScheme.onSurface
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(ContinuousRoundedRectangle(17.dp))
            .heightIn(min = 48.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.size(12.dp))
            Text(text = text, fontSize = 15.6.sp, fontWeight = FontWeight.Medium, color = textColor)
        }
    }
}

private class TopBarMenuClipShape(
    private val fractionProgress: () -> Float,
    private val cornerRadius: Dp,
    private val buttonDiameter: Dp,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val fraction = fractionProgress().coerceIn(0f, 1f)
        val scale = 0.24f + 0.76f * fraction
        val buttonPx = with(density) { buttonDiameter.toPx() }
        val visualWidth = buttonPx + (size.width - buttonPx) * fraction
        val visualHeight = buttonPx + (size.height - buttonPx) * fraction
        val clipWidth = (visualWidth / scale).coerceAtMost(size.width)
        val clipHeight = (visualHeight / scale).coerceAtMost(size.height)
        val radius = with(density) { (cornerRadius / scale).toPx() }
        val path = Path().apply {
            addRoundRect(
                RoundRect(
                    left = size.width - clipWidth,
                    top = 0f,
                    right = size.width,
                    bottom = clipHeight,
                    cornerRadius = CornerRadius(radius, radius),
                ),
            )
        }
        return Outline.Generic(path)
    }
}
