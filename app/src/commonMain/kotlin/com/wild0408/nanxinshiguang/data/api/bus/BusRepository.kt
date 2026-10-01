package com.wild0408.nanxinshiguang.data.api.bus

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class BusVehicle(
    val id: String,
    val online: Boolean,
    val longitude: Double,
    val latitude: Double,
    val heading: Double,
    val speed: Double?,
    val gpsTime: String?,
)

data class BusSnapshot(
    val vehicles: List<BusVehicle>,
    val fetchedAtMillis: Long,
)

/**
 * 经纬度投影到校园底图后的归一化坐标。
 * [inBounds] 为 false 表示该点落在底图范围外，调用方应跳过而不是把它钉在边缘。
 */
data class BusMapPoint(
    val x: Float,
    val y: Float,
    val inBounds: Boolean,
)

class BusRepository(private val client: HttpClient) {
    companion object {
        private const val PAGE_URL = "http://www.ns-res.cn/hydxflat/indexelct.html"
        private const val API_BASE = "http://47.96.16.23:8080"
        // Keep this list synchronized with the public campus page. The page
        // currently monitors NXD5 and NXD6; the previous list omitted both,
        // which made active buses appear as zero online in the app.
        private val DEFAULT_VEHICLES = listOf(
            "NXD1", "NXD2", "NXD3", "NXD5", "NXD6", "NXD7", "NXD8",
            "NXD9", "NXD10", "NXD11", "NXD12", "NXD13",
        )
    }

    private var session: String? = null
    private var vehicleIds: List<String> = DEFAULT_VEHICLES

    /** 轮询与手动刷新可能同时触发，这里串行化，避免同一接口被并发请求、会话被互相覆盖。 */
    private val fetchMutex = Mutex()

    suspend fun fetchSnapshot(): BusSnapshot = fetchMutex.withLock { fetchSnapshotLocked() }

    private suspend fun fetchSnapshotLocked(): BusSnapshot {
        val jsession = session ?: login()
        val response = client.get("$API_BASE/StandardApiAction_getDeviceStatus.action") {
            parameter("jsession", jsession)
            parameter("vehiIdno", vehicleIds.joinToString(","))
            parameter("toMap", "1")
        }
        val root = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        if (root["result"]?.jsonPrimitive?.content?.toIntOrNull() != 0) {
            session = null
            error(root["resultTip"]?.jsonPrimitive?.content ?: "公交接口会话失效")
        }
        val vehicles = root["status"]?.jsonArray.orEmpty().mapNotNull { element ->
            val item = element.jsonObject
            val lon = item["mlng"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: return@mapNotNull null
            val lat = item["mlat"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: return@mapNotNull null
            BusVehicle(
                id = item["vid"]?.jsonPrimitive?.content ?: return@mapNotNull null,
                online = item["ol"]?.jsonPrimitive?.content?.toIntOrNull() == 1,
                longitude = lon,
                latitude = lat,
                heading = item["hx"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0,
                speed = item["sp"]?.jsonPrimitive?.content?.toDoubleOrNull(),
                gpsTime = item["gt"]?.jsonPrimitive?.content,
            )
        }
        return BusSnapshot(vehicles, kotlin.time.Clock.System.now().toEpochMilliseconds())
    }

    private suspend fun login(): String {
        val page = client.get(PAGE_URL).bodyAsText()
        Regex("vehiIdno=([A-Za-z0-9_,-]+)").find(page)?.groupValues?.getOrNull(1)
            ?.split(',')?.filter(String::isNotBlank)?.distinct()
            ?.takeIf { it.isNotEmpty() }?.let { vehicleIds = it }
        val match = Regex("StandardApiAction_login\\.action\\?account=([^&\\\"']+)&password=([^\\\"']+)").find(page)
            ?: error("公交页面未提供登录配置")
        val account = match.groupValues[1]
        val password = match.groupValues[2]
        val root = Json.parseToJsonElement(client.get("$API_BASE/StandardApiAction_login.action") {
            parameter("account", account)
            parameter("password", password)
        }.bodyAsText()).jsonObject
        val token = root["jsession"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
            ?: error(root["resultTip"]?.jsonPrimitive?.content ?: "公交接口登录失败")
        session = token
        return token
    }
}

expect fun createBusRepository(): BusRepository

fun projectBusCoordinate(longitude: Double, latitude: Double): BusMapPoint {
    val rawX = (longitude - LONGITUDE_MIN) / (LONGITUDE_MAX - LONGITUDE_MIN)
    val rawY = 1.0 - (latitude - LATITUDE_MIN) / (LATITUDE_MAX - LATITUDE_MIN)
    val inBounds = rawX in 0.0..1.0 && rawY in 0.0..1.0
    return BusMapPoint(
        x = rawX.coerceIn(0.0, 1.0).toFloat(),
        y = rawY.coerceIn(0.0, 1.0).toFloat(),
        inBounds = inBounds,
    )
}

/** 校园底图覆盖的经纬度范围（与底图四角一致）。 */
const val LONGITUDE_MIN: Double = 118.704857
const val LONGITUDE_MAX: Double = 118.728726
const val LATITUDE_MIN: Double = 32.197464
const val LATITUDE_MAX: Double = 32.208535
