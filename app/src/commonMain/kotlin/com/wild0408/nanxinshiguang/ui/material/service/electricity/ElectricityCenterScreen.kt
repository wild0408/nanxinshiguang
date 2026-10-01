package com.wild0408.nanxinshiguang.ui.material.service.electricity

import nanxinshiguang.generated.resources.action_edit_room
import nanxinshiguang.generated.resources.electricity_action_authenticating
import nanxinshiguang.generated.resources.electricity_action_clear
import nanxinshiguang.generated.resources.electricity_action_load_rooms
import nanxinshiguang.generated.resources.electricity_action_reauth
import nanxinshiguang.generated.resources.electricity_action_refresh
import nanxinshiguang.generated.resources.electricity_balance_low
import nanxinshiguang.generated.resources.electricity_balance_normal
import nanxinshiguang.generated.resources.electricity_current_balance
import nanxinshiguang.generated.resources.electricity_history_empty
import nanxinshiguang.generated.resources.electricity_insufficient_data
import nanxinshiguang.generated.resources.electricity_no_room
import nanxinshiguang.generated.resources.electricity_none
import nanxinshiguang.generated.resources.electricity_query_failed
import nanxinshiguang.generated.resources.electricity_section_auth
import nanxinshiguang.generated.resources.electricity_section_history
import nanxinshiguang.generated.resources.electricity_section_recent
import nanxinshiguang.generated.resources.electricity_section_trend
import nanxinshiguang.generated.resources.electricity_stat_queries
import nanxinshiguang.generated.resources.electricity_stat_recent_change
import nanxinshiguang.generated.resources.electricity_stat_recent_record
import nanxinshiguang.generated.resources.electricity_trend_need_two
import nanxinshiguang.generated.resources.electricity_unit_degree
import nanxinshiguang.generated.resources.electricity_updated_at

import kotlinx.datetime.number

import com.wild0408.nanxinshiguang.ui.viewmodel.service.electricity.ElectricityViewModel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wild0408.nanxinshiguang.data.api.electricity.ElectricityLocation
import com.wild0408.nanxinshiguang.ui.components.ToastManager
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.action_save_query
import nanxinshiguang.generated.resources.a11y_back
import nanxinshiguang.generated.resources.arrow_back_24px
import nanxinshiguang.generated.resources.label_building
import nanxinshiguang.generated.resources.label_campus
import nanxinshiguang.generated.resources.label_room
import nanxinshiguang.generated.resources.refresh_24px
import nanxinshiguang.generated.resources.title_electricity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectricityCenterScreen(
    onBack: () -> Unit,
    vm: ElectricityViewModel = koinViewModel(),
) {
    val state by vm.state.collectAsState()
    val room = listOfNotNull(
        state.selectedCampus?.name,
        state.selectedBuilding?.name,
        state.selectedRoom?.name,
    ).joinToString(" ")

    val failedTemplate = stringResource(Res.string.electricity_query_failed)

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.takeIf(String::isNotBlank)?.let { message ->
            ToastManager.show(failedTemplate.format(message))
            vm.dismissError()
        }
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.title_electricity)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            vectorResource(Res.drawable.arrow_back_24px),
                            contentDescription = stringResource(Res.string.a11y_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = vm::refresh, enabled = state.isConfigured && !state.isLoading) {
                        Icon(vectorResource(Res.drawable.refresh_24px), contentDescription = stringResource(Res.string.electricity_action_refresh))
                    }
                },
            )
        },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(contentPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (state.isInitializing && state.balance == null) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (!state.isConfigured || state.editing) {
                item { Text(stringResource(Res.string.electricity_section_auth), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                item {
                    Button(
                        onClick = vm::loginAndLoadCampuses,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoadingLocations,
                    ) {
                        Text(
                            when {
                                state.isLoadingLocations -> stringResource(Res.string.electricity_action_authenticating)
                                state.isLoggedIn -> stringResource(Res.string.electricity_action_reauth)
                                else -> stringResource(Res.string.electricity_action_load_rooms)
                            },
                        )
                    }
                }
                if (state.isLoggedIn) {
                    item { LocationMenu(stringResource(Res.string.label_campus), state.campuses, state.selectedCampus, vm::selectCampus, vm::loadCampuses) }
                    item { LocationMenu(stringResource(Res.string.label_building), state.buildings, state.selectedBuilding, vm::selectBuilding) }
                    item { LocationMenu(stringResource(Res.string.label_room), state.rooms, state.selectedRoom, vm::selectRoom) }
                }
                item {
                    Button(
                        onClick = vm::saveAndQuery,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading && state.isLoggedIn && state.selectedRoom != null,
                    ) {
                        Text(stringResource(Res.string.action_save_query))
                    }
                }
            }
            state.balance?.let { balance ->
                item { BalanceCard(balance, room, state.lastUpdated) }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(vm::refresh, Modifier.weight(1f), enabled = !state.isLoading) { Text(stringResource(Res.string.electricity_action_refresh)) }
                        Button(vm::editConfiguration, Modifier.weight(1f)) { Text(stringResource(Res.string.action_edit_room)) }
                    }
                }
                item { Text(stringResource(Res.string.electricity_section_history), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
                item { StatsCard(state.history) }
                item { TrendCard(state.history) }
                item { TextButton(onClick = vm::clearConfiguration, modifier = Modifier.fillMaxWidth()) { Text(stringResource(Res.string.electricity_action_clear)) } }
            }
            if (state.isConfigured && state.balance == null && state.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceCard(balance: Double, room: String, updated: Long?) {
    val color = when {
        balance < 10 -> MaterialTheme.colorScheme.error
        balance < 30 -> Color(0xFFB26A00)
        else -> MaterialTheme.colorScheme.primary
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(stringResource(Res.string.electricity_current_balance), style = MaterialTheme.typography.bodyLarge)
            Text(stringResource(Res.string.electricity_unit_degree, "%.2f".format(balance)), style = MaterialTheme.typography.headlineLarge.copy(fontSize = 38.sp, fontWeight = FontWeight.Bold), color = color)
            Text(if (balance < 10) stringResource(Res.string.electricity_balance_low) else stringResource(Res.string.electricity_balance_normal), color = color, style = MaterialTheme.typography.labelMedium)
            Text(if (room.isBlank()) stringResource(Res.string.electricity_no_room) else room, modifier = Modifier.padding(top = 14.dp))
            Text(stringResource(Res.string.electricity_updated_at, updated?.let(::formatTime) ?: "--"), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun StatsCard(history: List<Pair<Long, Double>>) {
    val latest = history.firstOrNull()
    val previous = history.getOrNull(1)
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Stat(stringResource(Res.string.electricity_stat_queries), history.size.toString())
            Stat(stringResource(Res.string.electricity_stat_recent_change), if (latest != null && previous != null) stringResource(Res.string.electricity_unit_degree, "%+.2f".format(latest.second - previous.second)) else stringResource(Res.string.electricity_insufficient_data))
            Stat(stringResource(Res.string.electricity_stat_recent_record), latest?.first?.let(::formatTime) ?: stringResource(Res.string.electricity_none))
        }
    }
}

@Composable
private fun TrendCard(history: List<Pair<Long, Double>>) {
    var range by remember { mutableStateOf(ElectricityTrendRange.DAYS_7) }
    val points = history.withoutConsecutiveDuplicates().forRange(range)
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(Res.string.electricity_section_trend), style = MaterialTheme.typography.titleMedium)
            SlidingRangeSelector(range, { range = it }, MaterialTheme.colorScheme)
            if (points.size < 2) {
                Text(stringResource(Res.string.electricity_trend_need_two), style = MaterialTheme.typography.labelMedium)
            } else {
                ElectricityLineChart(points, MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun SlidingRangeSelector(
    selected: ElectricityTrendRange,
    onSelected: (ElectricityTrendRange) -> Unit,
    colors: androidx.compose.material3.ColorScheme,
) {
    BoxWithConstraints(Modifier.fillMaxWidth().border(1.dp, colors.outlineVariant, RoundedCornerShape(10.dp)).padding(3.dp)) {
        val offset by animateDpAsState(if (selected.ordinal == 1) maxWidth / 2 else 0.dp, tween(220), label = "trend-range")
        Box(Modifier.width(maxWidth / 2).offset(x = offset).background(colors.primaryContainer, RoundedCornerShape(8.dp)).height(36.dp))
        Row(Modifier.fillMaxWidth()) {
            ElectricityTrendRange.entries.forEach { option ->
                Box(
                    Modifier.weight(1f).clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelected(option) },
                    ).height(36.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(stringResource(option.labelRes), color = if (selected == option) colors.onPrimaryContainer else colors.onSurfaceVariant, fontWeight = if (selected == option) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}

@Composable
private fun ElectricityLineChart(points: List<ElectricityTrendPoint>, color: Color) {
    var selectedIndex by remember(points) { mutableStateOf(points.lastIndex) }
    val min = points.minOf { it.balance }
    val max = points.maxOf { it.balance }
    val selected = points.getOrNull(selectedIndex)
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.width(52.dp)) {
                Text("%.2f".format(max), style = MaterialTheme.typography.labelSmall)
                Text("%.2f".format((max + min) / 2), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(vertical = 56.dp))
                Text("%.2f".format(min), style = MaterialTheme.typography.labelSmall)
            }
            Canvas(Modifier.weight(1f).height(190.dp).pointerInput(points) {
                detectTapGestures { offset -> selectedIndex = ((offset.x / size.width) * points.size).toInt().coerceIn(0, points.lastIndex) }
            }) {
        val top = 10.dp.toPx()
        val bottom = size.height - 12.dp.toPx()
        val left = 2.dp.toPx()
        val right = size.width - 2.dp.toPx()
        val range = (max - min).coerceAtLeast(0.01)
        listOf(top, (top + bottom) / 2f, bottom).forEach { y -> drawLine(color.copy(alpha = .18f), androidx.compose.ui.geometry.Offset(left, y), androidx.compose.ui.geometry.Offset(right, y), 1.dp.toPx()) }
        drawLine(color.copy(alpha = .55f), androidx.compose.ui.geometry.Offset(left, top), androidx.compose.ui.geometry.Offset(left, bottom), 1.dp.toPx())
        drawLine(color.copy(alpha = .55f), androidx.compose.ui.geometry.Offset(left, bottom), androidx.compose.ui.geometry.Offset(right, bottom), 1.dp.toPx())
        val path = Path()
        points.forEachIndexed { index, point ->
            val x = if (points.size == 1) left else left + (right - left) * index / (points.size - 1)
            val y = bottom - ((point.balance - min) / range * (bottom - top)).toFloat()
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            drawCircle(color, 4.dp.toPx(), androidx.compose.ui.geometry.Offset(x, y))
            if (index == selectedIndex) drawCircle(color, 8.dp.toPx(), androidx.compose.ui.geometry.Offset(x, y), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
            if (index == selectedIndex) drawLine(color.copy(alpha = .45f), androidx.compose.ui.geometry.Offset(x, top), androidx.compose.ui.geometry.Offset(x, bottom), 1.dp.toPx())
        }
        drawPath(path, color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()))
            }
        }
        Row(Modifier.fillMaxWidth().padding(start = 52.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatTrendDate(points.first().time), style = MaterialTheme.typography.labelSmall)
            if (points.size > 2) Text(formatTrendDate(points[points.size / 2].time), style = MaterialTheme.typography.labelSmall)
            Text(formatTrendDate(points.last().time), style = MaterialTheme.typography.labelSmall)
        }
        selected?.let { Text(formatTrendDate(it.time) + "  " + stringResource(Res.string.electricity_unit_degree, "%.2f".format(it.balance)), style = MaterialTheme.typography.labelMedium, color = color, modifier = Modifier.padding(start = 52.dp, top = 6.dp)) }
    }
}

private fun formatTrendDate(time: Long): String {
    val date = Instant.fromEpochMilliseconds(time).toLocalDateTime(TimeZone.currentSystemDefault())
    return "%02d/%02d".format(date.month.number, date.day)
}

@Composable
private fun Stat(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun HistoryCard(history: List<Pair<Long, Double>>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(stringResource(Res.string.electricity_section_recent), style = MaterialTheme.typography.titleMedium)
            if (history.isEmpty()) {
                Text(stringResource(Res.string.electricity_history_empty), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
            } else {
                history.take(7).forEach { record ->
                    Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(formatTime(record.first), style = MaterialTheme.typography.labelMedium)
                        Text(stringResource(Res.string.electricity_unit_degree, "%.2f".format(record.second)), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val time = Instant.fromEpochMilliseconds(ms).toLocalDateTime(TimeZone.currentSystemDefault())
    return "%04d-%02d-%02d %02d:%02d".format(time.year, time.month.number, time.day, time.hour, time.minute)
}

@Composable
private fun LocationMenu(
    label: String,
    items: List<ElectricityLocation>,
    selected: ElectricityLocation?,
    onSelect: (ElectricityLocation) -> Unit,
    onOpen: (() -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Button(
            onClick = { expanded = true; onOpen?.invoke() },
            modifier = Modifier.fillMaxWidth(),
            enabled = items.isNotEmpty() || onOpen != null,
        ) {
            Text(selected?.name ?: label)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { location ->
                DropdownMenuItem(
                    text = { Text(location.name) },
                    onClick = { onSelect(location); expanded = false },
                )
            }
        }
    }
}
