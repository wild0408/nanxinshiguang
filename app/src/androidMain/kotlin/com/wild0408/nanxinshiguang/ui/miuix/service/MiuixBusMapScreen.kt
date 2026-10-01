package com.wild0408.nanxinshiguang.ui.miuix.service

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.wild0408.nanxinshiguang.data.api.bus.BusRepository
import com.wild0408.nanxinshiguang.data.api.bus.BusVehicle
import com.wild0408.nanxinshiguang.ui.components.LocalNavigationHostPadding
import com.wild0408.nanxinshiguang.ui.components.bus.BusMapPalette
import com.wild0408.nanxinshiguang.ui.components.bus.BusMapState
import com.wild0408.nanxinshiguang.ui.components.bus.BusMapViewport
import com.wild0408.nanxinshiguang.ui.components.bus.BusMapViewportState
import com.wild0408.nanxinshiguang.ui.components.bus.formatBusNumber
import com.wild0408.nanxinshiguang.ui.components.bus.formatBusTime
import com.wild0408.nanxinshiguang.ui.components.bus.rememberBusMapState
import com.wild0408.nanxinshiguang.ui.components.bus.rememberBusMapViewportState
import com.wild0408.nanxinshiguang.ui.miuix.hyper.basic.HyperLiquidTopBarButton
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import kotlin.math.roundToInt
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.add_24px
import nanxinshiguang.generated.resources.bus_close
import nanxinshiguang.generated.resources.bus_connecting
import nanxinshiguang.generated.resources.bus_map_title
import nanxinshiguang.generated.resources.bus_online_count
import nanxinshiguang.generated.resources.bus_refresh
import nanxinshiguang.generated.resources.bus_reset_view
import nanxinshiguang.generated.resources.bus_unknown
import nanxinshiguang.generated.resources.bus_updated_at
import nanxinshiguang.generated.resources.bus_vehicle_heading
import nanxinshiguang.generated.resources.bus_vehicle_heading_value
import nanxinshiguang.generated.resources.bus_vehicle_last_report
import nanxinshiguang.generated.resources.bus_vehicle_offline
import nanxinshiguang.generated.resources.bus_vehicle_online
import nanxinshiguang.generated.resources.bus_vehicle_speed
import nanxinshiguang.generated.resources.bus_vehicle_speed_value
import nanxinshiguang.generated.resources.bus_vehicle_status
import nanxinshiguang.generated.resources.bus_vehicle_title
import nanxinshiguang.generated.resources.bus_zoom_in
import nanxinshiguang.generated.resources.bus_zoom_out
import nanxinshiguang.generated.resources.fit_screen_24px
import nanxinshiguang.generated.resources.refresh_24px
import nanxinshiguang.generated.resources.remove_24px
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
internal fun MiuixBusMapScreen(onBack: () -> Unit) {
    val repository = koinInject<BusRepository>()
    var refreshRequest by remember { mutableIntStateOf(0) }
    val state = rememberBusMapState(repository, refreshRequest)
    val viewportState = rememberBusMapViewportState()
    var selectedVehicle by remember { mutableStateOf<BusVehicle?>(null) }

    val background = MiuixTheme.colorScheme.surface
    val hostPadding = LocalNavigationHostPadding.current
    val pageTitle = stringResource(Res.string.bus_map_title)
    val refreshLabel = stringResource(Res.string.bus_refresh)

    // 底栏与浮动控件不使用同一个 Backdrop：把按钮自身内容录进 Backdrop 会在部分
    // HyperOS 设备上造成 RenderNode 循环，导致 RenderThread SIGSEGV。
    val backdrop = rememberLayerBackdrop { drawRect(background) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = background,
    ) { padding ->
        Box(Modifier.fillMaxSize().background(background)) {
            BusMapViewport(
                state = viewportState,
                vehicles = state.snapshot?.vehicles.orEmpty(),
                palette = miuixBusPalette(),
                labelTextStyle = MiuixTheme.textStyles.footnote2,
                onVehicleClick = { selectedVehicle = it },
                modifier = Modifier.fillMaxSize().padding(padding),
            )

            Box(Modifier.statusBarsPadding().fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp)) {
                HyperLiquidTopBarButton(
                    onClick = onBack,
                    backdrop = backdrop,
                    icon = MiuixIcons.ChevronBackward,
                    contentDescription = pageTitle,
                    modifier = Modifier.align(Alignment.TopStart),
                    backdropAlpha = 1f,
                    shadowAlpha = 1f,
                )
                Box(
                    modifier = Modifier.height(48.dp).wrapContentWidth().align(Alignment.TopCenter),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        pageTitle,
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.onSurfaceContainer,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(MiuixTheme.colorScheme.surfaceContainer.copy(alpha = .92f))
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                    )
                }
                HyperLiquidTopBarButton(
                    onClick = { refreshRequest++ },
                    backdrop = backdrop,
                    icon = vectorResource(Res.drawable.refresh_24px),
                    contentDescription = refreshLabel,
                    modifier = Modifier.align(Alignment.TopEnd),
                    backdropAlpha = 1f,
                    shadowAlpha = 1f,
                )
            }

            MiuixBusStatusChip(
                state = state,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 64.dp, end = 12.dp),
            )

            MiuixBusZoomControls(
                state = viewportState,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = hostPadding.calculateBottomPadding() + 20.dp),
            )

            if (state.loading && state.snapshot == null) {
                CircularProgressIndicator(Modifier.align(Alignment.Center).size(28.dp))
            }
        }
    }

    selectedVehicle?.let { vehicle ->
        WindowDialog(
            show = true,
            title = stringResource(Res.string.bus_vehicle_title, vehicle.id),
            onDismissRequest = { selectedVehicle = null },
            insideMargin = DpSize(16.dp, 16.dp),
        ) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                MiuixBusDetailRow(
                    label = stringResource(Res.string.bus_vehicle_status),
                    value = stringResource(
                        if (vehicle.online) Res.string.bus_vehicle_online else Res.string.bus_vehicle_offline
                    ),
                )
                MiuixBusDetailRow(
                    label = stringResource(Res.string.bus_vehicle_last_report),
                    value = vehicle.gpsTime ?: stringResource(Res.string.bus_unknown),
                )
                MiuixBusDetailRow(
                    label = stringResource(Res.string.bus_vehicle_speed),
                    value = vehicle.speed
                        ?.let { stringResource(Res.string.bus_vehicle_speed_value, formatBusNumber(it)) }
                        ?: stringResource(Res.string.bus_unknown),
                )
                MiuixBusDetailRow(
                    label = stringResource(Res.string.bus_vehicle_heading),
                    value = stringResource(
                        Res.string.bus_vehicle_heading_value,
                        vehicle.heading.roundToInt().toString(),
                    ),
                )
                TextButton(
                    text = stringResource(Res.string.bus_close),
                    onClick = { selectedVehicle = null },
                    modifier = Modifier.fillMaxWidth(),
                    colors = top.yukonga.miuix.kmp.basic.ButtonDefaults.textButtonColorsPrimary(),
                )
            }
        }
    }

}

@Composable
private fun MiuixBusStatusChip(state: BusMapState, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = .92f)),
    ) {
        Column(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                if (state.loading && state.snapshot == null) {
                    stringResource(Res.string.bus_connecting)
                } else {
                    stringResource(Res.string.bus_online_count, state.snapshot?.vehicles?.count { it.online } ?: 0)
                },
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.onSurface,
            )
            state.lastUpdatedMillis?.let { millis ->
                Text(
                    stringResource(Res.string.bus_updated_at, formatBusTime(millis)),
                    style = MiuixTheme.textStyles.footnote2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}

@Composable
private fun MiuixBusZoomControls(state: BusMapViewportState, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.defaultColors(color = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = .92f)),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = state::zoomIn, enabled = state.canZoomIn) {
                Icon(
                    imageVector = vectorResource(Res.drawable.add_24px),
                    contentDescription = stringResource(Res.string.bus_zoom_in),
                    tint = MiuixTheme.colorScheme.onSurface,
                )
            }
            IconButton(onClick = state::zoomOut, enabled = state.canZoomOut) {
                Icon(
                    imageVector = vectorResource(Res.drawable.remove_24px),
                    contentDescription = stringResource(Res.string.bus_zoom_out),
                    tint = MiuixTheme.colorScheme.onSurface,
                )
            }
            IconButton(onClick = state::reset, enabled = state.canReset) {
                Icon(
                    imageVector = vectorResource(Res.drawable.fit_screen_24px),
                    contentDescription = stringResource(Res.string.bus_reset_view),
                    tint = MiuixTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun MiuixBusDetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            Modifier.weight(1f),
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
        Text(
            value,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun miuixBusPalette(): BusMapPalette = BusMapPalette(
    mapBackground = MiuixTheme.colorScheme.surface,
    stationBackground = MiuixTheme.colorScheme.surface,
    stationTint = MiuixTheme.colorScheme.primary,
    vehicleOnlineBackground = MiuixTheme.colorScheme.primary,
    vehicleOnlineTint = MiuixTheme.colorScheme.onPrimary,
    vehicleOfflineBackground = MiuixTheme.colorScheme.surfaceContainerHigh,
    vehicleOfflineTint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
    labelBackground = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = .96f),
    labelText = MiuixTheme.colorScheme.onSurfaceContainer,
)
