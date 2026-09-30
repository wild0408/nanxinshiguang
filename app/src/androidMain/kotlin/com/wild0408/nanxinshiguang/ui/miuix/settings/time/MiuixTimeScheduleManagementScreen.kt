package com.wild0408.nanxinshiguang.ui.miuix.settings.time

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wild0408.nanxinshiguang.Destination
import com.wild0408.nanxinshiguang.ui.components.LocalNavigationHostPadding
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.HyperLiquidTopBarButton
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.rememberSharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperGlassTopBar
import com.wild0408.nanxinshiguang.ui.miuix.hyper.utils.overScrollVertical
import com.wild0408.nanxinshiguang.ui.settings.time.ScheduleType
import com.wild0408.nanxinshiguang.ui.settings.time.TimeScheduleItemUiModel
import com.wild0408.nanxinshiguang.ui.settings.time.TimeScheduleManagementViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.a11y_add_schedule_scheme
import nanxinshiguang.generated.resources.a11y_back
import nanxinshiguang.generated.resources.action_add_combo_schedule
import nanxinshiguang.generated.resources.action_add_public_schedule
import nanxinshiguang.generated.resources.action_cancel
import nanxinshiguang.generated.resources.action_copy
import nanxinshiguang.generated.resources.add_24px
import nanxinshiguang.generated.resources.a11y_edit_schedule
import nanxinshiguang.generated.resources.arrow_back_24px
import nanxinshiguang.generated.resources.follow_course_table
import nanxinshiguang.generated.resources.option_exclusive_schedule
import nanxinshiguang.generated.resources.option_unnamed_schedule
import nanxinshiguang.generated.resources.schedule_type_combo
import nanxinshiguang.generated.resources.schedule_type_public
import nanxinshiguang.generated.resources.title_schedule_management
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun MiuixTimeScheduleManagementScreen(
    onBack: () -> Unit,
    onEditSingleSchedule: (tableId: String?, isPublic: Boolean, copyFromId: String?) -> Unit,
    onEditComboSchedule: (comboId: String?, copyFromId: String?) -> Unit,
    viewModel: TimeScheduleManagementViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var showAddMenu by remember { mutableStateOf(false) }
    val background = MiuixTheme.colorScheme.surface
    val scrollBehavior = rememberSharedScrollBehavior()
    val backdrop = rememberLayerBackdrop { drawRect(background); drawContent() }
    val direction = LocalLayoutDirection.current
    val hostPadding = LocalNavigationHostPadding.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = background,
        topBar = {
            HyperGlassTopBar(
                title = stringResource(Res.string.title_schedule_management),
                backdrop = backdrop,
                scrollBehavior = scrollBehavior,
                startAction = { a, s ->
                    HyperLiquidTopBarButton(
                        onClick = onBack,
                        backdrop = backdrop,
                        icon = MiuixIcons.ChevronBackward,
                        contentDescription = stringResource(Res.string.a11y_back),
                        backdropAlpha = a,
                        shadowAlpha = s,
                    )
                },
                endAction = { a, s ->
                    HyperLiquidTopBarButton(
                        onClick = { showAddMenu = true },
                        backdrop = backdrop,
                        icon = vectorResource(Res.drawable.add_24px),
                        contentDescription = stringResource(Res.string.a11y_add_schedule_scheme),
                        backdropAlpha = a,
                        shadowAlpha = s,
                    )
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().background(background).layerBackdrop(backdrop)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().overScrollVertical().nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(
                    start = padding.calculateLeftPadding(direction) + 20.dp,
                    top = padding.calculateTopPadding() + 12.dp,
                    end = padding.calculateRightPadding(direction) + 20.dp,
                    bottom = padding.calculateBottomPadding() + hostPadding.calculateBottomPadding() + 20.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (!state.isDataLoaded) {
                    item { Text(stringResource(Res.string.title_schedule_management), color = MiuixTheme.colorScheme.onSurfaceVariantSummary) }
                } else {
                    items(state.items, key = { "${it.type.name}_${it.id}" }) { item ->
                        MiuixTimeScheduleItem(
                            item = item,
                            onSelect = { viewModel.bindTimeSchedule(item) },
                            onEdit = {
                                when (item.type) {
                                    ScheduleType.EXCLUSIVE -> onEditSingleSchedule(item.id, false, null)
                                    ScheduleType.PUBLIC -> onEditSingleSchedule(item.id, true, null)
                                    ScheduleType.COMBO -> onEditComboSchedule(item.id, null)
                                }
                            },
                            onCopy = {
                                when (item.type) {
                                    ScheduleType.PUBLIC -> onEditSingleSchedule(null, true, item.id)
                                    ScheduleType.COMBO -> onEditComboSchedule(null, item.id)
                                    ScheduleType.EXCLUSIVE -> Unit
                                }
                            },
                        )
                    }
                }
            }
        }
        OverlayDialog(
            title = stringResource(Res.string.title_schedule_management),
            show = showAddMenu,
            onDismissRequest = { showAddMenu = false },
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(
                    text = stringResource(Res.string.action_add_public_schedule),
                    onClick = { showAddMenu = false; onEditSingleSchedule(null, true, null) },
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = stringResource(Res.string.action_add_combo_schedule),
                    onClick = { showAddMenu = false; onEditComboSchedule(null, null) },
                    modifier = Modifier.weight(1f),
                )
            }
            TextButton(
                text = stringResource(Res.string.action_cancel),
                onClick = { showAddMenu = false },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun MiuixTimeScheduleItem(
    item: TimeScheduleItemUiModel,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onCopy: () -> Unit,
) {
    val title = when (item.type) {
        ScheduleType.EXCLUSIVE -> stringResource(Res.string.option_exclusive_schedule)
        ScheduleType.PUBLIC -> item.name ?: stringResource(Res.string.option_unnamed_schedule)
        ScheduleType.COMBO -> item.name ?: stringResource(Res.string.schedule_type_combo)
    }
    val summary = when {
        item.isSelected -> stringResource(Res.string.follow_course_table)
        item.type == ScheduleType.PUBLIC -> stringResource(Res.string.schedule_type_public)
        item.type == ScheduleType.COMBO -> stringResource(Res.string.schedule_type_combo)
        else -> stringResource(Res.string.option_exclusive_schedule)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer),
        insideMargin = PaddingValues(vertical = 2.dp),
    ) {
        BasicComponent(
            modifier = Modifier.fillMaxWidth(),
            title = title,
            summary = summary,
            onClick = onSelect,
            endActions = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(text = stringResource(Res.string.a11y_edit_schedule), onClick = onEdit)
                    if (item.type != ScheduleType.EXCLUSIVE) TextButton(text = stringResource(Res.string.action_copy), onClick = onCopy)
                }
            },
        )
    }
}
