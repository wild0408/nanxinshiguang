package com.wild0408.nanxinshiguang.data.api.quote

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

actual fun createDailyQuoteApi(): DailyQuoteApi = DailyQuoteApi(
    HttpClient(CIO) {
        install(ContentNegotiation) { json(DailyQuoteApi.json) }
        install(HttpTimeout) {
            connectTimeoutMillis = 10_000
            requestTimeoutMillis = 15_000
            socketTimeoutMillis = 15_000
        }
    }
)
