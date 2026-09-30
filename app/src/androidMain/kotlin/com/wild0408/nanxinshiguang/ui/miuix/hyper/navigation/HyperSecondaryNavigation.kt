package com.wild0408.nanxinshiguang.ui.miuix.hyper.navigation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay

private const val SecondaryPageTransitionDurationMillis = 360
private val SecondaryPageEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

/**
 * Navigation 3 host for ordinary full-screen secondary pages.
 *
 * The caller owns [backStack] and removes its last item from [onBack]. [NavDisplay]
 * connects that operation to system back and predictive-back progress. Screen-level
 * back handlers must not compete with this host.
 */
@Composable
fun <T : Any> HyperSecondaryNavigationHost(
    backStack: List<T>,
    onBack: () -> Unit,
    entryProvider: (T) -> NavEntry<T>,
    modifier: Modifier = Modifier,
    entryDecorators: List<NavEntryDecorator<T>>? = null,
) {
    require(backStack.isNotEmpty()) { "HyperSecondaryNavigationHost requires a root entry" }

    val layoutDirection = LocalLayoutDirection.current
    val defaultDirection = if (layoutDirection == LayoutDirection.Ltr) 1 else -1
    val resolvedDecorators = entryDecorators ?: listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
    )

    NavDisplay(
        backStack = backStack,
        onBack = onBack,
        entryDecorators = resolvedDecorators,
        entryProvider = entryProvider,
        modifier = modifier,
        transitionSpec = { hyperSecondaryForwardTransform(defaultDirection) },
        popTransitionSpec = { hyperSecondaryPopTransform(defaultDirection) },
        predictivePopTransitionSpec = { hyperSecondaryPopTransform(defaultDirection) },
    )
}

private fun hyperSecondaryForwardTransform(direction: Int): ContentTransform {
    val animation = tween<IntOffset>(
        durationMillis = SecondaryPageTransitionDurationMillis,
        easing = SecondaryPageEasing,
    )
    return (
        slideInHorizontally(animationSpec = animation) { width -> width * direction } +
            fadeIn(animationSpec = tween(180))
        ).togetherWith(
        slideOutHorizontally(animationSpec = animation) { width ->
            -(width * 0.12f).toInt() * direction
        } + fadeOut(animationSpec = tween(220)),
    )
}

private fun hyperSecondaryPopTransform(direction: Int): ContentTransform {
    val animation = tween<IntOffset>(
        durationMillis = SecondaryPageTransitionDurationMillis,
        easing = SecondaryPageEasing,
    )
    return (
        slideInHorizontally(animationSpec = animation) { width ->
            -(width * 0.12f).toInt() * direction
        } + fadeIn(animationSpec = tween(220))
        ).togetherWith(
        slideOutHorizontally(animationSpec = animation) { width -> width * direction } +
            fadeOut(animationSpec = tween(180)),
    )
}
