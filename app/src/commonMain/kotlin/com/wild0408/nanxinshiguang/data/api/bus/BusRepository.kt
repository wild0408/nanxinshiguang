package com.wild0408.nanxinshiguang.data.api.bus

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
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

    suspend fun fetchSnapshot(): BusSnapshot {
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

fun projectBusCoordinate(longitude: Double, latitude: Double): Pair<Float, Float> {
    val x = ((longitude - 118.704857) / (118.728726 - 118.704857)).toFloat()
    val y = (1.0 - (latitude - 32.197464) / (32.208535 - 32.197464)).toFloat()
    return x.coerceIn(0f, 1f) to y.coerceIn(0f, 1f)
}
