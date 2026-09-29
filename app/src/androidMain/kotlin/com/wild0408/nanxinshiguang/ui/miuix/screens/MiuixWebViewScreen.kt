package com.wild0408.nanxinshiguang.ui.miuix.screens

import androidx.compose.runtime.Composable
import com.wild0408.nanxinshiguang.Destination
import com.wild0408.nanxinshiguang.ui.schoolselection.web.WebViewScreen

/**
 * WebView is intentionally kept visually unchanged. The Android Miuix host
 * owns the destination, while the existing cross-platform WebView UI and
 * bridge remain the source of truth for browser/import behaviour.
 */
@Composable
internal fun MiuixWebViewScreen(
    onNavigate: (Destination) -> Unit,
    onBack: () -> Unit,
    initialUrl: String?,
    assetJsPath: String?,
) {
    WebViewScreen(
        onNavigate = onNavigate,
        onBack = onBack,
        initialUrl = initialUrl,
        assetJsPath = assetJsPath,
    )
}
