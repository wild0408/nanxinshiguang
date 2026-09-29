package com.wild0408.nanxinshiguang.ui.miuix.hyper.basic

internal const val TopBarButtonEffectCollapsedThreshold = 0.45f

/**
 * Keeps top-bar action material visible once a large title is visibly collapsed, even when a
 * short page has no remaining content scroll range. Content scrolling and pull-down overscroll
 * remain valid triggers for long pages and rebound feedback.
 */
internal fun shouldShowTopBarButtonEffect(
    hasLargeTitle: Boolean,
    collapsedFraction: Float,
    contentOffset: Float,
    scrollThresholdPx: Float,
    overscrollOffset: Float,
): Boolean =
    (hasLargeTitle && collapsedFraction >= TopBarButtonEffectCollapsedThreshold) ||
        contentOffset < -scrollThresholdPx ||
        (contentOffset >= 0f && overscrollOffset < 0f)
