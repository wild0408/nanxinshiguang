package com.wild0408.nanxinshiguang.ui.miuix.service.electricity

import nanxinshiguang.generated.resources.a11y_back
import nanxinshiguang.generated.resources.a11y_refresh
import nanxinshiguang.generated.resources.action_edit_room
import nanxinshiguang.generated.resources.electricity_action_authenticating
import nanxinshiguang.generated.resources.electricity_action_clear
import nanxinshiguang.generated.resources.electricity_action_load_rooms
import nanxinshiguang.generated.resources.electricity_action_reauth
import nanxinshiguang.generated.resources.electricity_action_refresh
import nanxinshiguang.generated.resources.electricity_balance_low
import nanxinshiguang.generated.resources.electricity_balance_normal
import nanxinshiguang.generated.resources.electricity_current_balance
import nanxinshiguang.generated.resources.electricity_insufficient_data
import nanxinshiguang.generated.resources.electricity_loading_prompt
import nanxinshiguang.generated.resources.electricity_no_room
import nanxinshiguang.generated.resources.electricity_none
import nanxinshiguang.generated.resources.electricity_query_failed
import nanxinshiguang.generated.resources.electricity_section_auth
import nanxinshiguang.generated.resources.electricity_section_history
import nanxinshiguang.generated.resources.electricity_section_trend
import nanxinshiguang.generated.resources.electricity_select_room_hint
import nanxinshiguang.generated.resources.electricity_select_room_prompt
import nanxinshiguang.generated.resources.electricity_stat_queries
import nanxinshiguang.generated.resources.electricity_stat_recent_change
import nanxinshiguang.generated.resources.electricity_stat_recent_record
import nanxinshiguang.generated.resources.electricity_trend_need_two
import nanxinshiguang.generated.resources.electricity_unit_degree
import nanxinshiguang.generated.resources.electricity_updated_at

import kotlinx.datetime.number

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.Canvas
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import com.wild0408.nanxinshiguang.data.api.electricity.ElectricityLocation
import com.wild0408.nanxinshiguang.ui.components.ToastManager
import com.wild0408.nanxinshiguang.ui.components.LocalNavigationHostPadding
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.HyperLiquidTopBarButton
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.rememberSharedScrollBehavior
import com.wild0408.nanxinshiguang.ui.miuix.hyper.chrome.HyperGlassTopBar
import com.wild0408.nanxinshiguang.ui.miuix.hyper.utils.overScrollVertical
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wild0408.nanxinshiguang.ui.viewmodel.service.electricity.ElectricityViewModel
import com.wild0408.nanxinshiguang.ui.material.service.electricity.ElectricityTrendPoint
import com.wild0408.nanxinshiguang.ui.material.service.electricity.ElectricityTrendRange
import com.wild0408.nanxinshiguang.ui.material.service.electricity.forRange
import com.wild0408.nanxinshiguang.ui.material.service.electricity.withoutConsecutiveDuplicates
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.viewmodel.koinViewModel
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.action_save_query
import nanxinshiguang.generated.resources.label_building
import nanxinshiguang.generated.resources.label_campus
import nanxinshiguang.generated.resources.label_room
import nanxinshiguang.generated.resources.refresh_24px
import nanxinshiguang.generated.resources.title_electricity
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun MiuixElectricityCenterScreen(
    onBack: () -> Unit,
    viewModel: ElectricityViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val configuring = !state.isConfigured || state.editing
    val room = listOfNotNull(state.selectedCampus?.name, state.selectedBuilding?.name, state.selectedRoom?.name).joinToString(" ")

    val failedTemplate = stringResource(Res.string.electricity_query_failed)

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.takeIf(String::isNotBlank)?.let {
            ToastManager.show(failedTemplate.format(it))
            viewModel.dismissError()
        }
    }
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
                title = stringResource(Res.string.title_electricity),
                backdrop = backdrop,
                scrollBehavior = scrollBehavior,
                startAction = { a, s -> HyperLiquidTopBarButton(onBack, backdrop, MiuixIcons.ChevronBackward, stringResource(Res.string.a11y_back), backdropAlpha = a, shadowAlpha = s) },
                endAction = { a, s -> HyperLiquidTopBarButton(viewModel::refresh, backdrop, vectorResource(Res.drawable.refresh_24px), stringResource(Res.string.a11y_refresh), backdropAlpha = a, shadowAlpha = s) },
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
            if (state.isInitializing && state.balance == null) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (configuring) {
                item { SmallTitle(stringResource(Res.string.electricity_section_auth)) }
                item {
                    Button(
                        onClick = viewModel::loginAndLoadCampuses,
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
                    item { LocationPreference(stringResource(Res.string.label_campus), state.campuses, state.selectedCampus, viewModel::selectCampus, viewModel::loadCampuses, state.isLoadingLocations) }
                    item { LocationPreference(stringResource(Res.string.label_building), state.buildings, state.selectedBuilding, viewModel::selectBuilding, loading = state.isLoadingLocations) }
                    item { LocationPreference(stringResource(Res.string.label_room), state.rooms, state.selectedRoom, viewModel::selectRoom, loading = state.isLoadingLocations) }
                }
                item {
                    Button(
                        onClick = viewModel::saveAndQuery,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading && state.isLoggedIn && state.selectedRoom != null,
                    ) { Text(stringResource(Res.string.action_save_query)) }
                }
            }
            state.balance?.let { balance ->
                item { BalanceCard(balance, room, state.lastUpdated) }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button({ viewModel.refresh() }, Modifier.weight(1f), enabled = !state.isLoading) { Text(stringResource(Res.string.electricity_action_refresh)) }
                        Button(viewModel::editConfiguration, Modifier.weight(1f)) { Text(stringResource(Res.string.action_edit_room)) }
                    }
                }
                item { SmallTitle(stringResource(Res.string.electricity_section_history)) }
                item { StatsCard(state.history) }
                item { TrendCard(state.history) }
                item { TextButton(stringResource(Res.string.electricity_action_clear), viewModel::clearConfiguration, Modifier.fillMaxWidth()) }
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
}

@Composable
private fun LocationPreference(
    title: String,
    items: List<ElectricityLocation>,
    selected: ElectricityLocation?,
    onSelect: (ElectricityLocation) -> Unit,
    onOpen: (() -> Unit)? = null,
    loading: Boolean = false,
) {
    LaunchedEffect(items.isEmpty(), selected?.value) {
        if (items.isEmpty() && selected == null) onOpen?.invoke()
    }
    WindowDropdownPreference(
        items = items.map(ElectricityLocation::name),
        selectedIndex = selected?.let(items::indexOf)?.takeIf { it >= 0 } ?: 0,
        title = title,
        summary = when {
            loading -> stringResource(Res.string.electricity_loading_prompt, title)
            selected != null -> selected.name
            items.isEmpty() -> stringResource(Res.string.electricity_select_room_hint)
            else -> stringResource(Res.string.electricity_select_room_prompt, title)
        },
        modifier = Modifier.fillMaxWidth(),
        onSelectedIndexChange = { index -> items.getOrNull(index)?.let(onSelect) },
    )
}

@Composable
private fun BalanceCard(balance: Double, room: String, updated: Long?) {
    val color = when { balance < 10 -> MiuixTheme.colorScheme.error; balance < 30 -> Color(0xFFB26A00); else -> MiuixTheme.colorScheme.primary }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(stringResource(Res.string.electricity_current_balance), style = MiuixTheme.textStyles.body1)
            Text(stringResource(Res.string.electricity_unit_degree, "%.2f".format(balance)), style = MiuixTheme.textStyles.headline1.copy(fontSize = 38.sp, fontWeight = FontWeight.Bold), color = color, modifier = Modifier.padding(top = 4.dp))
            Text(if (balance < 10) stringResource(Res.string.electricity_balance_low) else stringResource(Res.string.electricity_balance_normal), color = color, style = MiuixTheme.textStyles.footnote1)
            Text(if (room.isBlank()) stringResource(Res.string.electricity_no_room) else room, modifier = Modifier.padding(top = 14.dp))
            Text(stringResource(Res.string.electricity_updated_at, updated?.let(::formatTime) ?: "--"), style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        }
    }
}

@Composable private fun StatsCard(history: List<Pair<Long, Double>>) { val latest = history.firstOrNull(); val previous = history.getOrNull(1); Card(Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) { Stat(stringResource(Res.string.electricity_stat_queries), history.size.toString()); Stat(stringResource(Res.string.electricity_stat_recent_change), if (latest != null && previous != null) stringResource(Res.string.electricity_unit_degree, "%+.2f".format(latest.second - previous.second)) else stringResource(Res.string.electricity_insufficient_data)); Stat(stringResource(Res.string.electricity_stat_recent_record), latest?.first?.let(::formatTime) ?: stringResource(Res.string.electricity_none)) } } }
@Composable private fun Stat(label: String, value: String) { Column { Text(label, style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary); Text(value, style = MiuixTheme.textStyles.body2, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp)) } }
@Composable private fun TrendCard(history: List<Pair<Long, Double>>) { var range by remember { mutableStateOf(ElectricityTrendRange.DAYS_7) }; val points = history.withoutConsecutiveDuplicates().forRange(range); Card(Modifier.fillMaxWidth()) { Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text(stringResource(Res.string.electricity_section_trend), style = MiuixTheme.textStyles.title3); BoxWithConstraints(Modifier.fillMaxWidth().border(1.dp, MiuixTheme.colorScheme.dividerLine, RoundedCornerShape(10.dp)).padding(3.dp)) { val offset by animateDpAsState(if (range.ordinal == 1) maxWidth / 2 else 0.dp, tween(220), label = "trend-range"); Box(Modifier.width(maxWidth / 2).offset(x = offset).background(MiuixTheme.colorScheme.primaryVariant, RoundedCornerShape(8.dp)).height(36.dp)); Row(Modifier.fillMaxWidth()) { ElectricityTrendRange.entries.forEach { option -> Box(Modifier.weight(1f).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { range = option }.height(36.dp), contentAlignment = Alignment.Center) { Text(stringResource(option.labelRes), color = if (range == option) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurfaceVariantSummary) } } } }; if (points.size < 2) Text(stringResource(Res.string.electricity_trend_need_two), style = MiuixTheme.textStyles.footnote1, color = MiuixTheme.colorScheme.onSurfaceVariantSummary) else ElectricityLineChart(points, MiuixTheme.colorScheme.primary) } } }
@Composable private fun ElectricityLineChart(points: List<ElectricityTrendPoint>, color: Color) { var selectedIndex by remember(points) { mutableStateOf(points.lastIndex) }; val min = points.minOf { it.balance }; val max = points.maxOf { it.balance }; val selected = points.getOrNull(selectedIndex); Column(Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.width(52.dp)) { Text("%.2f".format(max), style = MiuixTheme.textStyles.footnote1); Text("%.2f".format((max + min) / 2), style = MiuixTheme.textStyles.footnote1, modifier = Modifier.padding(vertical = 55.dp)); Text("%.2f".format(min), style = MiuixTheme.textStyles.footnote1) }; Canvas(Modifier.weight(1f).height(190.dp).pointerInput(points) { detectTapGestures { offset -> selectedIndex = ((offset.x / size.width) * points.size).toInt().coerceIn(0, points.lastIndex) } }) { val top = 10.dp.toPx(); val bottom = size.height - 12.dp.toPx(); val left = 2.dp.toPx(); val right = size.width - 2.dp.toPx(); val range = (max - min).coerceAtLeast(0.01); listOf(top, (top + bottom) / 2f, bottom).forEach { y -> drawLine(color.copy(alpha = .18f), androidx.compose.ui.geometry.Offset(left, y), androidx.compose.ui.geometry.Offset(right, y), 1.dp.toPx()) }; drawLine(color.copy(alpha = .55f), androidx.compose.ui.geometry.Offset(left, top), androidx.compose.ui.geometry.Offset(left, bottom), 1.dp.toPx()); drawLine(color.copy(alpha = .55f), androidx.compose.ui.geometry.Offset(left, bottom), androidx.compose.ui.geometry.Offset(right, bottom), 1.dp.toPx()); val path = androidx.compose.ui.graphics.Path(); points.forEachIndexed { index, point -> val x = if (points.size == 1) left else left + (right - left) * index / (points.size - 1); val y = bottom - ((point.balance - min) / range * (bottom - top)).toFloat(); if (index == 0) path.moveTo(x, y) else path.lineTo(x, y); drawCircle(color, 4.dp.toPx(), androidx.compose.ui.geometry.Offset(x, y)); if (index == selectedIndex) { drawCircle(color, 8.dp.toPx(), androidx.compose.ui.geometry.Offset(x, y), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())); drawLine(color.copy(alpha = .45f), androidx.compose.ui.geometry.Offset(x, top), androidx.compose.ui.geometry.Offset(x, bottom), 1.dp.toPx()) } }; drawPath(path, color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())) } }; Row(Modifier.fillMaxWidth().padding(start = 52.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(formatTrendDate(points.first().time), style = MiuixTheme.textStyles.footnote1); if (points.size > 2) Text(formatTrendDate(points[points.size / 2].time), style = MiuixTheme.textStyles.footnote1); Text(formatTrendDate(points.last().time), style = MiuixTheme.textStyles.footnote1) }; selected?.let { Text(formatTrendDate(it.time) + "  " + stringResource(Res.string.electricity_unit_degree, "%.2f".format(it.balance)), style = MiuixTheme.textStyles.footnote1, color = color, modifier = Modifier.padding(start = 52.dp, top = 6.dp)) } } }
private fun formatTrendDate(time: Long): String { val date = Instant.fromEpochMilliseconds(time).toLocalDateTime(TimeZone.currentSystemDefault()); return "%02d/%02d".format(date.month.number, date.day) }
private fun formatTime(ms: Long): String { val time = Instant.fromEpochMilliseconds(ms).toLocalDateTime(TimeZone.currentSystemDefault()); return "%04d-%02d-%02d %02d:%02d".format(time.year, time.month.number, time.day, time.hour, time.minute) }
