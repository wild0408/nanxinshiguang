package com.wild0408.nanxinshiguang.ui.miuix.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wild0408.nanxinshiguang.Destination
import com.wild0408.nanxinshiguang.data.db.main.CourseWithWeeks
import com.wild0408.nanxinshiguang.data.model.schedule_style.ScheduleModeProto
import com.wild0408.nanxinshiguang.ui.miuix.components.MiuixCourseBlock
import com.wild0408.nanxinshiguang.ui.miuix.components.MiuixCourseAction
import com.wild0408.nanxinshiguang.ui.miuix.components.MiuixCourseActionMenu
import com.wild0408.nanxinshiguang.ui.miuix.components.MiuixCourseTablePickerDialog
import com.wild0408.nanxinshiguang.ui.miuix.components.MiuixScheduleGrid
import com.wild0408.nanxinshiguang.ui.miuix.components.MiuixTimeColumn
import com.wild0408.nanxinshiguang.ui.miuix.components.MiuixWeekSelectorDialog
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.HyperLiquidTopBarButton
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.rememberSharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperGlassTopBar
import com.wild0408.nanxinshiguang.ui.miuix.hyper.components.HyperTopBarMenuButton
import com.wild0408.nanxinshiguang.ui.miuix.hyper.components.HyperTopBarMenuItem
import com.wild0408.nanxinshiguang.ui.miuix.hyper.components.HyperTopBarMenuOverlay
import com.wild0408.nanxinshiguang.ui.miuix.hyper.utils.overScrollVertical
import com.wild0408.nanxinshiguang.ui.components.LocalNavigationHostPadding
import com.wild0408.nanxinshiguang.ui.schedule.MergedCourseBlock
import com.wild0408.nanxinshiguang.ui.schedule.WeeklyScheduleViewModel
import com.wild0408.nanxinshiguang.ui.schedule.WeeklyScheduleUiState
import com.wild0408.nanxinshiguang.ui.schedule.PasteResult
import com.wild0408.nanxinshiguang.navigation.AddEditCourseChannel
import com.wild0408.nanxinshiguang.navigation.PresetCourseData
import com.wild0408.nanxinshiguang.ui.schedule.components.FloatingCourseBar
import com.wild0408.nanxinshiguang.ui.schedule.components.ScheduleGridStyleComposed
import com.wild0408.nanxinshiguang.ui.schedule.components.rearrangeDays
import com.wild0408.nanxinshiguang.ui.components.ToastManager
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.action_select_table
import nanxinshiguang.generated.resources.action_adjust_course_time
import nanxinshiguang.generated.resources.action_copy_course
import nanxinshiguang.generated.resources.action_edit_course
import nanxinshiguang.generated.resources.action_new_course
import nanxinshiguang.generated.resources.action_paste_course
import nanxinshiguang.generated.resources.archive_24px
import nanxinshiguang.generated.resources.add_24px
import nanxinshiguang.generated.resources.calendar_today_24px
import nanxinshiguang.generated.resources.class_24px
import nanxinshiguang.generated.resources.double_arrow_24px
import nanxinshiguang.generated.resources.edit_24px
import nanxinshiguang.generated.resources.location_on_24px
import nanxinshiguang.generated.resources.more_vert_24px
import nanxinshiguang.generated.resources.palette_24px
import nanxinshiguang.generated.resources.content_copy_24px
import nanxinshiguang.generated.resources.content_paste_24px
import nanxinshiguang.generated.resources.person_24px
import nanxinshiguang.generated.resources.refresh_24px
import nanxinshiguang.generated.resources.schedule_24px
import nanxinshiguang.generated.resources.sticky_note_2_24px
import nanxinshiguang.generated.resources.title_select_week
import nanxinshiguang.generated.resources.toast_course_adjust_saved
import nanxinshiguang.generated.resources.toast_course_copied
import nanxinshiguang.generated.resources.toast_course_outside_semester_add
import nanxinshiguang.generated.resources.toast_course_outside_semester_adjust
import nanxinshiguang.generated.resources.toast_course_pasted
import nanxinshiguang.generated.resources.toast_course_time_overlap
import nanxinshiguang.generated.resources.toast_invalid_paste_target
import nanxinshiguang.generated.resources.toast_no_course_table
import nanxinshiguang.generated.resources.toast_no_course_to_paste
import nanxinshiguang.generated.resources.toast_paste_failed
import nanxinshiguang.generated.resources.swap_horiz_24px
import nanxinshiguang.generated.resources.week_days_short_names
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Scaffold as MiuixScaffold
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import top.yukonga.miuix.kmp.window.WindowDialog
import kotlin.math.roundToInt
import kotlinx.datetime.number
import kotlinx.datetime.isoDayNumber
import kotlin.time.Clock
import coil3.compose.AsyncImage

private const val MIUIX_PAGER_CENTER = 50
private const val MIUIX_PAGER_PAGE_COUNT = 101

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MiuixWeeklyScheduleScreen(
    onNavigate: (Destination) -> Unit,
    onBack: () -> Unit,
    viewModel: WeeklyScheduleViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val today = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = MIUIX_PAGER_CENTER,
        pageCount = { MIUIX_PAGER_PAGE_COUNT }
    )
    var showTableSwitcher by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showWeekSelector by remember { mutableStateOf(false) }
    var selectedBlock by remember { mutableStateOf<MergedCourseBlock?>(null) }
    var editingBlock by remember { mutableStateOf<MergedCourseBlock?>(null) }
    var copiedCourse by remember { mutableStateOf<CourseWithWeeks?>(null) }
    var actionMenuAnchor by remember { mutableStateOf<Rect?>(null) }
    var actionMenuFocusRect by remember { mutableStateOf<Rect?>(null) }
    var actionMenuFocusCourse by remember { mutableStateOf<CourseWithWeeks?>(null) }
    var actionMenuActions by remember { mutableStateOf<List<MiuixCourseAction>>(emptyList()) }
    val style = remember(uiState.style) {
        with(ScheduleGridStyleComposed) { uiState.style.toComposedStyle() }
    }
    // The schedule grid normalizes the compact default time-column width for
    // the Miuix layout. Keep the weekday header on that exact same metric so
    // each date stays centered over its corresponding day column.
    val effectiveTimeColumnWidth = remember(style.timeColumnWidth) {
        if (style.timeColumnWidth == 40.dp) 36.dp else style.timeColumnWidth
    }
    val addActionIcon = vectorResource(Res.drawable.add_24px)
    val copyActionIcon = vectorResource(Res.drawable.content_copy_24px)
    val pasteActionIcon = vectorResource(Res.drawable.content_paste_24px)
    val editActionIcon = vectorResource(Res.drawable.edit_24px)
    val adjustActionIcon = vectorResource(Res.drawable.schedule_24px)
    val actionNewCourse = stringResource(Res.string.action_new_course)
    val actionCopyCourse = stringResource(Res.string.action_copy_course)
    val actionPasteCourse = stringResource(Res.string.action_paste_course)
    val actionEditCourse = stringResource(Res.string.action_edit_course)
    val actionAdjustCourseTime = stringResource(Res.string.action_adjust_course_time)
    val toastCourseCopied = stringResource(Res.string.toast_course_copied)
    val toastNoCourseToPaste = stringResource(Res.string.toast_no_course_to_paste)
    val toastCoursePasted = stringResource(Res.string.toast_course_pasted)
    val toastCourseTimeOverlap = stringResource(Res.string.toast_course_time_overlap)
    val toastOutsideSemesterAdd = stringResource(Res.string.toast_course_outside_semester_add)
    val toastOutsideSemesterAdjust = stringResource(Res.string.toast_course_outside_semester_adjust)
    val toastCourseAdjustSaved = stringResource(Res.string.toast_course_adjust_saved)
    val toastNoCourseTable = stringResource(Res.string.toast_no_course_table)
    val toastInvalidPasteTarget = stringResource(Res.string.toast_invalid_paste_target)
    val toastPasteFailedTemplate = stringResource(Res.string.toast_paste_failed, "{error}")
    val floatingCourse = uiState.floatingCourse
    val floatingDuration = remember(floatingCourse, style.scheduleMode) {
        val course = floatingCourse?.course
        if (course == null) {
            1f
        } else if (style.scheduleMode == ScheduleModeProto.TIME_24H_MODE) {
            val start = runCatching { LocalTime.parse(course.customStartTime ?: "") }.getOrNull()
            val end = runCatching { LocalTime.parse(course.customEndTime ?: "") }.getOrNull()
            if (start != null && end != null) {
                val rawMinutes = (end.toSecondOfDay() - start.toSecondOfDay()) / 60
                val durationMinutes = if (rawMinutes <= 0) rawMinutes + 24 * 60 else rawMinutes
                (durationMinutes / 60f).coerceAtLeast(0.25f)
            } else {
                1f
            }
        } else {
            val start = course.startSection ?: 1
            val end = course.endSection ?: start
            (end - start + 1).coerceAtLeast(1).toFloat()
        }
    }

    fun dismissCourseMenu() {
        actionMenuAnchor = null
        actionMenuFocusRect = null
        actionMenuFocusCourse = null
        actionMenuActions = emptyList()
    }

    fun showToast(message: String) = ToastManager.show(message)

    fun addCourseAt(day: Int, section: Int) {
        val week = uiState.weekIndexInPager ?: uiState.currentWeekNumber
        if (week == null || week !in 1..uiState.totalWeeks) {
            showToast(toastOutsideSemesterAdd)
            return
        }
        val preset = if (style.scheduleMode == ScheduleModeProto.TIME_24H_MODE) {
            val startHour = section.coerceIn(0, 23)
            PresetCourseData(
                day = day,
                isCustomTime = true,
                customStartTime = "%02d:00".format(startHour),
                customEndTime = if (startHour == 23) "23:59" else "%02d:00".format(startHour + 1),
                presetWeeks = setOf(week),
            )
        } else {
            PresetCourseData(day = day, startSection = section, endSection = section, presetWeeks = setOf(week))
        }
        AddEditCourseChannel.sendEvent(preset)
        onNavigate(Destination.AddEditCourse())
    }

    fun pasteCourseAt(source: CourseWithWeeks?, day: Int, section: Float) {
        val week = uiState.weekIndexInPager ?: uiState.currentWeekNumber
        if (source == null) {
            showToast(toastNoCourseToPaste)
            return
        }
        if (week == null || week !in 1..uiState.totalWeeks) {
            showToast(toastOutsideSemesterAdd)
            return
        }
        viewModel.pasteCourseToSlot(source, week, day, section, style.scheduleMode) { result ->
            when (result) {
                is PasteResult.Success -> {
                    showToast(toastCoursePasted)
                    if (result.hasOverlap) showToast(toastCourseTimeOverlap)
                }
                PasteResult.OutsideSemester -> showToast(toastOutsideSemesterAdd)
                PasteResult.NoCourseTable -> showToast(toastNoCourseTable)
                PasteResult.InvalidTarget -> showToast(toastInvalidPasteTarget)
                is PasteResult.Failure -> showToast(toastPasteFailedTemplate.replace("{error}", result.message))
            }
        }
    }

    fun handleBlankTap(day: Int, section: Int) {
        if (floatingCourse == null) {
            addCourseAt(day, section)
            return
        }
        val targetWeek = uiState.weekIndexInPager ?: uiState.currentWeekNumber
        if (targetWeek == null || targetWeek !in 1..uiState.totalWeeks) {
            viewModel.exitFloatingMode()
            showToast(toastOutsideSemesterAdjust)
            return
        }
        val start = section.toFloat()
        val end = if (style.scheduleMode == ScheduleModeProto.TIME_24H_MODE) {
            (start + floatingDuration).coerceAtMost(24f)
        } else {
            (start + floatingDuration - 1f).coerceAtMost(uiState.timeSlots.size.toFloat())
        }
        viewModel.updateCourseTimeByFloatingGesture(targetWeek, day, start, end) {
            showToast(toastCourseAdjustSaved)
        }
    }

    fun showBlankMenu(day: Int, section: Int, anchor: Rect) {
        if (floatingCourse != null) return
        actionMenuAnchor = anchor
        actionMenuFocusRect = null
        actionMenuFocusCourse = null
        actionMenuActions = listOf(
            MiuixCourseAction("add", actionNewCourse, addActionIcon) { addCourseAt(day, section); dismissCourseMenu() },
            MiuixCourseAction("paste", actionPasteCourse, pasteActionIcon, copiedCourse != null) { pasteCourseAt(copiedCourse, day, section.toFloat()); dismissCourseMenu() },
        )
    }

    fun showCourseMenu(block: MergedCourseBlock, anchor: Rect) {
        val source = block.courses.firstOrNull()
        val targetSection = if (style.scheduleMode == ScheduleModeProto.TIME_24H_MODE) block.startSection else block.startSection + 1f
        actionMenuAnchor = anchor
        actionMenuFocusRect = anchor
        actionMenuFocusCourse = source
        actionMenuActions = listOf(
            MiuixCourseAction("copy", actionCopyCourse, copyActionIcon) { copiedCourse = source; showToast(toastCourseCopied); dismissCourseMenu() },
            MiuixCourseAction("paste", actionPasteCourse, pasteActionIcon, copiedCourse != null) { pasteCourseAt(copiedCourse, block.day, targetSection); dismissCourseMenu() },
            MiuixCourseAction("edit", actionEditCourse, editActionIcon) { dismissCourseMenu(); source?.let { onNavigate(Destination.AddEditCourse(it.course.id)) } },
            MiuixCourseAction("adjust", actionAdjustCourseTime, adjustActionIcon) { dismissCourseMenu(); editingBlock = block },
        )
    }

    fun weekStart(date: LocalDate, firstDay: Int): LocalDate {
        val target = DayOfWeek(firstDay.coerceIn(1, 7)).isoDayNumber
        val diff = (date.dayOfWeek.isoDayNumber - target + 7) % 7
        return date.plus(-diff, DateTimeUnit.DAY)
    }

    LaunchedEffect(pagerState, uiState.firstDayOfWeek) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                val offset = (page - MIUIX_PAGER_CENTER).toLong()
                viewModel.updatePagerDate(weekStart(today, uiState.firstDayOfWeek).plus(offset * 7, DateTimeUnit.DAY))
            }
    }

    val displayTitle = when {
        !uiState.isSemesterSet || uiState.semesterStartDate == null -> "学期未开始"
        uiState.daysUntilStart > 0 -> "还有 ${uiState.daysUntilStart} 天开学"
        uiState.weekIndexInPager != null && uiState.weekIndexInPager!! in 1..uiState.totalWeeks -> "第${uiState.weekIndexInPager}周"
        else -> "假期中"
    }
    val isViewingCurrentWeek = uiState.weekIndexInPager == uiState.currentWeekNumber
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    // Miuix Scaffold applies the system-bar/cutout insets to its body content.
    // The header lives in the top-bar slot, so apply the same horizontal
    // insets explicitly or it drifts from the grid on landscape devices.
    val scheduleContentInsets = WindowInsets.systemBars
        .union(WindowInsets.displayCutout)
        .asPaddingValues()
    val topMenuItems = listOf(
        HyperTopBarMenuItem("week", "跳转周数", vectorResource(Res.drawable.double_arrow_24px)),
        HyperTopBarMenuItem("courses", "课程管理", vectorResource(Res.drawable.archive_24px)),
        HyperTopBarMenuItem("style", "课表外观", vectorResource(Res.drawable.palette_24px)),
    )
    val background = MiuixTheme.colorScheme.surface
    val hasWallpaper = style.backgroundImagePath.isNotEmpty()
    val scrollBehavior = rememberSharedScrollBehavior()
    val backdrop = rememberLayerBackdrop {
        drawRect(background)
        drawContent()
    }
    val hostPadding = LocalNavigationHostPadding.current
    val layoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current

    Box(Modifier.fillMaxSize()) {
        MiuixScaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = if (style.backgroundImagePath.isNotEmpty()) Color.Transparent else background,
            topBar = {
                HyperGlassTopBar(
                    title = displayTitle,
                    showLargeTitle = false,
                    titleOnClick = {
                        if (uiState.isSemesterSet) showWeekSelector = true else onNavigate(Destination.Settings)
                    },
                    titleOnLongClick = {
                        if (uiState.isSemesterSet) showWeekSelector = true else onNavigate(Destination.Settings)
                    },
                    titleOnClickLabel = stringResource(Res.string.title_select_week),
                    titleOnLongClickLabel = stringResource(Res.string.title_select_week),
                    backdrop = backdrop,
                    scrollBehavior = scrollBehavior,
                    tintIntensity = if (hasWallpaper) 0.04f else 0.2f,
                    tintColor = if (hasWallpaper) Color.Transparent else background,
                    startAction = if (!isViewingCurrentWeek) {
                        { backdropAlpha, shadowAlpha ->
                    HyperLiquidTopBarButton(
                        onClick = { scope.launch { pagerState.animateScrollToPage(MIUIX_PAGER_CENTER) } },
                        backdrop = backdrop,
                        icon = vectorResource(Res.drawable.refresh_24px),
                        contentDescription = "返回本周",
                        backdropAlpha = backdropAlpha,
                        shadowAlpha = shadowAlpha,
                    )
                        }
                    } else null,
                    endAction = { backdropAlpha, shadowAlpha ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            HyperLiquidTopBarButton(
                                onClick = { showTableSwitcher = true },
                                backdrop = backdrop,
                                icon = vectorResource(Res.drawable.swap_horiz_24px),
                                contentDescription = stringResource(Res.string.action_select_table),
                                backdropAlpha = backdropAlpha,
                                shadowAlpha = shadowAlpha,
                            )
                            HyperTopBarMenuButton(
                                expanded = showMoreMenu,
                                onExpandedChange = { showMoreMenu = it },
                                backdrop = backdrop,
                                icon = vectorResource(Res.drawable.more_vert_24px),
                                contentDescription = "更多",
                                backdropAlpha = backdropAlpha,
                                shadowAlpha = shadowAlpha,
                            )
                        }
                    },
                    supplementaryContent = { topBarBottom ->
                        WeekdayHeader(
                            weekDates = uiState.pagerMondayDate,
                            firstDayOfWeek = uiState.firstDayOfWeek,
                            showWeekends = uiState.showWeekends,
                            timeColumnWidth = effectiveTimeColumnWidth,
                            horizontalInsets = scheduleContentInsets,
                            layoutDirection = layoutDirection,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = topBarBottom),
                        )
                    },
                )
            },
        ) { scaffoldPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (style.backgroundImagePath.isNotEmpty()) Color.Transparent else background)
                    .layerBackdrop(backdrop),
            ) {
                if (hasWallpaper) {
                    AsyncImage(
                        model = style.backgroundImagePath,
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
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    // A page is a fully measured course grid. Let Pager compose a
                    // neighbour when a swipe begins instead of during page entry.
                    beyondViewportPageCount = 0,
                ) { page ->
                    val pageDate = remember(page, uiState.firstDayOfWeek) {
                        weekStart(today, uiState.firstDayOfWeek).plus((page - MIUIX_PAGER_CENTER).toLong() * 7, DateTimeUnit.DAY)
                    }
                    val courses = uiState.courseCache[pageDate.toString()].orEmpty()
                    val scrollState = remember(page) { androidx.compose.foundation.ScrollState(0) }
                    var viewportHeightPx by remember(page) { mutableIntStateOf(0) }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .onSizeChanged { viewportHeightPx = it.height }
                            .overScrollVertical()
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                            .verticalScroll(scrollState)
                            .padding(
                                start = scaffoldPadding.calculateLeftPadding(layoutDirection),
                                // The weekday labels sit on the center line of their header;
                                // bring the grid slightly underneath that header so the first
                                // schedule row does not look detached from the dates.
                                top = scaffoldPadding.calculateTopPadding(),
                                end = scaffoldPadding.calculateRightPadding(layoutDirection),
                                bottom = scaffoldPadding.calculateBottomPadding() +
                                    hostPadding.calculateBottomPadding(),
                            ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        MiuixScheduleGrid(
                            style = style,
                            viewState = uiState,
                            courses = courses,
                            scrollState = scrollState,
                            viewportHeightPx = viewportHeightPx.toFloat(),
                            editingBlock = editingBlock,
                            onEditingChanged = { if (!it) editingBlock = null },
                            onCourseClick = { selectedBlock = it },
                            onCourseLongPress = ::showCourseMenu,
                            onBlankTap = ::handleBlankTap,
                            onBlankLongPress = ::showBlankMenu,
                            onTimeSlotClick = { onNavigate(Destination.TimeScheduleManagement) },
                            onCourseMoved = { block, day, start, end ->
                                val currentWeek = uiState.weekIndexInPager ?: uiState.currentWeekNumber
                                val courseId = block.courses.firstOrNull()?.course?.id
                                if (currentWeek != null && courseId != null && currentWeek in 1..uiState.totalWeeks) {
                                    viewModel.updateCourseTimeByGesture(courseId, day, start, end) { showToast(toastCourseAdjustSaved) }
                                } else showToast(toastOutsideSemesterAdjust)
                            },
                            onCourseTimeAdjusted = { block, start, end ->
                                val currentWeek = uiState.weekIndexInPager ?: uiState.currentWeekNumber
                                val courseId = block.courses.firstOrNull()?.course?.id
                                if (currentWeek != null && courseId != null && currentWeek in 1..uiState.totalWeeks) {
                                    viewModel.updateCourseTimeByGesture(courseId, block.day, start, end) { showToast(toastCourseAdjustSaved) }
                                } else showToast(toastOutsideSemesterAdjust)
                            },
                            onInitiateFloatingMode = { block ->
                                val source = block.courses.firstOrNull()
                                val currentWeek = uiState.weekIndexInPager ?: uiState.currentWeekNumber
                                if (source != null && currentWeek != null && currentWeek in 1..uiState.totalWeeks) {
                                    viewModel.enterFloatingMode(source, currentWeek)
                                } else {
                                    showToast(toastOutsideSemesterAdjust)
                                }
                            },
                        )
                    }
                }

                if (showTableSwitcher) {
                    MiuixCourseTablePickerDialog(
                        title = stringResource(Res.string.action_select_table),
                        onDismissRequest = { showTableSwitcher = false },
                        onTableSelected = { table ->
                            viewModel.switchCourseTable(table.id)
                            showTableSwitcher = false
                        },
                    )
                }
                if (showWeekSelector) {
                    MiuixWeekSelectorDialog(
                        totalWeeks = uiState.totalWeeks,
                        currentWeek = uiState.currentWeekNumber ?: 1,
                        selectedWeek = uiState.weekIndexInPager ?: (uiState.currentWeekNumber ?: 1),
                        onWeekSelected = { week ->
                            val selected = uiState.weekIndexInPager ?: 1
                            val target = (pagerState.currentPage + week - selected)
                                .coerceIn(0, MIUIX_PAGER_PAGE_COUNT - 1)
                            scope.launch { pagerState.animateScrollToPage(target) }
                            showWeekSelector = false
                        },
                        onDismissRequest = { showWeekSelector = false },
                    )
                }
                selectedBlock?.let { block ->
                    MiuixCourseDetailDialog(
                        block = block,
                        onDismiss = { selectedBlock = null },
                        onEdit = { id ->
                            selectedBlock = null
                            onNavigate(Destination.AddEditCourse(courseId = id))
                        },
                    )
                }
            }
        }

        HyperTopBarMenuOverlay(
            expanded = showMoreMenu,
            onExpandedChange = { showMoreMenu = it },
            items = topMenuItems,
            onItemSelected = { item ->
                when (item.key) {
                    "week" -> showWeekSelector = true
                    "courses" -> onNavigate(Destination.CourseManagementList)
                    "style" -> onNavigate(Destination.StyleSettings)
                }
            },
            backdrop = backdrop,
        )

        actionMenuAnchor?.let { anchor ->
            MiuixCourseActionMenu(
                anchor = anchor,
                backdrop = backdrop,
                actions = actionMenuActions,
                focusRect = actionMenuFocusRect,
                focusContent = actionMenuFocusCourse?.let { course ->
                    {
                        MiuixCourseBlock(
                            courseWrapper = course,
                            isVisualDemoted = false,
                            style = style,
                            timeSlots = uiState.timeSlots,
                            modifier = Modifier.fillMaxSize(),
                            isFloating = false,
                        )
                    }
                },
                onDismiss = ::dismissCourseMenu,
            )
        }

        if (uiState.floatingCourse != null) {
            FloatingCourseBar(
                floatingCourse = uiState.floatingCourse,
                onCancelClick = viewModel::exitFloatingMode,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp + bottomInset),
            )
        }
    }
}

@Composable
private fun WeekdayHeader(
    weekDates: LocalDate,
    firstDayOfWeek: Int,
    showWeekends: Boolean,
    timeColumnWidth: androidx.compose.ui.unit.Dp,
    horizontalInsets: PaddingValues,
    layoutDirection: LayoutDirection,
    modifier: Modifier = Modifier,
) {
    val days = stringArrayResource(Res.array.week_days_short_names).toList()
    val reorderedDays = rearrangeDays(days, firstDayOfWeek)
    val displayDays = if (showWeekends) reorderedDays else reorderedDays.take(5)
    val dates = (0 until displayDays.size).map { weekDates.plus(it.toLong(), DateTimeUnit.DAY) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .padding(
                start = horizontalInsets.calculateStartPadding(layoutDirection),
                end = horizontalInsets.calculateEndPadding(layoutDirection),
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(Modifier.width(timeColumnWidth))
        displayDays.forEachIndexed { index, day ->
            val date = dates[index]
            val isToday = date == Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(day, style = MiuixTheme.textStyles.footnote1, color = if (isToday) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface)
                Text("%02d/%02d".format(date.month.number, date.day), style = MiuixTheme.textStyles.footnote2, color = if (isToday) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantActions)
            }
        }
    }
}

@Composable
private fun MiuixMenuItem(icon: org.jetbrains.compose.resources.DrawableResource, title: String, onClick: () -> Unit) {
    BasicComponent(
        title = title,
        onClick = onClick,
        startAction = { Icon(vectorResource(icon), null, tint = MiuixTheme.colorScheme.primary) }
    )
}

@Composable
private fun MiuixCourseDetailDialog(
    block: MergedCourseBlock,
    onDismiss: () -> Unit,
    onEdit: (String) -> Unit
) {
    val courses = remember(block) { block.clusterCourses.ifEmpty { block.courses } }
    if (courses.isEmpty()) return

    // Keep the reference project's vertical list structure inside the
    // Miuix dialog available in this KMP version. Each conflicting course is
    // a separate card with an inline edit action.
    WindowDialog(
        show = true,
        title = "课程详情",
        onDismissRequest = onDismiss,
        insideMargin = DpSize(16.dp, 16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(top = 56.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            courses.forEach { wrapper ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    cornerRadius = 20.dp,
                    insideMargin = PaddingValues(0.dp),
                    pressFeedbackType = PressFeedbackType.None,
                    showIndication = true,
                    colors = CardDefaults.defaultColors(
                        color = MiuixTheme.colorScheme.surfaceContainer,
                        contentColor = MiuixTheme.colorScheme.onSurface,
                    ),
                    onClick = { onEdit(wrapper.course.id) }
                ) {
                    MiuixCourseDetailItem(wrapper, onEdit)
                }
            }
        }
    }
}

@Composable
private fun MiuixCourseDetailItem(wrapper: CourseWithWeeks, onEdit: (String) -> Unit) {
    val course = wrapper.course
    val weekNames = stringArrayResource(Res.array.week_days_short_names)
    val dayName = weekNames.getOrNull(course.day - 1) ?: ""
    val time = if (course.isCustomTime) {
        "${course.customStartTime.orEmpty()} - ${course.customEndTime.orEmpty()}"
    } else {
        "第${course.startSection ?: 0}-${course.endSection ?: 0}节"
    }
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(vectorResource(Res.drawable.class_24px), null, tint = MiuixTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                course.name,
                style = MiuixTheme.textStyles.title3,
                color = MiuixTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            IconButton({ onEdit(course.id) }) { Icon(vectorResource(Res.drawable.edit_24px), "编辑", tint = MiuixTheme.colorScheme.primary) }
        }
        if (course.teacher.isNotBlank()) DetailLine(Res.drawable.person_24px, course.teacher)
        if (course.position.isNotBlank()) DetailLine(Res.drawable.location_on_24px, course.position)
        DetailLine(Res.drawable.calendar_today_24px, "${dayName} · ${formatWeekSummary(wrapper.weeks.map { it.weekNumber })}")
        DetailLine(Res.drawable.schedule_24px, time)
        if (!course.remark.isNullOrBlank()) DetailLine(Res.drawable.sticky_note_2_24px, course.remark.orEmpty())
    }
}

private fun formatWeekSummary(weeks: List<Int>): String {
    val sorted = weeks.distinct().sorted()
    if (sorted.isEmpty()) return "未设置周次"
    val ranges = buildList {
        var start = sorted.first()
        var previous = start
        for (week in sorted.drop(1)) {
            if (week == previous + 1) {
                previous = week
            } else {
                add(if (start == previous) "$start" else "$start-$previous")
                start = week
                previous = week
            }
        }
        add(if (start == previous) "$start" else "$start-$previous")
    }
    return ranges.joinToString(", ") + "周"
}

@Composable
private fun DetailLine(icon: org.jetbrains.compose.resources.DrawableResource, text: String) {
    Row(Modifier.fillMaxWidth().padding(top = 9.dp), verticalAlignment = Alignment.Top) {
        Icon(vectorResource(icon), null, tint = MiuixTheme.colorScheme.onSurfaceVariantActions, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, style = MiuixTheme.textStyles.body1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
    }
}
