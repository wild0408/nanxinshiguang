package com.wild0408.nanxinshiguang.ui.miuix.hyper.basic

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.wild0408.nanxinshiguang.ui.miuix.hyper.effects.liquidglass.InteractiveHighlight
import com.wild0408.nanxinshiguang.ui.miuix.hyper.utils.isAppDarkTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import top.yukonga.miuix.kmp.basic.Icon

private val MinimumTopBarButtonTouchTarget = 48.dp

/**
 * Circular liquid-glass action intended for a collapsing top app bar.
 * [backdropAlpha] and [shadowAlpha] should come from [CollapsibleTopAppBar]'s action slot.
 */
@Composable
fun HyperLiquidTopBarButton(
    onClick: () -> Unit,
    backdrop: Backdrop,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    iconOffset: DpOffset = DpOffset.Zero,
    buttonHeight: Dp = 42.dp,
    backdropAlpha: Float = 1f,
    shadowAlpha: Float = 1f,
    iconTint: Color = Color.Unspecified,
    containerColor: Color = Color.Unspecified,
    draggable: Boolean = false,
) {
    val animationScope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    val isLightTheme = !isAppDarkTheme()
    val resolvedContainerColor = when {
        containerColor != Color.Unspecified -> containerColor
        isLightTheme -> Color.White.copy(alpha = 0.76f)
        else -> Color(0xFF242424).copy(alpha = 0.84f)
    }
    val interactiveHighlight = remember(animationScope) {
        InteractiveHighlight(animationScope = animationScope)
    }
    val interactionSource = remember { MutableInteractionSource() }

    val touchTargetSize = maxOf(buttonHeight, MinimumTopBarButtonTouchTarget)

    Box(
        modifier = modifier
            .wrapContentSize()
            .size(touchTargetSize)
            .semantics { this.contentDescription = contentDescription }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
                    onClick()
                },
            )
            .then(
                if (draggable) interactiveHighlight.gestureModifier
                else interactiveHighlight.pressOnlyModifier,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
                modifier = Modifier
                    .size(buttonHeight)
                // Keep the 48dp touch target outside, but clip every visual/press layer.
                .clip(CircleShape)
                .then(interactiveHighlight.modifier)
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { CircleShape },
                    effects = {
                        vibrancy()
                        blur(4.dp.toPx())
                        lens(8.dp.toPx(), 24.dp.toPx())
                    },
                    highlight = null,
                    shadow = null,
                    layerBlock = {
                        val progress = interactiveHighlight.pressProgress
                        val scale = 1f + 2.dp.toPx() / buttonHeight.toPx() * progress
                        scaleX = scale
                        scaleY = scale
                        val offset = interactiveHighlight.offset
                        translationX = size.minDimension * 0.05f * offset.x / size.maxDimension
                        translationY = size.minDimension * 0.05f * offset.y / size.maxDimension
                        alpha = backdropAlpha
                    },
                    onDrawSurface = {
                        drawRect(resolvedContainerColor)
                        drawRect(Color.Black.copy(alpha = 0.03f * interactiveHighlight.pressProgress))
                    },
                )
                .zIndex(0f),
        )
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(iconSize)
                .offset(iconOffset.x, iconOffset.y)
                .zIndex(1f),
            tint = when {
                iconTint != Color.Unspecified -> iconTint
                isLightTheme -> Color.Black.copy(alpha = 0.85f)
                else -> Color.White.copy(alpha = 0.85f)
            },
        )
    }
}
