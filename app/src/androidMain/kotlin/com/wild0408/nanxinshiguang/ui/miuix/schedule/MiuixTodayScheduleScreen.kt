package com.wild0408.nanxinshiguang.ui.miuix.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wild0408.nanxinshiguang.Destination
import com.wild0408.nanxinshiguang.data.model.ScheduleGridStyle
import com.wild0408.nanxinshiguang.ui.components.LocalNavigationHostPadding
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.SharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.rememberSharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperGlassTopBar
import com.wild0408.nanxinshiguang.ui.miuix.hyper.utils.overScrollVertical
import com.wild0408.nanxinshiguang.ui.viewmodel.today.CourseDisplayModel
import com.wild0408.nanxinshiguang.ui.viewmodel.today.TodayScheduleViewModel
import com.wild0408.nanxinshiguang.ui.viewmodel.today.TodayStatus
import com.wild0408.nanxinshiguang.ui.viewmodel.today.TodayUiState
import com.wild0408.nanxinshiguang.ui.theme.LocalIsDarkTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.stringArrayResource
import org.koin.compose.viewmodel.koinViewModel
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.course_position_prefix
import nanxinshiguang.generated.resources.course_teacher_prefix
import nanxinshiguang.generated.resources.date_format_year_month_day
import nanxinshiguang.generated.resources.nav_today_schedule
import nanxinshiguang.generated.resources.status_semester_ended
import nanxinshiguang.generated.resources.text_no_courses_today
import nanxinshiguang.generated.resources.title_current_week
import nanxinshiguang.generated.resources.title_semester_not_set
import nanxinshiguang.generated.resources.title_vacation
import nanxinshiguang.generated.resources.week_days_full_names
import kotlinx.datetime.number
import kotlinx.datetime.isoDayNumber
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import coil3.compose.AsyncImage

@Composable
internal fun MiuixTodayScheduleScreen(
    onNavigate: (Destination) -> Unit,
    viewModel: TodayScheduleViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val gridStyle by viewModel.gridStyle.collectAsStateWithLifecycle()
    val dark = LocalIsDarkTheme.current
    val background = MiuixTheme.colorScheme.surface
    val hasWallpaper = !gridStyle.backgroundImagePath.isNullOrEmpty()
    val scrollBehavior = rememberSharedScrollBehavior()
    val backdrop = rememberLayerBackdrop {
        drawRect(background)
        drawContent()
    }
    val direction = LocalLayoutDirection.current
    val hostPadding = LocalNavigationHostPadding.current
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = if (hasWallpaper) Color.Transparent else background,
        topBar = {
            HyperGlassTopBar(
                title = stringResource(Res.string.nav_today_schedule),
                backdrop = backdrop,
                scrollBehavior = scrollBehavior,
                tintIntensity = if (hasWallpaper) 0.04f else 0.2f,
                tintColor = if (hasWallpaper) Color.Transparent else background,
            )
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(if (hasWallpaper) Color.Transparent else background)
                .layerBackdrop(backdrop),
        ) {
            if (hasWallpaper) {
                AsyncImage(
                    model = gridStyle.backgroundImagePath,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.TopCenter,
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(background.copy(alpha = 0.58f)),
                )
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
            ) {
            when (val value = state) {
                TodayUiState.Loading -> Box(Modifier.fillMaxSize().padding(padding))
                is TodayUiState.Success -> TodayBody(
                    value,
                    gridStyle,
                    dark,
                    PaddingValues(
                        start = padding.calculateLeftPadding(direction) + 20.dp,
                        top = padding.calculateTopPadding() + 12.dp,
                        end = padding.calculateRightPadding(direction) + 20.dp,
                        bottom = padding.calculateBottomPadding() + hostPadding.calculateBottomPadding() + 20.dp,
                    ),
                    scrollBehavior,
                )
            }
            }
        }
    }
}

@Composable
private fun TodayBody(
    state: TodayUiState.Success,
    gridStyle: ScheduleGridStyle,
    dark: Boolean,
    padding: PaddingValues,
    scrollBehavior: SharedScrollBehavior,
) {
    val weekDays = stringArrayResource(Res.array.week_days_full_names)
    val date = stringResource(
        Res.string.date_format_year_month_day,
        state.today.year.toString(),
        state.today.month.number.toString(),
        state.today.day.toString(),
    )
    val weekDay = weekDays.getOrNull(state.today.dayOfWeek.isoDayNumber - 1).orEmpty()
    val subtitle = when (state.status) {
        TodayStatus.NoSemesterConfig -> stringResource(Res.string.title_semester_not_set)
        TodayStatus.Vacation -> stringResource(Res.string.title_vacation)
        TodayStatus.SemesterEnded -> stringResource(Res.string.status_semester_ended, 1)
        TodayStatus.Normal -> stringResource(Res.string.title_current_week, state.weekIndex.toString())
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().overScrollVertical().nestedScroll(scrollBehavior.nestedScrollConnection),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(
            start = padding.calculateLeftPadding(LocalLayoutDirection.current),
            top = padding.calculateTopPadding(),
            end = padding.calculateRightPadding(LocalLayoutDirection.current),
            bottom = padding.calculateBottomPadding(),
        ),
    ) {
        item {
            Text("$date $weekDay", style = MiuixTheme.textStyles.title3)
            Text(
                subtitle,
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        if (state.courses.isEmpty()) {
            item {
                Box(
                    Modifier.fillParentMaxHeight(0.55f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        stringResource(Res.string.text_no_courses_today),
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        } else {
            items(state.courses) { course -> MiuixTodayCourse(course, gridStyle, dark) }
        }
    }
}

@Composable
private fun MiuixTodayCourse(model: CourseDisplayModel, gridStyle: ScheduleGridStyle, dark: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.width(58.dp), horizontalAlignment = Alignment.End) {
            Text(model.startTime ?: "--:--", style = MiuixTheme.textStyles.title3)
            Text(model.endTime ?: "--:--", style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantActions)
        }
        Spacer(Modifier.width(12.dp))
        Card(Modifier.weight(1f)) {
            Column(Modifier.padding(14.dp)) {
                Text(model.course.name, style = MiuixTheme.textStyles.title3)
                if (model.course.position.isNotBlank()) Text(stringResource(Res.string.course_position_prefix, model.course.position), style = MiuixTheme.textStyles.body2)
                if (model.course.teacher.isNotBlank()) Text(stringResource(Res.string.course_teacher_prefix, model.course.teacher), style = MiuixTheme.textStyles.body2)
            }
        }
    }
}
