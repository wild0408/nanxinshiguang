package com.wild0408.nanxinshiguang.ui.material.settings.style

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable

@Composable
expect fun MiuixStyleSettingsScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
)
