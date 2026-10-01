package com.wild0408.nanxinshiguang.ui.material.service.bus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wild0408.nanxinshiguang.data.api.bus.BusRepository
import com.wild0408.nanxinshiguang.data.api.bus.BusVehicle
import com.wild0408.nanxinshiguang.ui.components.bus.BusMapPalette
import com.wild0408.nanxinshiguang.ui.components.bus.BusMapState
import com.wild0408.nanxinshiguang.ui.components.bus.BusMapViewport
import com.wild0408.nanxinshiguang.ui.components.bus.BusMapViewportState
import com.wild0408.nanxinshiguang.ui.components.bus.formatBusNumber
import com.wild0408.nanxinshiguang.ui.components.bus.formatBusTime
import com.wild0408.nanxinshiguang.ui.components.bus.rememberBusMapState
import com.wild0408.nanxinshiguang.ui.components.bus.rememberBusMapViewportState
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.koin.compose.koinInject
import kotlin.math.roundToInt
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.a11y_back
import nanxinshiguang.generated.resources.add_24px
import nanxinshiguang.generated.resources.arrow_back_24px
import nanxinshiguang.generated.resources.bus_close
import nanxinshiguang.generated.resources.bus_connecting
import nanxinshiguang.generated.resources.bus_load_failed
import nanxinshiguang.generated.resources.bus_map_title
import nanxinshiguang.generated.resources.bus_online_of_total
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
import nanxinshiguang.generated.resources.remove_24px

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusMapScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(Res.string.bus_map_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            vectorResource(Res.drawable.arrow_back_24px),
                            contentDescription = stringResource(Res.string.a11y_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        BusMapBody(Modifier.padding(padding))
    }
}

@Composable
fun BusMapBody(
    modifier: Modifier = Modifier,
    refreshRequest: Int = 0,
    showStatusCard: Boolean = true,
) {
    val repository = koinInject<BusRepository>()
    val state = rememberBusMapState(repository, refreshRequest)
    val viewportState = rememberBusMapViewportState()
    var selectedVehicle by remember { mutableStateOf<BusVehicle?>(null) }

    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        if (showStatusCard) {
            BusStatusCard(state)
        }
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(18.dp)),
        ) {
            BusMapViewport(
                state = viewportState,
                vehicles = state.snapshot?.vehicles.orEmpty(),
                palette = materialBusPalette(),
                labelTextStyle = MaterialTheme.typography.labelSmall,
                onVehicleClick = { selectedVehicle = it },
                modifier = Modifier.fillMaxSize(),
            )
            BusZoomControls(viewportState, Modifier.align(Alignment.BottomEnd).padding(12.dp))
            if (state.loading && state.snapshot == null) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
            state.error?.let { message ->
                Card(Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            message.ifBlank { stringResource(Res.string.bus_load_failed) },
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                        TextButton(onClick = state::refresh) { Text(stringResource(Res.string.bus_refresh)) }
                    }
                }
            }
        }
    }

    selectedVehicle?.let { vehicle ->
        AlertDialog(
            onDismissRequest = { selectedVehicle = null },
            confirmButton = { TextButton({ selectedVehicle = null }) { Text(stringResource(Res.string.bus_close)) } },
            title = { Text(stringResource(Res.string.bus_vehicle_title, vehicle.id)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    BusDetailRow(
                        label = stringResource(Res.string.bus_vehicle_status),
                        value = stringResource(
                            if (vehicle.online) Res.string.bus_vehicle_online else Res.string.bus_vehicle_offline
                        ),
                    )
                    BusDetailRow(
                        label = stringResource(Res.string.bus_vehicle_last_report),
                        value = vehicle.gpsTime ?: stringResource(Res.string.bus_unknown),
                    )
                    BusDetailRow(
                        label = stringResource(Res.string.bus_vehicle_speed),
                        value = vehicle.speed
                            ?.let { stringResource(Res.string.bus_vehicle_speed_value, formatBusNumber(it)) }
                            ?: stringResource(Res.string.bus_unknown),
                    )
                    BusDetailRow(
                        label = stringResource(Res.string.bus_vehicle_heading),
                        value = stringResource(
                            Res.string.bus_vehicle_heading_value,
                            vehicle.heading.roundToInt().toString(),
                        ),
                    )
                }
            },
        )
    }
}

@Composable
private fun BusStatusCard(state: BusMapState) {
    Card(
        Modifier.fillMaxWidth().padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                val vehicles = state.snapshot?.vehicles.orEmpty()
                Text(
                    if (state.loading && state.snapshot == null) {
                        stringResource(Res.string.bus_connecting)
                    } else {
                        stringResource(
                            Res.string.bus_online_of_total,
                            vehicles.count { it.online },
                            vehicles.size,
                        )
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                state.lastUpdatedMillis?.let { millis ->
                    Text(
                        stringResource(Res.string.bus_updated_at, formatBusTime(millis)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            TextButton(onClick = state::refresh) { Text(stringResource(Res.string.bus_refresh)) }
        }
    }
}

@Composable
private fun BusZoomControls(state: BusMapViewportState, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = state::zoomIn, enabled = state.canZoomIn) {
                Icon(vectorResource(Res.drawable.add_24px), contentDescription = stringResource(Res.string.bus_zoom_in))
            }
            IconButton(onClick = state::zoomOut, enabled = state.canZoomOut) {
                Icon(vectorResource(Res.drawable.remove_24px), contentDescription = stringResource(Res.string.bus_zoom_out))
            }
            IconButton(onClick = state::reset, enabled = state.canReset) {
                Icon(vectorResource(Res.drawable.fit_screen_24px), contentDescription = stringResource(Res.string.bus_reset_view))
            }
        }
    }
}

@Composable
private fun BusDetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.End)
    }
}

@Composable
private fun materialBusPalette(): BusMapPalette = BusMapPalette(
    mapBackground = MaterialTheme.colorScheme.surface,
    stationBackground = MaterialTheme.colorScheme.surface,
    stationTint = MaterialTheme.colorScheme.primary,
    vehicleOnlineBackground = MaterialTheme.colorScheme.primary,
    vehicleOnlineTint = MaterialTheme.colorScheme.onPrimary,
    vehicleOfflineBackground = MaterialTheme.colorScheme.surfaceVariant,
    vehicleOfflineTint = MaterialTheme.colorScheme.onSurfaceVariant,
    labelBackground = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .96f),
    labelText = MaterialTheme.colorScheme.onSurfaceVariant,
)
