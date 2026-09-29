package com.wild0408.nanxinshiguang.ui.miuix.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.title_select_week
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun MiuixWeekSelectorDialog(
    totalWeeks: Int,
    currentWeek: Int?,
    selectedWeek: Int?,
    onWeekSelected: (Int) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val weeks = (1..totalWeeks.coerceAtLeast(0)).toList()
    WindowDialog(
        show = true,
        title = stringResource(Res.string.title_select_week),
        onDismissRequest = onDismissRequest,
        insideMargin = DpSize(16.dp, 16.dp),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 72.dp),
            modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(weeks) { week ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onWeekSelected(week) },
                    colors = CardDefaults.defaultColors(
                        color = if (week == selectedWeek) MiuixTheme.colorScheme.primaryContainer
                        else MiuixTheme.colorScheme.surfaceContainer,
                    ),
                ) {
                    Column(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(week.toString(), color = if (week == selectedWeek) MiuixTheme.colorScheme.onPrimaryContainer else MiuixTheme.colorScheme.onSurface, style = MiuixTheme.textStyles.title3)
                        if (week == currentWeek) {
                            Text("本周", color = if (week == selectedWeek) MiuixTheme.colorScheme.onPrimaryContainer else MiuixTheme.colorScheme.onSurfaceVariantSummary, style = MiuixTheme.textStyles.footnote1)
                        }
                    }
                }
            }
        }
    }
}
