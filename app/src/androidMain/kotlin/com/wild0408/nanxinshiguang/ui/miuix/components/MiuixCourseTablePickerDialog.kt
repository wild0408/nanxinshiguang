package com.wild0408.nanxinshiguang.ui.miuix.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.a11y_add_new_table
import nanxinshiguang.generated.resources.action_cancel
import nanxinshiguang.generated.resources.action_confirm
import nanxinshiguang.generated.resources.action_add
import nanxinshiguang.generated.resources.add_24px
import nanxinshiguang.generated.resources.dialog_title_add_table
import nanxinshiguang.generated.resources.label_current
import nanxinshiguang.generated.resources.label_table_name
import nanxinshiguang.generated.resources.text_no_course_tables
import nanxinshiguang.generated.resources.toast_add_table_success
import nanxinshiguang.generated.resources.toast_name_empty
import com.wild0408.nanxinshiguang.data.db.main.CourseTable
import com.wild0408.nanxinshiguang.data.repository.AppSettingsRepository
import com.wild0408.nanxinshiguang.data.repository.CourseTableRepository
import com.wild0408.nanxinshiguang.ui.components.ToastManager
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.RadioButton
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@KoinViewModel
class MiuixCourseTablePickerDeps(
    val courseTableRepository: CourseTableRepository,
    val appSettingsRepository: AppSettingsRepository,
) : ViewModel() {
    fun createNewCourseTable(name: String) {
        viewModelScope.launch { courseTableRepository.createNewCourseTable(name) }
    }
}

@Composable
fun MiuixCourseTablePickerDialog(
    title: String,
    onDismissRequest: () -> Unit,
    onTableSelected: (CourseTable) -> Unit,
    deps: MiuixCourseTablePickerDeps = koinViewModel(),
) {
    val tables by deps.courseTableRepository.getAllCourseTables().collectAsState(initial = emptyList())
    val settings by deps.appSettingsRepository.getAppSettings().collectAsState(initial = null)
    var selectedId by remember { mutableStateOf<String?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    LaunchedEffect(tables, settings?.currentCourseTableId) {
        if (selectedId == null) selectedId = tables.firstOrNull { it.id == settings?.currentCourseTableId }?.id ?: tables.firstOrNull()?.id
    }

    WindowDialog(show = true, title = title, onDismissRequest = onDismissRequest, insideMargin = DpSize(16.dp, 16.dp)) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (tables.isEmpty()) {
                Text(stringResource(Res.string.text_no_course_tables), color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
            } else {
                LazyColumn(Modifier.fillMaxWidth().heightIn(max = 360.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tables, key = CourseTable::id) { table ->
                        val selected = table.id == selectedId
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { selectedId = table.id },
                            colors = CardDefaults.defaultColors(if (selected) MiuixTheme.colorScheme.primaryContainer.copy(alpha = .28f) else MiuixTheme.colorScheme.surfaceContainer),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(table.name, color = MiuixTheme.colorScheme.onSurface)
                                    if (table.id == settings?.currentCourseTableId) {
                                        Text(stringResource(Res.string.label_current), color = MiuixTheme.colorScheme.primary, style = MiuixTheme.textStyles.footnote1)
                                    }
                                }
                                RadioButton(selected = selected, onClick = { selectedId = table.id })
                            }
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { showAdd = true }) { Icon(vectorResource(Res.drawable.add_24px), stringResource(Res.string.a11y_add_new_table), tint = MiuixTheme.colorScheme.primary) }
                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(text = stringResource(Res.string.action_cancel), onClick = onDismissRequest)
                    TextButton(text = stringResource(Res.string.action_confirm), enabled = selectedId != null, colors = ButtonDefaults.textButtonColorsPrimary(), onClick = { tables.firstOrNull { it.id == selectedId }?.let(onTableSelected) })
                }
            }
        }
    }

    if (showAdd) {
        val addLabel = stringResource(Res.string.action_add)
        val emptyMessage = stringResource(Res.string.toast_name_empty)
        val successMessage = stringResource(Res.string.toast_add_table_success, newName)
        WindowDialog(show = true, title = stringResource(Res.string.dialog_title_add_table), onDismissRequest = { showAdd = false; newName = "" }, insideMargin = DpSize(16.dp, 16.dp)) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(value = newName, onValueChange = { newName = it }, modifier = Modifier.fillMaxWidth(), label = stringResource(Res.string.label_table_name), singleLine = true)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)) {
                    TextButton(text = stringResource(Res.string.action_cancel), onClick = { showAdd = false })
                    TextButton(text = addLabel, enabled = newName.isNotBlank(), colors = ButtonDefaults.textButtonColorsPrimary(), onClick = {
                        if (newName.isBlank()) ToastManager.show(emptyMessage)
                        else { deps.createNewCourseTable(newName); ToastManager.show(successMessage); showAdd = false; newName = "" }
                    })
                }
            }
        }
    }
}
