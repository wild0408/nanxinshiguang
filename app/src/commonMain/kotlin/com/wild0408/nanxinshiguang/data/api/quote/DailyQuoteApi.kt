package com.wild0408.nanxinshiguang.data.api.quote

import com.wild0408.nanxinshiguang.data.model.DailyQuote
import com.wild0408.nanxinshiguang.data.model.formatQuoteSource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** 一言接口（v1.hitokoto.cn）返回结构，只取用得到的字段。 */
@Serializable
data class HitokotoResponse(
    @SerialName("hitokoto") val text: String = "",
    @SerialName("from") val from: String? = null,
    @SerialName("from_who") val fromWho: String? = null,
)

/** 转成界面用的模型；正文为空时返回 null。 */
fun HitokotoResponse.toDailyQuote(): DailyQuote? {
    val content = text.trim()
    if (content.isEmpty()) return null
    return DailyQuote(text = content, source = formatQuoteSource(from, fromWho))
}

/**
 * 拉取一言。
 *
 * 分类限定为 `d`(文学) / `i`(诗词) / `k`(哲学)：接口默认分类里包含歌词、网络用语等，
 * 不适合出现在校园应用的首页，因此请求时显式限定分类。
 */
class DailyQuoteApi(private val client: HttpClient) {

    suspend fun fetch(): DailyQuote? {
        val response: HitokotoResponse = client.get(
            "https://v1.hitokoto.cn/?encode=json&charset=utf-8&max_length=40&c=d&c=i&c=k"
        ).body()
        return response.toDailyQuote()
    }

    companion object {
        val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    }
}

/** 各平台提供已配置好的客户端（超时、JSON 反序列化）。 */
expect fun createDailyQuoteApi(): DailyQuoteApi
