package com.wild0408.nanxinshiguang.ui.material.service.bus

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wild0408.nanxinshiguang.data.api.bus.BusSnapshot
import com.wild0408.nanxinshiguang.data.api.bus.BusVehicle
import com.wild0408.nanxinshiguang.data.api.bus.createBusRepository
import com.wild0408.nanxinshiguang.data.api.bus.projectBusCoordinate
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.bus_campus_map
import nanxinshiguang.generated.resources.bus_station
import nanxinshiguang.generated.resources.bus_vehicle_offline
import nanxinshiguang.generated.resources.bus_vehicle_online
import nanxinshiguang.generated.resources.arrow_back_24px
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

private data class BusStation(val name: String, val longitude: Double, val latitude: Double)

private val stations = listOf(
    BusStation("行政楼南", 118.724220, 32.203514), BusStation("行政楼北", 118.724214, 32.206448),
    BusStation("气象楼站", 118.722460, 32.204662), BusStation("文德楼站", 118.720797, 32.204471),
    BusStation("明德楼站", 118.718598, 32.204233), BusStation("中苑老食堂", 118.716371, 32.202204),
    BusStation("图书馆站", 118.713603, 32.201820), BusStation("逸夫楼站", 118.711286, 32.201121),
    BusStation("滨江楼站", 118.708732, 32.199900), BusStation("滨江食堂", 118.706329, 32.203364),
)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun BusMapScreen(onBack: () -> Unit) {
    Scaffold(topBar = { CenterAlignedTopAppBar(title = { Text("校园公交") }, navigationIcon = {
        IconButton(onClick = onBack) { androidx.compose.material3.Icon(org.jetbrains.compose.resources.vectorResource(Res.drawable.arrow_back_24px), contentDescription = "返回") }
    }) }) { padding ->
        BusMapBody(Modifier.padding(padding), onBack)
    }
}

@Composable
fun BusMapBody(modifier: Modifier = Modifier, onBack: (() -> Unit)? = null) {
    BusMapBody(modifier, onBack, refreshRequest = 0)
}

@Composable
fun BusMapBody(modifier: Modifier = Modifier, onBack: (() -> Unit)? = null, refreshRequest: Int = 0, showStatusCard: Boolean = true) {
    val repository = remember { runCatching { createBusRepository() }.getOrNull() }
    var snapshot by remember { mutableStateOf<BusSnapshot?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var selectedVehicle by remember { mutableStateOf<BusVehicle?>(null) }
    var selectedStation by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var scale by remember { mutableFloatStateOf(1f) }
    var translation by remember { mutableStateOf(Offset.Zero) }

    suspend fun refresh() {
        loading = true
        val source = repository ?: error("公交网络模块初始化失败")
        runCatching { source.fetchSnapshot() }
            .onSuccess { snapshot = it; error = null }
            .onFailure { error = it.message ?: "公交数据读取失败" }
        loading = false
    }

    LaunchedEffect(repository) {
        while (isActive) {
            refresh()
            delay(5_000)
        }
    }
    LaunchedEffect(refreshRequest) {
        if (refreshRequest > 0) refresh()
    }
    BoxWithConstraints(modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxSize()) {
            if (showStatusCard) Card(
                Modifier.fillMaxWidth().padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("校园公交", style = MaterialTheme.typography.titleMedium)
                        val online = snapshot?.vehicles?.count { it.online } ?: 0
                        Text(if (loading && snapshot == null) "正在连接车辆平台…" else "在线车辆 $online / ${snapshot?.vehicles?.size ?: 0}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { scope.launch { refresh() } }) { Text("刷新") }
                }
            }
            BoxWithConstraints(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 8.dp).clip(RoundedCornerShape(18.dp))
                        .transformable(rememberTransformableState { zoom, pan, _ ->
                            scale = (scale * zoom).coerceIn(1f, 4f)
                            translation += pan
                        })
                        .pointerInput(Unit) { detectTapGestures(onDoubleTap = { scale = 1f; translation = Offset.Zero }) }) {
                // Fit the rotated map into the viewport. Only the image scales;
                // markers use the same projected positions in screen coordinates.
                val mapWidth = minOf(maxWidth, maxHeight * (999f / 1830f))
                val mapHeight = mapWidth * (1830f / 999f)
                Box(Modifier.size(mapWidth, mapHeight).align(Alignment.Center)) {
                    Image(painterResource(Res.drawable.bus_campus_map), null,
                        Modifier.requiredSize(mapHeight, mapWidth).align(Alignment.Center)
                            .graphicsLayer {
                                rotationZ = -90f
                                scaleX = scale
                                scaleY = scale
                                translationX = translation.x
                                translationY = translation.y
                            }, contentScale = ContentScale.FillBounds)
                    stations.forEach { station ->
                        val (x, y) = projectBusCoordinate(station.longitude, station.latitude)
                        MapMarker(y, 1f - x, mapWidth, mapHeight, scale, translation, painterResource(Res.drawable.bus_station), station.name, markerSize = 20.dp, iconSize = 15.dp, showLabel = true) { selectedStation = station.name }
                    }
                    snapshot?.vehicles?.forEach { vehicle ->
                        val (x, y) = projectBusCoordinate(vehicle.longitude, vehicle.latitude)
                        MapMarker(y, 1f - x, mapWidth, mapHeight, scale, translation, painterResource(if (vehicle.online) Res.drawable.bus_vehicle_online else Res.drawable.bus_vehicle_offline), vehicle.id, markerSize = 28.dp, iconSize = 22.dp) { selectedVehicle = vehicle }
                    }
                }
                if (!showStatusCard) {
                    val online = snapshot?.vehicles?.count { it.online } ?: 0
                    Text(
                        if (loading && snapshot == null) "正在连接…" else "在线车辆 $online",
                        Modifier.align(Alignment.TopEnd).padding(top = 64.dp, end = 10.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = .82f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                    )
                }
                if (loading && snapshot == null) CircularProgressIndicator(Modifier.align(Alignment.Center))
                error?.let { message ->
                    Card(Modifier.align(Alignment.BottomCenter).padding(12.dp)) { Text(message, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error) }
                }
            }
        }
    }

    selectedVehicle?.let { vehicle ->
        AlertDialog(onDismissRequest = { selectedVehicle = null }, confirmButton = { Button({ selectedVehicle = null }) { Text("关闭") } }, title = { Text(vehicle.id) }, text = { Text("状态：${if (vehicle.online) "在线" else "离线"}\n最后上报：${vehicle.gpsTime ?: "未知"}\n速度：${vehicle.speed ?: 0}  航向：${vehicle.heading.roundToInt()}°") })
    }
    selectedStation?.let { station ->
        AlertDialog(onDismissRequest = { selectedStation = null }, confirmButton = { Button({ selectedStation = null }) { Text("关闭") } }, title = { Text("公交站") }, text = { Text(station) })
    }
}

@Composable
private fun MapMarker(x: Float, y: Float, width: androidx.compose.ui.unit.Dp, height: androidx.compose.ui.unit.Dp, scale: Float, translation: Offset, painter: androidx.compose.ui.graphics.painter.Painter, label: String, markerSize: androidx.compose.ui.unit.Dp, iconSize: androidx.compose.ui.unit.Dp, showLabel: Boolean = false, onClick: () -> Unit) {
    val density = LocalDensity.current
    Row(Modifier.offset {
        val w = with(density) { width.toPx() }
        val h = with(density) { height.toPx() }
        val radius = with(density) { (markerSize / 2).toPx() }
        IntOffset((w / 2 + (x - .5f) * w * scale + translation.x - radius).roundToInt(),
            (h / 2 + (y - .5f) * h * scale + translation.y - radius).roundToInt())
    }.pointerInput(label) { detectTapGestures { onClick() } }, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(markerSize).clip(CircleShape).background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center) {
            Image(painter, label, Modifier.size(iconSize), contentScale = ContentScale.Fit)
        }
        if (showLabel) Text(label, Modifier.padding(start = 3.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .96f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, maxLines = 1)
    }
}
