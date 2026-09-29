package com.wild0408.nanxinshiguang.ui.miuix.hyper.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.kyant.capsule.ContinuousRoundedRectangle
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val QuartOutEasing = Easing { fraction ->
    val inverse = 1f - fraction
    1f - inverse * inverse * inverse * inverse
}
private val CubicOutEasing = Easing { fraction ->
    val inverse = 1f - fraction
    1f - inverse * inverse * inverse
}
private val FifthPowerOutEasing = Easing { fraction ->
    val inverse = 1f - fraction
    1f - inverse * inverse * inverse * inverse * inverse
}
private val QuadraticOutEasing = Easing { fraction -> 1f - (1f - fraction) * (1f - fraction) }

/** State shared by card sources and the full-window expansion overlay. */
@Stable
class HyperCardExpansionState<T> internal constructor() {
    var item: T? by mutableStateOf(null)
        private set
    var sourceBounds: Rect by mutableStateOf(Rect.Zero)
        private set
    internal var sourceSnapshot: GraphicsLayer? by mutableStateOf(null)
        private set
    var expanded: Boolean by mutableStateOf(false)
        private set

    fun expand(item: T, sourceBounds: Rect, sourceSnapshot: GraphicsLayer? = null) {
        if (sourceBounds.width <= 0f || sourceBounds.height <= 0f) return
        this.item = item
        this.sourceBounds = sourceBounds
        this.sourceSnapshot = sourceSnapshot
        expanded = true
    }

    fun collapse() {
        expanded = false
    }

    internal fun finishCollapse() {
        if (!expanded) {
            item = null
            sourceSnapshot = null
            sourceBounds = Rect.Zero
        }
    }
}

@Composable
fun <T> rememberHyperCardExpansionState(): HyperCardExpansionState<T> =
    remember { HyperCardExpansionState() }

private data class ExpansionTransform(
    val scrimAlpha: Float,
    val snapshotAlpha: Float,
    val contentAlpha: Float,
    val translationX: Float,
    val translationY: Float,
    val scale: Float,
    val clipBottom: Float,
    val progress: Float,
)

private class ExpansionClipShape(
    private val screenWidth: Float,
    private val screenCornerRadiusPx: Float,
    private val sourceCornerRadiusPx: Float,
    private val transform: androidx.compose.runtime.State<ExpansionTransform>,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val value = transform.value
        val radiusPx = when {
            value.progress >= 1f -> 0f
            value.progress <= 0.7f -> sourceCornerRadiusPx +
                (screenCornerRadiusPx - sourceCornerRadiusPx) * (value.progress / 0.7f)
            else -> screenCornerRadiusPx
        }
        val radius = (radiusPx / value.scale.coerceAtLeast(0.01f) / density.density).dp
        return ContinuousRoundedRectangle(radius).createOutline(
            size = Size(screenWidth, value.clipBottom),
            layoutDirection = layoutDirection,
            density = density,
        )
    }
}

/**
 * Morphs a card captured in root coordinates into a full-window detail surface.
 * The optional source [GraphicsLayer] is cross-faded during the first part of the motion.
 */
@Composable
fun <T> BoxScope.HyperCardExpansionOverlay(
    state: HyperCardExpansionState<T>,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    sourceCornerRadius: androidx.compose.ui.unit.Dp = 24.dp,
    screenCornerRadius: androidx.compose.ui.unit.Dp = 32.dp,
    content: @Composable BoxScope.(item: T, onCollapse: () -> Unit) -> Unit,
) {
    val item = state.item ?: return
    val progress = remember(state) { Animatable(0f) }
    val translationProgress = remember(state) { Animatable(0f) }

    BackHandler(enabled = state.expanded) { state.collapse() }

    androidx.compose.runtime.LaunchedEffect(state.expanded, item) {
        if (state.expanded) {
            progress.snapTo(0f)
            translationProgress.snapTo(0f)
            delay(12)
            coroutineScope {
                launch { progress.animateTo(1f, tween(560, easing = QuartOutEasing)) }
                launch { translationProgress.animateTo(1f, tween(500, easing = FifthPowerOutEasing)) }
            }
        } else {
            coroutineScope {
                launch { progress.animateTo(0f, tween(350, easing = CubicOutEasing)) }
                launch { translationProgress.animateTo(0f, tween(320, easing = QuadraticOutEasing)) }
            }
            state.finishCollapse()
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .zIndex(20f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {},
    ) {
        val screenWidth = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        val screenHeight = constraints.maxHeight.toFloat().coerceAtLeast(1f)
        val density = LocalDensity.current
        val source = state.sourceBounds
        val transform = remember(source, screenWidth, screenHeight) {
            derivedStateOf {
                val morph = progress.value
                val translation = translationProgress.value
                val startScale = (source.width / screenWidth).coerceIn(0.01f, 1f)
                val scale = startScale + (1f - startScale) * morph
                val sourceCenterY = source.top + source.height / 2f
                val targetCenterY = sourceCenterY + (screenHeight / 2f - sourceCenterY) * translation
                val translationY = targetCenterY -
                    screenHeight / 2f * (1f - scale) -
                    (source.height + (screenHeight - source.height) * morph) / 2f
                val translationX = source.left * (1f - morph) -
                    screenWidth / 2f * (1f - scale)
                val rawClipBottom = source.height + (screenHeight - source.height) * morph
                ExpansionTransform(
                    scrimAlpha = (morph * 0.42f).coerceIn(0f, 0.42f),
                    snapshotAlpha = (1f - morph * 3f).coerceIn(0f, 1f),
                    contentAlpha = ((morph - 0.1f) / 0.5f).coerceIn(0f, 1f),
                    translationX = translationX,
                    translationY = translationY,
                    scale = scale,
                    clipBottom = rawClipBottom / scale,
                    progress = morph,
                )
            }
        }
        val value = transform.value
        val clipShape = remember(source, screenWidth, density) {
            ExpansionClipShape(
                screenWidth = screenWidth,
                screenCornerRadiusPx = with(density) { screenCornerRadius.toPx() },
                sourceCornerRadiusPx = with(density) { sourceCornerRadius.toPx() },
                transform = transform,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = value.scrimAlpha)),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    transformOrigin = TransformOrigin.Center
                    scaleX = value.scale
                    scaleY = value.scale
                    translationX = value.translationX
                    translationY = value.translationY
                }
                .clip(clipShape)
                .background(backgroundColor),
        ) {
            val snapshot = state.sourceSnapshot
            if (snapshot != null && value.snapshotAlpha > 0f) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = value.snapshotAlpha },
                ) {
                    val snapshotScale = screenWidth / source.width.coerceAtLeast(1f)
                    withTransform({ scale(snapshotScale, snapshotScale, Offset.Zero) }) {
                        drawLayer(snapshot)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = value.contentAlpha },
            ) {
                content(item) { state.collapse() }
            }
        }
    }
}
