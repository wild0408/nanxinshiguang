package com.wild0408.nanxinshiguang.ui.settings.style

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wild0408.nanxinshiguang.ui.schedule.WeeklyScheduleUiState
import com.wild0408.nanxinshiguang.ui.schedule.components.ScheduleGridStyleComposed

/** Preview implementation follows the active platform's schedule renderer. */
@Composable
expect fun MiuixStylePreview(
    style: ScheduleGridStyleComposed,
    demoUiState: WeeklyScheduleUiState,
    modifier: Modifier = Modifier,
)
