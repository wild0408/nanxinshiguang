package com.wild0408.nanxinshiguang.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.wild0408.nanxinshiguang.data.model.AcademicSummary
import com.wild0408.nanxinshiguang.tool.SecureCrypto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

interface AcademicSummaryRepository {
    val summary: StateFlow<AcademicSummary?>
    suspend fun load()
    suspend fun save(value: AcademicSummary)
    suspend fun clear()
}

@Single
class DataStoreAcademicSummaryRepository(
    @Named("AcademicSummary") private val store: DataStore<Preferences>,
    private val secureCrypto: SecureCrypto,
) : AcademicSummaryRepository {
    private val _summary = MutableStateFlow<AcademicSummary?>(null)
    override val summary: StateFlow<AcademicSummary?> = _summary.asStateFlow()
    private val encryptedKey = stringPreferencesKey("summary_ciphertext")
    private val ivKey = stringPreferencesKey("summary_iv")
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun load() {
        val saved = store.data.first()
        _summary.value = saved[encryptedKey]?.let { ciphertext ->
            saved[ivKey]?.let { iv ->
                secureCrypto.decrypt(ciphertext, iv)?.let {
                    runCatching { json.decodeFromString<AcademicSummary>(it) }.getOrNull()
                }
            }
        }
    }

    override suspend fun save(value: AcademicSummary) {
        val encrypted = secureCrypto.encrypt(json.encodeToString(value))
            ?: error("学业概览缓存加密失败")
        store.edit {
            it[encryptedKey] = encrypted.encryptedData
            it[ivKey] = encrypted.iv
        }
        _summary.value = value
    }

    override suspend fun clear() {
        store.edit {
            it.remove(encryptedKey)
            it.remove(ivKey)
        }
        _summary.value = null
    }
}
