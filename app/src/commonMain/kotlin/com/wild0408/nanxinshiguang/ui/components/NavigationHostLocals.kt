package com.wild0408.nanxinshiguang.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/** Host-owned insets for platform-specific navigation shells. */
val LocalNavigationHostPadding = staticCompositionLocalOf { PaddingValues(0.dp) }

/** Whether a common page is currently responsible for drawing its own navigation. */
val LocalNavigationHostEnabled = staticCompositionLocalOf { true }
