package com.wild0408.nanxinshiguang.ui.material.settings.style

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.wild0408.nanxinshiguang.ui.viewmodel.schedule.WeeklyScheduleUiState
import com.wild0408.nanxinshiguang.ui.material.schedule.components.ScheduleGridStyleComposed

/** Preview implementation follows the active platform's schedule renderer. */
@Composable
expect fun MiuixStylePreview(
    style: ScheduleGridStyleComposed,
    demoUiState: WeeklyScheduleUiState,
    modifier: Modifier = Modifier,
)
