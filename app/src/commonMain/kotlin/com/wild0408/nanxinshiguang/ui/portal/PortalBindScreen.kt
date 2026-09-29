package com.wild0408.nanxinshiguang.ui.portal

import androidx.compose.runtime.Composable

@Composable
expect fun PortalBindScreen(
    onBack: () -> Unit,
    onCompleted: () -> Unit,
)
