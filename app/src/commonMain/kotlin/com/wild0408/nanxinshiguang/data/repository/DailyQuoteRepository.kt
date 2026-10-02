package com.wild0408.nanxinshiguang.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.wild0408.nanxinshiguang.data.api.quote.DailyQuoteApi
import com.wild0408.nanxinshiguang.data.model.DailyQuote
import com.wild0408.nanxinshiguang.tool.AppLog
import kotlinx.coroutines.flow.first
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import kotlin.time.Clock

private const val TAG = "DailyQuote"

/**
 * 「每日一言」的取值与缓存。
 *
 * 规则：
 * - 同一天内进入页面复用缓存，保证"每日一言"当天稳定；
 * - 点击卡片强制重新拉取，并把新内容写入当天缓存（用户当天看到的就是这一条）；
 * - 网络失败时保留已有缓存；从未成功获取过则返回 null，界面据此不渲染卡片。
 */
@Single
class DailyQuoteRepository(
    @Named("DailyQuote") private val dataStore: DataStore<Preferences>,
    private val api: DailyQuoteApi,
) {

    private companion object {
        val KEY_TEXT = stringPreferencesKey("quote_text")
        val KEY_SOURCE = stringPreferencesKey("quote_source")
        val KEY_DAY = longPreferencesKey("quote_epoch_day")
    }

    suspend fun current(): DailyQuote? {
        val prefs = dataStore.data.first()
        val cached = prefs.toQuote()
        val today = todayEpochDay()
        if (cached != null && prefs[KEY_DAY] == today) return cached
        return refresh() ?: cached
    }

    suspend fun refresh(): DailyQuote? {
        val fetched = runCatching { api.fetch() }.getOrElse { error ->
            AppLog.e(TAG, "拉取每日一言失败: ${error::class.simpleName}")
            null
        } ?: return dataStore.data.first().toQuote()

        dataStore.edit { prefs ->
            prefs[KEY_TEXT] = fetched.text
            prefs[KEY_SOURCE] = fetched.source.orEmpty()
            prefs[KEY_DAY] = todayEpochDay()
        }
        return fetched
    }

    private fun Preferences.toQuote(): DailyQuote? {
        val text = this[KEY_TEXT]?.trim().orEmpty()
        if (text.isEmpty()) return null
        return DailyQuote(text = text, source = this[KEY_SOURCE]?.trim()?.takeIf { it.isNotEmpty() })
    }

    private fun todayEpochDay(): Long =
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toEpochDays().toLong()
}
