package com.wild0408.nanxinshiguang.ui.miuix.settings.course

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.DpSize
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wild0408.nanxinshiguang.data.db.main.CourseTable
import com.wild0408.nanxinshiguang.ui.components.LocalNavigationHostPadding
import com.wild0408.nanxinshiguang.ui.components.ToastManager
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.HyperLiquidTopBarButton
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.rememberSharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperGlassTopBar
import com.wild0408.nanxinshiguang.ui.miuix.hyper.utils.overScrollVertical
import com.wild0408.nanxinshiguang.ui.viewmodel.settings.course.ManageCourseTablesViewModel
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.a11y_back
import nanxinshiguang.generated.resources.a11y_add_new_table
import nanxinshiguang.generated.resources.a11y_current_table
import nanxinshiguang.generated.resources.a11y_delete
import nanxinshiguang.generated.resources.a11y_edit
import nanxinshiguang.generated.resources.a11y_save
import nanxinshiguang.generated.resources.action_add
import nanxinshiguang.generated.resources.action_cancel
import nanxinshiguang.generated.resources.add_24px
import nanxinshiguang.generated.resources.check_circle_24px
import nanxinshiguang.generated.resources.confirm_delete
import nanxinshiguang.generated.resources.course_table_created_at_prefix
import nanxinshiguang.generated.resources.course_table_id_prefix
import nanxinshiguang.generated.resources.delete_24px
import nanxinshiguang.generated.resources.dialog_text_confirm_delete
import nanxinshiguang.generated.resources.dialog_title_add_table
import nanxinshiguang.generated.resources.dialog_title_edit_table
import nanxinshiguang.generated.resources.edit_24px
import nanxinshiguang.generated.resources.label_table_name
import nanxinshiguang.generated.resources.text_no_tables_hint
import nanxinshiguang.generated.resources.title_manage_course_tables
import nanxinshiguang.generated.resources.toast_add_table_success
import nanxinshiguang.generated.resources.toast_delete_last_table_failed
import nanxinshiguang.generated.resources.toast_delete_table_success
import nanxinshiguang.generated.resources.toast_edit_table_success
import nanxinshiguang.generated.resources.toast_name_empty
import nanxinshiguang.generated.resources.toast_switch_table_success
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog
import kotlin.time.Instant

@Composable
internal fun MiuixManageCourseTablesScreen(
    onBack: () -> Unit,
    viewModel: ManageCourseTablesViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val title = stringResource(Res.string.title_manage_course_tables)
    val scrollBehavior = rememberSharedScrollBehavior()
    val background = MiuixTheme.colorScheme.surface
    val backdrop = rememberLayerBackdrop {
        drawRect(background)
        drawContent()
    }
    val hostPadding = LocalNavigationHostPadding.current
    val layoutDirection = LocalLayoutDirection.current

    var editor by remember { mutableStateOf<MiuixTableEditor?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<CourseTable?>(null) }
    var showDelete by remember { mutableStateOf(false) }

    val emptyName = stringResource(Res.string.toast_name_empty)
    val editSuccess = stringResource(Res.string.toast_edit_table_success)
    val lastTableError = stringResource(Res.string.toast_delete_last_table_failed)
    val addSuccess = stringResource(Res.string.toast_add_table_success, "%NAME%")

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = background,
        topBar = {
            HyperGlassTopBar(
                title = title,
                largeTitle = title,
                backdrop = backdrop,
                scrollBehavior = scrollBehavior,
                startAction = { backdropAlpha, shadowAlpha ->
                    HyperLiquidTopBarButton(
                        onClick = onBack,
                        backdrop = backdrop,
                        icon = MiuixIcons.ChevronBackward,
                        contentDescription = stringResource(Res.string.a11y_back),
                        backdropAlpha = backdropAlpha,
                        shadowAlpha = shadowAlpha,
                    )
                },
                endAction = { backdropAlpha, shadowAlpha ->
                    HyperLiquidTopBarButton(
                        onClick = {
                            editor = MiuixTableEditor.Add
                            showEditor = true
                        },
                        backdrop = backdrop,
                        icon = vectorResource(Res.drawable.add_24px),
                        contentDescription = stringResource(Res.string.a11y_add_new_table),
                        backdropAlpha = backdropAlpha,
                        shadowAlpha = shadowAlpha,
                    )
                },
            )
        },
    ) { scaffoldPadding ->
        val contentPadding = PaddingValues(
            start = scaffoldPadding.calculateLeftPadding(layoutDirection) + 20.dp,
            top = scaffoldPadding.calculateTopPadding() + 12.dp,
            end = scaffoldPadding.calculateRightPadding(layoutDirection) + 20.dp,
            bottom = scaffoldPadding.calculateBottomPadding() +
                hostPadding.calculateBottomPadding() + 20.dp,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .layerBackdrop(backdrop),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (state.courseTables.isEmpty()) {
                    item(key = "empty") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            insideMargin = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
                            colors = CardDefaults.defaultColors(
                                color = MiuixTheme.colorScheme.surfaceContainer,
                            ),
                        ) {
                            Text(
                                text = stringResource(Res.string.text_no_tables_hint),
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                    }
                } else {
                    items(state.courseTables, key = CourseTable::id) { table ->
                        val selected = table.id == state.currentActiveTableId
                        val switchMessage = stringResource(
                            Res.string.toast_switch_table_success,
                            table.name,
                        )
                        MiuixCourseTableCard(
                            table = table,
                            selected = selected,
                            onSelect = {
                                viewModel.switchCourseTable(table.id)
                                ToastManager.show(switchMessage)
                            },
                            onEdit = {
                                editor = MiuixTableEditor.Edit(table)
                                showEditor = true
                            },
                            onDelete = {
                                deleting = table
                                showDelete = true
                            },
                        )
                    }
                }
            }
        }

        MiuixTableNameDialog(
            show = showEditor,
            mode = editor ?: MiuixTableEditor.Add,
            onDismiss = {
                showEditor = false
                editor = null
            },
            onConfirm = { mode, name ->
                if (name.isBlank()) {
                    ToastManager.show(emptyName)
                } else {
                    when (mode) {
                        MiuixTableEditor.Add -> {
                            viewModel.createNewCourseTable(name)
                            ToastManager.show(addSuccess.replace("%NAME%", name))
                        }

                        is MiuixTableEditor.Edit -> {
                            viewModel.updateCourseTable(mode.table.copy(name = name))
                            ToastManager.show(editSuccess)
                        }
                    }
                    showEditor = false
                }
            },
        )

        val tableToDelete = deleting
        val deleteSuccess = tableToDelete?.let {
            stringResource(Res.string.toast_delete_table_success, it.name)
        }.orEmpty()
        OverlayDialog(
            show = showDelete && tableToDelete != null,
            title = stringResource(Res.string.confirm_delete),
            summary = tableToDelete?.let {
                stringResource(Res.string.dialog_text_confirm_delete, it.name)
            }.orEmpty(),
            onDismissRequest = { showDelete = false },
            onDismissFinished = { deleting = null },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    text = stringResource(Res.string.action_cancel),
                    onClick = { showDelete = false },
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = stringResource(Res.string.a11y_delete),
                    onClick = {
                        tableToDelete?.let { table ->
                            if (state.courseTables.size <= 1) {
                                ToastManager.show(lastTableError)
                            } else {
                                viewModel.deleteCourseTable(table)
                                ToastManager.show(deleteSuccess)
                            }
                        }
                        showDelete = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColors(color = MiuixTheme.colorScheme.error),
                )
            }
        }
    }
}

private sealed interface MiuixTableEditor {
    data object Add : MiuixTableEditor
    data class Edit(val table: CourseTable) : MiuixTableEditor
}

@Composable
private fun MiuixTableNameDialog(
    show: Boolean,
    mode: MiuixTableEditor,
    onDismiss: () -> Unit,
    onConfirm: (MiuixTableEditor, String) -> Unit,
) {
    var name by remember(mode) {
        mutableStateOf((mode as? MiuixTableEditor.Edit)?.table?.name.orEmpty())
    }
    WindowDialog(
        show = show,
        title = stringResource(
            if (mode is MiuixTableEditor.Add) {
                Res.string.dialog_title_add_table
            } else {
                Res.string.dialog_title_edit_table
            },
        ),
        onDismissRequest = onDismiss,
        insideMargin = DpSize(16.dp, 16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.label_table_name),
                singleLine = true,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    text = stringResource(Res.string.action_cancel),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    text = stringResource(
                        if (mode is MiuixTableEditor.Add) {
                            Res.string.action_add
                        } else {
                            Res.string.a11y_save
                        },
                    ),
                    onClick = { onConfirm(mode, name) },
                    modifier = Modifier.weight(1f),
                    enabled = name.isNotBlank(),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
        }
    }
}

@Composable
private fun MiuixCourseTableCard(
    table: CourseTable,
    selected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onSelect,
        insideMargin = PaddingValues(start = 20.dp, top = 18.dp, end = 8.dp, bottom = 18.dp),
        colors = CardDefaults.defaultColors(
            color = if (selected) {
                MiuixTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MiuixTheme.colorScheme.surfaceContainer
            },
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(text = table.name, style = MiuixTheme.textStyles.body1)
                Text(
                    text = stringResource(
                        Res.string.course_table_id_prefix,
                        table.id.take(8) + "...",
                    ),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
                Text(
                    text = stringResource(
                        Res.string.course_table_created_at_prefix,
                        formatMiuixTableTime(table.createdAt),
                    ),
                    style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            if (selected) {
                Icon(
                    imageVector = vectorResource(Res.drawable.check_circle_24px),
                    contentDescription = stringResource(Res.string.a11y_current_table),
                    tint = MiuixTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = vectorResource(Res.drawable.edit_24px),
                    contentDescription = stringResource(Res.string.a11y_edit),
                    tint = MiuixTheme.colorScheme.onSurfaceVariantActions,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = vectorResource(Res.drawable.delete_24px),
                    contentDescription = stringResource(Res.string.a11y_delete),
                    tint = MiuixTheme.colorScheme.error,
                )
            }
        }
    }
}

private fun formatMiuixTableTime(timestamp: Long): String {
    val value = Instant.fromEpochMilliseconds(timestamp)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    return "%04d-%02d-%02d %02d:%02d".format(
        value.year,
        value.month.number,
        value.day,
        value.hour,
        value.minute,
    )
}
