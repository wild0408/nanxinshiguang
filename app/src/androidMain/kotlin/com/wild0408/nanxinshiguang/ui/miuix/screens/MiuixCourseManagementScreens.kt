package com.wild0408.nanxinshiguang.ui.miuix.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wild0408.nanxinshiguang.Destination
import com.wild0408.nanxinshiguang.data.db.main.CourseWithWeeks
import com.wild0408.nanxinshiguang.data.model.DualColor
import com.wild0408.nanxinshiguang.navigation.AddEditCourseChannel
import com.wild0408.nanxinshiguang.navigation.PresetCourseData
import com.wild0408.nanxinshiguang.ui.components.LocalNavigationHostPadding
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.HyperLiquidTopBarButton
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.rememberSharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperGlassTopBar
import com.wild0408.nanxinshiguang.ui.miuix.hyper.utils.overScrollVertical
import com.wild0408.nanxinshiguang.ui.settings.coursemanagement.CourseInstanceListViewModel
import com.wild0408.nanxinshiguang.ui.settings.coursemanagement.CourseNameCount
import com.wild0408.nanxinshiguang.ui.settings.coursemanagement.CourseNameListViewModel
import com.wild0408.nanxinshiguang.ui.settings.course.AddEditCourseViewModel
import com.wild0408.nanxinshiguang.ui.settings.course.CourseScheme
import com.wild0408.nanxinshiguang.ui.settings.course.UiEvent
import com.wild0408.nanxinshiguang.ui.components.ToastManager
import com.wild0408.nanxinshiguang.ui.theme.LocalIsDarkTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.a11y_back
import nanxinshiguang.generated.resources.a11y_color_selected
import nanxinshiguang.generated.resources.a11y_cancel_selection
import nanxinshiguang.generated.resources.a11y_delete
import nanxinshiguang.generated.resources.a11y_enter_selection_mode
import nanxinshiguang.generated.resources.a11y_exit_selection_mode
import nanxinshiguang.generated.resources.action_add
import nanxinshiguang.generated.resources.action_deselect_all
import nanxinshiguang.generated.resources.action_select_all
import nanxinshiguang.generated.resources.add_24px
import nanxinshiguang.generated.resources.check_24px
import nanxinshiguang.generated.resources.close_24px
import nanxinshiguang.generated.resources.delete_24px
import nanxinshiguang.generated.resources.item_course_management
import nanxinshiguang.generated.resources.menu_open_24px
import nanxinshiguang.generated.resources.text_no_unique_courses_hint
import nanxinshiguang.generated.resources.title_selected_items_count
import nanxinshiguang.generated.resources.week_days_full_names
import nanxinshiguang.generated.resources.title_add_course
import nanxinshiguang.generated.resources.title_edit_course
import nanxinshiguang.generated.resources.label_course_name
import nanxinshiguang.generated.resources.label_teacher
import nanxinshiguang.generated.resources.label_position
import nanxinshiguang.generated.resources.label_remark
import nanxinshiguang.generated.resources.label_custom_time
import nanxinshiguang.generated.resources.label_course_time
import nanxinshiguang.generated.resources.label_course_weeks
import nanxinshiguang.generated.resources.label_day_of_week
import nanxinshiguang.generated.resources.label_start_section
import nanxinshiguang.generated.resources.label_end_section
import nanxinshiguang.generated.resources.label_start_time
import nanxinshiguang.generated.resources.label_end_time
import nanxinshiguang.generated.resources.label_custom_time_range
import nanxinshiguang.generated.resources.title_select_weeks
import nanxinshiguang.generated.resources.title_select_color
import nanxinshiguang.generated.resources.title_select_time
import nanxinshiguang.generated.resources.action_cancel
import nanxinshiguang.generated.resources.action_confirm
import nanxinshiguang.generated.resources.a11y_save
import nanxinshiguang.generated.resources.toast_save_success
import nanxinshiguang.generated.resources.toast_delete_success
import nanxinshiguang.generated.resources.toast_name_empty
import nanxinshiguang.generated.resources.toast_time_invalid
import nanxinshiguang.generated.resources.common_dialog_title_abandon_changes
import nanxinshiguang.generated.resources.common_dialog_msg_unsaved_changes
import nanxinshiguang.generated.resources.common_action_continue_editing
import nanxinshiguang.generated.resources.common_action_exit_without_save
import nanxinshiguang.generated.resources.delete_24px
import nanxinshiguang.generated.resources.location_on_24px
import nanxinshiguang.generated.resources.person_24px
import nanxinshiguang.generated.resources.sticky_note_2_24px
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.RadioButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.window.WindowDialog
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.platform.LocalLayoutDirection

@Composable
internal fun MiuixCourseNameListScreen(
    onNavigate: (Destination) -> Unit,
    onBack: () -> Unit,
    viewModel: CourseNameListViewModel = koinViewModel(),
) {
    val names by viewModel.uniqueCourseNames.collectAsState()
    val selected = remember { mutableStateListOf<String>() }
    var selectionMode by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val title = stringResource(Res.string.item_course_management)
    val background = MiuixTheme.colorScheme.surface
    val scrollBehavior = rememberSharedScrollBehavior()
    val backdrop = rememberLayerBackdrop { drawRect(background); drawContent() }
    val hostPadding = LocalNavigationHostPadding.current
    val direction = LocalLayoutDirection.current
    fun exitSelection() { selectionMode = false; selected.clear() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = background,
        topBar = {
            HyperGlassTopBar(
                title = if (selectionMode) stringResource(Res.string.title_selected_items_count, selected.size) else title,
                backdrop = backdrop,
                scrollBehavior = scrollBehavior,
                startAction = { a, s ->
                    HyperLiquidTopBarButton(
                        onClick = { if (selectionMode) exitSelection() else onBack() },
                        backdrop = backdrop,
                        icon = if (selectionMode) vectorResource(Res.drawable.close_24px) else MiuixIcons.ChevronBackward,
                        contentDescription = stringResource(if (selectionMode) Res.string.a11y_cancel_selection else Res.string.a11y_back),
                        backdropAlpha = a,
                        shadowAlpha = s,
                    )
                },
                endAction = { a, s ->
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (selectionMode) {
                            val all = names.isNotEmpty() && selected.size == names.size
                            HyperLiquidTopBarButton(
                                onClick = { selected.clear(); if (!all) selected.addAll(names.map(CourseNameCount::name)) },
                                backdrop = backdrop,
                                icon = vectorResource(Res.drawable.check_24px),
                                contentDescription = stringResource(if (all) Res.string.action_deselect_all else Res.string.action_select_all),
                                backdropAlpha = a,
                                shadowAlpha = s,
                            )
                            HyperLiquidTopBarButton(
                                onClick = { if (selected.isNotEmpty()) scope.launch { viewModel.deleteSelectedCourses(selected.toList()); exitSelection() } },
                                backdrop = backdrop,
                                icon = vectorResource(Res.drawable.delete_24px),
                                contentDescription = stringResource(Res.string.a11y_delete),
                                backdropAlpha = a,
                                shadowAlpha = s,
                            )
                        }
                        HyperLiquidTopBarButton(
                            onClick = { if (selectionMode) exitSelection() else selectionMode = true },
                            backdrop = backdrop,
                            icon = vectorResource(Res.drawable.menu_open_24px),
                            contentDescription = stringResource(if (selectionMode) Res.string.a11y_exit_selection_mode else Res.string.a11y_enter_selection_mode),
                            backdropAlpha = a,
                            shadowAlpha = s,
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().background(background).layerBackdrop(backdrop)) {
            if (names.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(stringResource(Res.string.text_no_unique_courses_hint), color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize().overScrollVertical().nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentPadding = PaddingValues(
                        start = padding.calculateLeftPadding(direction) + 20.dp,
                        top = padding.calculateTopPadding() + 12.dp,
                        end = padding.calculateRightPadding(direction) + 20.dp,
                        bottom = padding.calculateBottomPadding() + hostPadding.calculateBottomPadding() + 88.dp,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(names, key = CourseNameCount::name) { item ->
                        MiuixCourseNameCard(item, item.name in selected, { name ->
                            if (selectionMode) {
                                if (name in selected) selected.remove(name) else selected.add(name)
                            } else onNavigate(Destination.CourseManagementDetail(name))
                        }, { name -> if (!selectionMode) { selectionMode = true; selected.add(name) } })
                    }
                }
            }
            if (!selectionMode) {
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            AddEditCourseChannel.sendEvent(PresetCourseData(startSection = 1, endSection = 2))
                            onNavigate(Destination.AddEditCourse())
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = hostPadding.calculateBottomPadding() + 20.dp),
                ) { Icon(vectorResource(Res.drawable.add_24px), stringResource(Res.string.action_add), tint = MiuixTheme.colorScheme.onPrimary) }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MiuixCourseNameCard(item: CourseNameCount, selected: Boolean, onClick: (String) -> Unit, onLongClick: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(104.dp).combinedClickable(onClick = { onClick(item.name) }, onLongClick = { onLongClick(item.name) }),
        insideMargin = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        colors = CardDefaults.defaultColors(color = if (selected) MiuixTheme.colorScheme.primaryContainer else MiuixTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Text(item.name, color = if (selected) MiuixTheme.colorScheme.onPrimaryContainer else MiuixTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text("${item.count}", color = if (selected) MiuixTheme.colorScheme.onPrimaryContainer else MiuixTheme.colorScheme.onSurfaceVariantSummary)
        }
    }
}

@Composable
internal fun MiuixCourseInstanceListScreen(
    courseName: String,
    onNavigateBack: () -> Unit,
    onNavigate: (Destination) -> Unit,
    viewModel: CourseInstanceListViewModel = koinViewModel(),
) {
    LaunchedEffect(courseName) { viewModel.initCourseName(courseName) }
    val instances by viewModel.courseInstances.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selected by viewModel.selectedCourseIds.collectAsStateWithLifecycle()
    val selectionMode by viewModel.isSelectionMode.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val background = MiuixTheme.colorScheme.surface
    val scrollBehavior = rememberSharedScrollBehavior()
    val backdrop = rememberLayerBackdrop { drawRect(background); drawContent() }
    val hostPadding = LocalNavigationHostPadding.current
    val direction = LocalLayoutDirection.current
    Scaffold(
        modifier = Modifier.fillMaxSize(), containerColor = background,
        topBar = {
            HyperGlassTopBar(
                title = if (selectionMode) stringResource(Res.string.title_selected_items_count, selected.size) else courseName,
                backdrop = backdrop, scrollBehavior = scrollBehavior,
                startAction = { a, s -> HyperLiquidTopBarButton(
                    onClick = { if (selectionMode) viewModel.toggleSelectionMode() else onNavigateBack() },
                    backdrop = backdrop,
                    icon = if (selectionMode) vectorResource(Res.drawable.close_24px) else MiuixIcons.ChevronBackward,
                    contentDescription = stringResource(if (selectionMode) Res.string.a11y_cancel_selection else Res.string.a11y_back),
                    backdropAlpha = a, shadowAlpha = s,
                ) },
                endAction = { a, s -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (selectionMode) {
                        HyperLiquidTopBarButton(onClick = viewModel::toggleSelectAll, backdrop = backdrop, icon = vectorResource(Res.drawable.check_24px), contentDescription = stringResource(Res.string.action_select_all), backdropAlpha = a, shadowAlpha = s)
                        HyperLiquidTopBarButton(onClick = { scope.launch { viewModel.deleteSelectedCourses() } }, backdrop = backdrop, icon = vectorResource(Res.drawable.delete_24px), contentDescription = stringResource(Res.string.a11y_delete), backdropAlpha = a, shadowAlpha = s)
                    }
                    HyperLiquidTopBarButton(onClick = viewModel::toggleSelectionMode, backdrop = backdrop, icon = vectorResource(Res.drawable.menu_open_24px), contentDescription = stringResource(if (selectionMode) Res.string.a11y_exit_selection_mode else Res.string.a11y_enter_selection_mode), backdropAlpha = a, shadowAlpha = s)
                } },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().background(background).layerBackdrop(backdrop)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().overScrollVertical().nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(start = padding.calculateLeftPadding(direction) + 20.dp, top = padding.calculateTopPadding() + 12.dp, end = padding.calculateRightPadding(direction) + 20.dp, bottom = padding.calculateBottomPadding() + hostPadding.calculateBottomPadding() + 88.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(instances, key = { it.course.id }) { item ->
                    MiuixCourseInstanceCard(item, item.course.id in selected, uiState.courseColorMaps, { id -> if (selectionMode) viewModel.toggleCourseSelection(id) else onNavigate(Destination.AddEditCourse(id)) }, { id -> if (!selectionMode) viewModel.toggleSelectionMode(); viewModel.toggleCourseSelection(id) })
                }
            }
            if (!selectionMode) FloatingActionButton(onClick = {
                scope.launch { AddEditCourseChannel.sendEvent(PresetCourseData(name = courseName, startSection = 1, endSection = 2)); onNavigate(Destination.AddEditCourse()) }
            }, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = hostPadding.calculateBottomPadding() + 20.dp)) { Icon(vectorResource(Res.drawable.add_24px), stringResource(Res.string.action_add), tint = MiuixTheme.colorScheme.onPrimary) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MiuixCourseInstanceCard(item: CourseWithWeeks, selected: Boolean, colors: List<DualColor>, onClick: (String) -> Unit, onLongClick: (String) -> Unit) {
    val dark = LocalIsDarkTheme.current
    val color = colors.getOrNull(item.course.colorInt)?.let { if (dark) it.dark else it.light } ?: MiuixTheme.colorScheme.surfaceContainer
    val days = org.jetbrains.compose.resources.stringArrayResource(Res.array.week_days_full_names)
    val day = days.getOrNull(item.course.day - 1).orEmpty()
    val time = if (item.course.isCustomTime) "${item.course.customStartTime.orEmpty()} - ${item.course.customEndTime.orEmpty()}" else "第${item.course.startSection ?: "?"}-${item.course.endSection ?: "?"}节"
    Card(modifier = Modifier.fillMaxWidth().combinedClickable(onClick = { onClick(item.course.id) }, onLongClick = { onLongClick(item.course.id) }), insideMargin = PaddingValues(16.dp), colors = CardDefaults.defaultColors(color = if (selected) MiuixTheme.colorScheme.primaryContainer else color.copy(alpha = 0.92f))) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(item.course.teacher.ifBlank { "未填写教师" }, color = MiuixTheme.colorScheme.onSurface)
            Text(item.course.position.ifBlank { "未填写地点" }, color = MiuixTheme.colorScheme.onSurfaceVariantSummary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("$day · $time", color = MiuixTheme.colorScheme.onSurface)
            Text("周次: ${item.weeks.joinToString(", ") { it.weekNumber.toString() }}", color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        }
    }
}

@Composable
internal fun MiuixAddEditCourseScreen(
    onBack: () -> Unit,
    courseId: String?,
    viewModel: AddEditCourseViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(courseId) { viewModel.initWithId(courseId) }
    var activeSchemeId by remember { mutableStateOf<String?>(null) }
    var showWeeks by remember { mutableStateOf(false) }
    var showColor by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    var showExit by remember { mutableStateOf(false) }
    val saveText = stringResource(Res.string.toast_save_success)
    val deleteText = stringResource(Res.string.toast_delete_success)
    val nameEmptyText = stringResource(Res.string.toast_name_empty)
    val invalidTimeText = stringResource(Res.string.toast_time_invalid)
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                UiEvent.SaveSuccess -> { ToastManager.show(saveText); onBack() }
                UiEvent.DeleteSuccess -> { ToastManager.show(deleteText); onBack() }
                UiEvent.Cancel -> onBack()
            }
        }
    }
    fun requestBack() { if (viewModel.hasUnsavedChanges()) showExit = true else onBack() }
    fun save() {
        if (state.name.isBlank()) { ToastManager.show(nameEmptyText); return }
        if (state.schemes.any { scheme ->
                if (scheme.isCustomTime) {
                    scheme.customStartTime.isBlank() || scheme.customEndTime.isBlank() || scheme.customStartTime >= scheme.customEndTime
                } else scheme.startSection > scheme.endSection
            }) {
            ToastManager.show(invalidTimeText)
        } else viewModel.onSave()
    }
    val background = MiuixTheme.colorScheme.surface
    val scrollBehavior = rememberSharedScrollBehavior()
    val backdrop = rememberLayerBackdrop { drawRect(background); drawContent() }
    val hostPadding = LocalNavigationHostPadding.current
    val direction = LocalLayoutDirection.current
    val title = stringResource(if (state.isEditing) Res.string.title_edit_course else Res.string.title_add_course)
    Scaffold(
        modifier = Modifier.fillMaxSize(), containerColor = background,
        topBar = {
            HyperGlassTopBar(
                title = title, backdrop = backdrop, scrollBehavior = scrollBehavior,
                startAction = { a, s -> HyperLiquidTopBarButton(::requestBack, backdrop, MiuixIcons.ChevronBackward, stringResource(Res.string.a11y_back), backdropAlpha = a, shadowAlpha = s) },
                endAction = { a, s -> Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (state.isEditing) HyperLiquidTopBarButton(viewModel::onDelete, backdrop, vectorResource(Res.drawable.delete_24px), stringResource(Res.string.a11y_delete), backdropAlpha = a, shadowAlpha = s)
                    HyperLiquidTopBarButton(::save, backdrop, vectorResource(Res.drawable.check_24px), stringResource(Res.string.a11y_save), backdropAlpha = a, shadowAlpha = s)
                } },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().background(background).layerBackdrop(backdrop)) {
            if (!state.isDataLoaded) {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text("加载中…", color = MiuixTheme.colorScheme.onSurfaceVariantSummary) }
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize().overScrollVertical().nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentPadding = PaddingValues(start = padding.calculateLeftPadding(direction) + 20.dp, top = padding.calculateTopPadding() + 12.dp, end = padding.calculateRightPadding(direction) + 20.dp, bottom = padding.calculateBottomPadding() + hostPadding.calculateBottomPadding() + 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), insideMargin = PaddingValues(16.dp), colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer)) {
                            TextField(state.name, viewModel::onNameChange, Modifier.fillMaxWidth(), label = stringResource(Res.string.label_course_name), singleLine = true)
                        }
                    }
                    items(state.schemes, key = CourseScheme::id) { scheme ->
                        MiuixCourseSchemeCard(
                            scheme = scheme,
                            timeSlots = state.timeSlots,
                            colors = state.courseColorMaps,
                            showRemove = state.schemes.size > 1,
                            onTeacher = { value -> viewModel.updateScheme(scheme.id) { it.copy(teacher = value) } },
                            onPosition = { value -> viewModel.updateScheme(scheme.id) { it.copy(position = value) } },
                            onRemark = { value -> viewModel.onSchemeRemarkChange(scheme.id, value) },
                            onWeeks = { activeSchemeId = scheme.id; showWeeks = true },
                            onColor = { activeSchemeId = scheme.id; showColor = true },
                            onTime = { activeSchemeId = scheme.id; showTime = true },
                            onRemove = { viewModel.removeScheme(scheme.id) },
                            onCustomTime = { value -> viewModel.toggleCustomTime(scheme.id, value) },
                        )
                    }
                    item {
                        Button(onClick = viewModel::addScheme, modifier = Modifier.fillMaxWidth()) { Text(stringResource(Res.string.action_add)) }
                    }
                }
            }
            val active = state.schemes.find { it.id == activeSchemeId }
            if (active != null && showWeeks) MiuixWeeksDialog(active.weeks, state.semesterTotalWeeks, { showWeeks = false }) { value -> viewModel.updateScheme(active.id) { it.copy(weeks = value) }; showWeeks = false }
            if (active != null && showColor) MiuixColorDialog(active.colorIndex, state.courseColorMaps, { showColor = false }) { value -> viewModel.updateScheme(active.id) { it.copy(colorIndex = value) }; showColor = false }
            if (active != null && showTime) MiuixTimeDialog(active, state.timeSlots, { showTime = false }) { day, start, end, custom, startTime, endTime ->
                viewModel.updateScheme(active.id) { it.copy(day = day, startSection = start, endSection = end, isCustomTime = custom, customStartTime = startTime, customEndTime = endTime) }
                showTime = false
            }
            if (showExit) OverlayDialog(show = true, title = stringResource(Res.string.common_dialog_title_abandon_changes), onDismissRequest = { showExit = false }) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(stringResource(Res.string.common_dialog_msg_unsaved_changes), color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TextButton(stringResource(Res.string.common_action_continue_editing), { showExit = false }, Modifier.weight(1f))
                        TextButton(stringResource(Res.string.common_action_exit_without_save), { showExit = false; onBack() }, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MiuixCourseSchemeCard(
    scheme: CourseScheme,
    timeSlots: List<com.wild0408.nanxinshiguang.data.db.main.TimeSlot>,
    colors: List<DualColor>,
    showRemove: Boolean,
    onTeacher: (String) -> Unit,
    onPosition: (String) -> Unit,
    onRemark: (String) -> Unit,
    onWeeks: () -> Unit,
    onColor: () -> Unit,
    onTime: () -> Unit,
    onRemove: () -> Unit,
    onCustomTime: (Boolean) -> Unit,
) {
    val dayNames = org.jetbrains.compose.resources.stringArrayResource(Res.array.week_days_full_names)
    val day = dayNames.getOrNull(scheme.day - 1).orEmpty()
    val slotStart = timeSlots.find { it.number == scheme.startSection }?.alias ?: scheme.startSection.toString()
    val slotEnd = timeSlots.find { it.number == scheme.endSection }?.alias ?: scheme.endSection.toString()
    val timeSummary = if (scheme.isCustomTime) "$day · ${scheme.customStartTime}-${scheme.customEndTime}" else "$day · $slotStart-$slotEnd 节"
    val color = colors.getOrNull(scheme.colorIndex)?.light ?: MiuixTheme.colorScheme.primary
    Card(modifier = Modifier.fillMaxWidth(), insideMargin = PaddingValues(start = 16.dp, top = 16.dp, end = 10.dp, bottom = 16.dp), colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(28.dp).clip(CircleShape).background(color).combinedClickable(onClick = onColor, onLongClick = onColor))
                TextField(scheme.teacher, onTeacher, Modifier.weight(1f), label = stringResource(Res.string.label_teacher), singleLine = true)
                Switch(checked = scheme.isCustomTime, onCheckedChange = onCustomTime)
                if (showRemove) IconButton(onClick = onRemove) { Icon(vectorResource(Res.drawable.delete_24px), stringResource(Res.string.a11y_delete), tint = MiuixTheme.colorScheme.error) }
            }
            TextField(scheme.position, onPosition, Modifier.fillMaxWidth(), label = stringResource(Res.string.label_position), singleLine = true)
            TextField(scheme.remark, onRemark, Modifier.fillMaxWidth(), label = stringResource(Res.string.label_remark), singleLine = true)
            BasicComponent(title = stringResource(Res.string.label_course_time), summary = timeSummary, onClick = onTime)
            BasicComponent(title = stringResource(Res.string.label_course_weeks), summary = if (scheme.weeks.isEmpty()) "未选择" else scheme.weeks.sorted().joinToString(", "), onClick = onWeeks)
        }
    }
}

@Composable
private fun MiuixWeeksDialog(selected: Set<Int>, totalWeeks: Int, onDismiss: () -> Unit, onConfirm: (Set<Int>) -> Unit) {
    var value by remember(selected) { mutableStateOf(selected) }
    WindowDialog(show = true, title = stringResource(Res.string.title_select_weeks), onDismissRequest = onDismiss, insideMargin = DpSize(16.dp, 16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LazyVerticalGrid(columns = GridCells.Fixed(5), modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(totalWeeks) { index ->
                    val week = index + 1
                    val checked = week in value
                    Box(Modifier.size(48.dp).background(if (checked) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.surfaceContainer).combinedClickable(onClick = { value = if (checked) value - week else value + week }, onLongClick = {}), contentAlignment = Alignment.Center) { Text(week.toString(), color = if (checked) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurface) }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(stringResource(Res.string.action_cancel), onDismiss, Modifier.weight(1f))
                TextButton(stringResource(Res.string.action_confirm), { onConfirm(value) }, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MiuixColorDialog(selected: Int, colors: List<DualColor>, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var value by remember(selected) { mutableStateOf(selected) }
    WindowDialog(show = true, title = stringResource(Res.string.title_select_color), onDismissRequest = onDismiss, insideMargin = DpSize(16.dp, 16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            LazyVerticalGrid(columns = GridCells.Fixed(4), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(colors) { index, color ->
                    Box(Modifier.size(48.dp).clip(CircleShape).background(color.light).combinedClickable(onClick = { value = index }, onLongClick = {}), contentAlignment = Alignment.Center) {
                        if (value == index) Icon(vectorResource(Res.drawable.check_24px), stringResource(Res.string.a11y_color_selected), tint = Color.Black)
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(stringResource(Res.string.action_cancel), onDismiss, Modifier.weight(1f))
                TextButton(stringResource(Res.string.action_confirm), { onConfirm(value) }, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MiuixTimeDialog(scheme: CourseScheme, timeSlots: List<com.wild0408.nanxinshiguang.data.db.main.TimeSlot>, onDismiss: () -> Unit, onConfirm: (Int, Int, Int, Boolean, String, String) -> Unit) {
    var day by remember(scheme) { mutableStateOf(scheme.day) }
    var start by remember(scheme) { mutableStateOf(scheme.startSection.toString()) }
    var end by remember(scheme) { mutableStateOf(scheme.endSection.toString()) }
    var custom by remember(scheme) { mutableStateOf(scheme.isCustomTime) }
    var startTime by remember(scheme) { mutableStateOf(scheme.customStartTime) }
    var endTime by remember(scheme) { mutableStateOf(scheme.customEndTime) }
    val days = org.jetbrains.compose.resources.stringArrayResource(Res.array.week_days_full_names)
    WindowDialog(show = true, title = stringResource(Res.string.title_select_time), onDismissRequest = onDismiss, insideMargin = DpSize(16.dp, 16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            BasicComponent(title = stringResource(Res.string.label_day_of_week), summary = days.getOrNull(day - 1).orEmpty(), onClick = { day = if (day >= 7) 1 else day + 1 })
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) { Text(stringResource(Res.string.label_custom_time_range)); Switch(custom, { custom = it }) }
            if (custom) {
                TextField(startTime, { startTime = it }, Modifier.fillMaxWidth(), label = stringResource(Res.string.label_start_time), singleLine = true)
                TextField(endTime, { endTime = it }, Modifier.fillMaxWidth(), label = stringResource(Res.string.label_end_time), singleLine = true)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextField(start, { start = it.filter(Char::isDigit) }, Modifier.weight(1f), label = stringResource(Res.string.label_start_section), singleLine = true)
                    TextField(end, { end = it.filter(Char::isDigit) }, Modifier.weight(1f), label = stringResource(Res.string.label_end_section), singleLine = true)
                }
                Text("可用节次：${timeSlots.sortedBy { it.number }.joinToString("、") { it.alias ?: it.number.toString() }}", color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(stringResource(Res.string.action_cancel), onDismiss, Modifier.weight(1f))
                TextButton(stringResource(Res.string.action_confirm), { onConfirm(day, start.toIntOrNull() ?: 1, end.toIntOrNull() ?: 1, custom, startTime, endTime) }, Modifier.weight(1f))
            }
        }
    }
}
