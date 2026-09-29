package com.wild0408.nanxinshiguang.ui.miuix.hyper.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.luminance
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

/**
 * The single theme entry point for Hyper.
 *
 * Miuix 0.9.3 defaults to its light color scheme when [MiuixTheme] is invoked
 * without colors, so the system appearance must be mapped explicitly.
 */
@Composable
fun HyperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) darkColorScheme() else lightColorScheme()
    MiuixTheme(
        colors = colors,
        content = content,
    )
}

/** Framework effects derive their mode from the active Miuix palette. */
@Composable
@ReadOnlyComposable
fun isAppDarkTheme(): Boolean = MiuixTheme.colorScheme.background.luminance() < 0.5f
