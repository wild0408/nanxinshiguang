package com.wild0408.nanxinshiguang.ui.miuix.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.todayIn
import org.jetbrains.compose.resources.stringResource
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.action_cancel
import nanxinshiguang.generated.resources.action_confirm
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.NumberPicker
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.window.WindowDialog

/** Android Miuix date wheel; Material's DatePicker remains untouched in commonMain. */
@Composable
internal fun MiuixDatePickerDialog(
    title: String,
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    val today = kotlin.time.Clock.System.todayIn(TimeZone.currentSystemDefault())
    var year by remember { mutableIntStateOf(today.year) }
    var month by remember { mutableIntStateOf(today.month.number) }
    var day by remember { mutableIntStateOf(today.day) }

    WindowDialog(
        title = title,
        show = true,
        onDismissRequest = onDismiss,
        insideMargin = DpSize(16.dp, 16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                NumberPicker(
                    value = year,
                    onValueChange = { year = it },
                    modifier = Modifier.weight(1f),
                    range = (today.year - 5)..(today.year + 10),
                )
                NumberPicker(
                    value = month,
                    onValueChange = { month = it },
                    modifier = Modifier.weight(1f),
                    range = 1..12,
                )
                NumberPicker(
                    value = day,
                    onValueChange = { day = it },
                    modifier = Modifier.weight(1f),
                    range = 1..31,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TextButton(stringResource(Res.string.action_cancel), onDismiss, Modifier.weight(1f))
                TextButton(
                    text = stringResource(Res.string.action_confirm),
                    onClick = {
                        val millis = runCatching {
                            LocalDateTime(LocalDate(year, month, day), LocalTime(0, 0))
                                .toInstant(TimeZone.currentSystemDefault())
                                .toEpochMilliseconds()
                        }.getOrNull()
                        onDateSelected(millis)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
        }
    }
}
