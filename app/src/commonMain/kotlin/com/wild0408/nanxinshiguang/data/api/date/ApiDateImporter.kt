package com.wild0408.nanxinshiguang.data.api.date

import com.wild0408.nanxinshiguang.data.repository.AppSettingsRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import com.wild0408.nanxinshiguang.tool.AppLog

private const val TAG = "ApiDateImporter"

@Serializable
data class ApiResponse(
    @SerialName("holiday")
    val holidays: Map<String, HolidayInfo>
)

@Serializable
data class HolidayInfo(
    @SerialName("date")
    val date: String,
    @SerialName("holiday")
    val isHoliday: Boolean
)

/**
 * API 导入对象，基于 Ktor 3.0 实现。
 */
object ApiDateImporter {
    private const val BASE_URL = "https://timor.tech/api/holiday/year"

    private val client = HttpClient {
        install(Logging) {
            level = LogLevel.INFO
            logger = Logger.DEFAULT
        }

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }

        defaultRequest {
            url(BASE_URL)
            header("User-Agent", "Mozilla/5.0 (Linux; Android 10; SM-G973F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36")
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 15000
        }
    }

    /**
     * 从 API 获取跳过的日期（假期），并保存到 AppSettingsRepository 中。
     */
    suspend fun importAndSaveSkippedDates(appSettingsRepository: AppSettingsRepository) {
        try {
            val response: ApiResponse = client.get("").body()

            val skippedDates = response.holidays.values
                .filter { it.isHoliday }
                .map { it.date }
                .toSet()

            val currentSettings = appSettingsRepository.getAppSettings().first()
            val updatedSettings = currentSettings.copy(skippedDates = skippedDates)
            appSettingsRepository.insertOrUpdateAppSettings(updatedSettings)

            AppLog.d(TAG, "已导入并保存跳过日期")
        } catch (e: Exception) {
            AppLog.e(TAG, "导入节假日数据失败", e)
        }
    }

    fun close() = client.close()
}
