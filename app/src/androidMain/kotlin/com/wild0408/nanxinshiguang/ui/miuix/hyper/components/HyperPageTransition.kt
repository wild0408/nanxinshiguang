package com.wild0408.nanxinshiguang.ui.miuix.hyper.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlin.math.roundToInt
import top.yukonga.miuix.kmp.anim.folmeSpring

/**
 * Direction-aware page transition for ordered peer destinations.
 *
 * The caller remains the single owner of [targetState]. [order] must return a stable
 * relative position so forward and backward navigation move in opposite directions.
 */
@Composable
fun <T> HyperPageTransition(
    targetState: T,
    order: (T) -> Int,
    modifier: Modifier = Modifier,
    contentKey: (T) -> Any? = { it },
    content: @Composable AnimatedContentScope.(T) -> Unit,
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            hyperPageContentTransform(
                forward = order(targetState) >= order(initialState),
            )
        },
        contentKey = contentKey,
        label = "HyperPageTransition",
        content = content,
    )
}

private fun hyperPageContentTransform(forward: Boolean): ContentTransform {
    val direction = if (forward) 1 else -1
    val enter = slideInHorizontally(
        animationSpec = folmeSpring(damping = 0.88f, response = 0.42f),
        initialOffsetX = { width -> (width * 0.1f * direction).roundToInt() },
    ) + fadeIn(
        animationSpec = folmeSpring(damping = 1f, response = 0.28f),
    ) + scaleIn(
        initialScale = 0.985f,
        animationSpec = folmeSpring(damping = 0.92f, response = 0.4f),
    )
    val exit = slideOutHorizontally(
        animationSpec = folmeSpring(damping = 1f, response = 0.32f),
        targetOffsetX = { width -> (-width * 0.045f * direction).roundToInt() },
    ) + fadeOut(
        animationSpec = tween(durationMillis = 150),
    ) + scaleOut(
        targetScale = 0.992f,
        animationSpec = folmeSpring(damping = 1f, response = 0.3f),
    )
    return enter.togetherWith(exit)
}
