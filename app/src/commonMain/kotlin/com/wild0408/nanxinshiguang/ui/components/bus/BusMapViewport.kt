package com.wild0408.nanxinshiguang.ui.components.bus

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.wild0408.nanxinshiguang.data.api.bus.BusVehicle
import com.wild0408.nanxinshiguang.data.api.bus.projectBusCoordinate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import nanxinshiguang.generated.resources.Res
import nanxinshiguang.generated.resources.bus_campus_map
import nanxinshiguang.generated.resources.bus_station
import nanxinshiguang.generated.resources.bus_vehicle
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt
import kotlin.time.Instant

/** 校园公交站点（与底图标注一致的固定坐标）。 */
data class BusStation(val name: String, val longitude: Double, val latitude: Double)

val busStations: List<BusStation> = listOf(
    BusStation("行政楼南", 118.724220, 32.203514), BusStation("行政楼北", 118.724214, 32.206448),
    BusStation("气象楼站", 118.722460, 32.204662), BusStation("文德楼站", 118.720797, 32.204471),
    BusStation("明德楼站", 118.718598, 32.204233), BusStation("中苑老食堂", 118.716371, 32.202204),
    BusStation("图书馆站", 118.713603, 32.201820), BusStation("逸夫楼站", 118.711286, 32.201121),
    BusStation("滨江楼站", 118.708732, 32.199900), BusStation("滨江食堂", 118.706329, 32.203364),
)

/**
 * 地图配色。Material 与 Miuix 两套界面各自传入自己的主题色，
 * 地图本体只使用这里给出的颜色，不直接依赖任何一方的主题。
 */
@Immutable
data class BusMapPalette(
    val mapBackground: Color,
    val stationBackground: Color,
    val stationTint: Color,
    val vehicleOnlineBackground: Color,
    val vehicleOnlineTint: Color,
    val vehicleOfflineBackground: Color,
    val vehicleOfflineTint: Color,
    val labelBackground: Color,
    val labelText: Color,
)

/** 地图缩放/平移状态；缩放范围 1x–4x，平移被限制在底图范围内，不会把地图拖出屏幕。 */
@Stable
class BusMapViewportState {
    var scale by mutableFloatStateOf(1f)
        private set
    var offset by mutableStateOf(Offset.Zero)
        private set

    private var contentWidth = 0f
    private var contentHeight = 0f

    val canZoomIn: Boolean get() = scale < MAX_SCALE - 0.01f
    val canZoomOut: Boolean get() = scale > MIN_SCALE + 0.01f
    val canReset: Boolean get() = canZoomOut || offset != Offset.Zero

    fun updateContentSize(widthPx: Float, heightPx: Float) {
        if (contentWidth != widthPx || contentHeight != heightPx) {
            contentWidth = widthPx
            contentHeight = heightPx
            offset = clampOffset(offset)
        }
    }

    fun onTransform(zoomChange: Float, panChange: Offset) {
        scale = (scale * zoomChange).coerceIn(MIN_SCALE, MAX_SCALE)
        offset = clampOffset(offset + panChange)
    }

    fun zoomIn() {
        scale = (scale * ZOOM_STEP).coerceIn(MIN_SCALE, MAX_SCALE)
        offset = clampOffset(offset)
    }

    fun zoomOut() {
        scale = (scale / ZOOM_STEP).coerceIn(MIN_SCALE, MAX_SCALE)
        offset = clampOffset(offset)
    }

    fun reset() {
        scale = MIN_SCALE
        offset = Offset.Zero
    }

    private fun clampOffset(value: Offset): Offset {
        if (contentWidth <= 0f || contentHeight <= 0f) return value
        val maxX = contentWidth * (scale - 1f) / 2f
        val maxY = contentHeight * (scale - 1f) / 2f
        return Offset(value.x.coerceIn(-maxX, maxX), value.y.coerceIn(-maxY, maxY))
    }

    private companion object {
        const val MIN_SCALE = 1f
        const val MAX_SCALE = 4f
        const val ZOOM_STEP = 1.5f
    }
}

@Composable
fun rememberBusMapViewportState(): BusMapViewportState = remember { BusMapViewportState() }

/**
 * 校园公交地图本体：底图、站点与车辆标记、缩放平移手势。
 * 只使用 Compose 基础组件与传入的配色/文字样式，Material 与 Miuix 两侧共用。
 */
@Composable
fun BusMapViewport(
    state: BusMapViewportState,
    vehicles: List<BusVehicle>,
    palette: BusMapPalette,
    labelTextStyle: TextStyle,
    onVehicleClick: (BusVehicle) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val mapPainter = painterResource(Res.drawable.bus_campus_map)
    val stationPainter = painterResource(Res.drawable.bus_station)
    val vehiclePainter = painterResource(Res.drawable.bus_vehicle)
    val transformState = rememberTransformableState { _, zoomChange, panChange, _ ->
        state.onTransform(zoomChange, panChange)
    }

    BoxWithConstraints(
        modifier = modifier
            .clipToBounds()
            .transformable(transformState)
            .pointerInput(Unit) { detectTapGestures(onDoubleTap = { state.reset() }) },
    ) {
        // 底图按 999:1830 的竖向比例等比适配，缩放到满足高度或宽度为止。
        val mapWidth = minOf(maxWidth, maxHeight * MAP_ASPECT)
        val mapHeight = mapWidth / MAP_ASPECT
        val mapWidthPx = with(density) { mapWidth.toPx() }
        val mapHeightPx = with(density) { mapHeight.toPx() }
        state.updateContentSize(mapWidthPx, mapHeightPx)

        Box(Modifier.size(mapWidth, mapHeight).align(Alignment.Center)) {
            Image(
                painter = mapPainter,
                contentDescription = null,
                modifier = Modifier
                    .requiredSize(mapHeight, mapWidth)
                    .align(Alignment.Center)
                    .graphicsLayer {
                        rotationZ = -90f
                        scaleX = state.scale
                        scaleY = state.scale
                        translationX = state.offset.x
                        translationY = state.offset.y
                    },
                contentScale = ContentScale.FillBounds,
            )

            busStations.forEach { station ->
                val point = projectBusCoordinate(station.longitude, station.latitude)
                if (!point.inBounds) return@forEach
                MapMarker(
                    x = point.y,
                    y = 1f - point.x,
                    width = mapWidth,
                    height = mapHeight,
                    scale = state.scale,
                    translation = state.offset,
                    markerSize = 22.dp,
                    iconSize = 16.dp,
                    background = palette.stationBackground,
                    tint = palette.stationTint,
                    painter = stationPainter,
                    label = station.name,
                    labelTextStyle = labelTextStyle,
                    labelBackground = palette.labelBackground,
                    labelTextColor = palette.labelText,
                    contentDescription = station.name,
                    onClick = null,
                )
            }

            vehicles.forEach { vehicle ->
                val point = projectBusCoordinate(vehicle.longitude, vehicle.latitude)
                if (!point.inBounds) return@forEach
                MapMarker(
                    x = point.y,
                    y = 1f - point.x,
                    width = mapWidth,
                    height = mapHeight,
                    scale = state.scale,
                    translation = state.offset,
                    markerSize = 30.dp,
                    iconSize = 20.dp,
                    background = if (vehicle.online) palette.vehicleOnlineBackground else palette.vehicleOfflineBackground,
                    tint = if (vehicle.online) palette.vehicleOnlineTint else palette.vehicleOfflineTint,
                    painter = vehiclePainter,
                    label = null,
                    labelTextStyle = labelTextStyle,
                    labelBackground = palette.labelBackground,
                    labelTextColor = palette.labelText,
                    contentDescription = vehicle.id,
                    onClick = { onVehicleClick(vehicle) },
                )
            }
        }
    }
}

@Composable
private fun MapMarker(
    x: Float,
    y: Float,
    width: Dp,
    height: Dp,
    scale: Float,
    translation: Offset,
    markerSize: Dp,
    iconSize: Dp,
    background: Color,
    tint: Color,
    painter: Painter,
    label: String?,
    labelTextStyle: TextStyle,
    labelBackground: Color,
    labelTextColor: Color,
    contentDescription: String,
    onClick: (() -> Unit)?,
) {
    val density = LocalDensity.current
    Row(
        modifier = Modifier
            .offset {
                val w = with(density) { width.toPx() }
                val h = with(density) { height.toPx() }
                val radius = with(density) { (markerSize / 2).toPx() }
                IntOffset(
                    (w / 2 + (x - .5f) * w * scale + translation.x - radius).roundToInt(),
                    (h / 2 + (y - .5f) * h * scale + translation.y - radius).roundToInt(),
                )
            }
            .then(
                if (onClick == null) Modifier
                else Modifier.pointerInput(contentDescription) { detectTapGestures { onClick() } }
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(markerSize).clip(CircleShape).background(background),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                modifier = Modifier.size(iconSize),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(tint),
            )
        }
        if (label != null) {
            BasicText(
                text = label,
                style = labelTextStyle.copy(color = labelTextColor),
                modifier = Modifier
                    .padding(start = 3.dp)
                    .background(labelBackground, RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                maxLines = 1,
            )
        }
    }
}

/** 底图（旋转后）的宽高比。 */
private const val MAP_ASPECT = 999f / 1830f

/** 速度保留一位小数，整数值不显示多余的 .0。 */
fun formatBusNumber(value: Double): String =
    if (value % 1.0 == 0.0) value.toInt().toString() else ((value * 10).roundToInt() / 10.0).toString()

/** 车辆数据时间格式化为 HH:mm:ss。 */
fun formatBusTime(millis: Long): String {
    val time = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.currentSystemDefault())
    return "%02d:%02d:%02d".format(time.hour, time.minute, time.second)
}
