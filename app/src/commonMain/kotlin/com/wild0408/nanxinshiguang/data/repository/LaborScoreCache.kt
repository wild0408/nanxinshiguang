package com.wild0408.nanxinshiguang.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.wild0408.nanxinshiguang.data.model.LaborScore
import com.wild0408.nanxinshiguang.tool.SecureCrypto
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

interface LaborScoreCache {
    suspend fun load(studentId: String): LaborScore?
    suspend fun save(studentId: String, score: LaborScore)
}

@Serializable
private data class CachedLaborScore(val studentId: String, val score: LaborScore)

@Single
class DataStoreLaborScoreCache(
    @Named("LaborScore") private val store: DataStore<Preferences>,
    private val secureCrypto: SecureCrypto,
) : LaborScoreCache {
    private val encryptedKey = stringPreferencesKey("score_ciphertext")
    private val ivKey = stringPreferencesKey("score_iv")
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun load(studentId: String): LaborScore? {
        val saved = store.data.first()
        val encrypted = saved[encryptedKey] ?: return null
        val iv = saved[ivKey] ?: return null
        val decoded = secureCrypto.decrypt(encrypted, iv) ?: error("劳动积分缓存无法解密")
        val cached = json.decodeFromString<CachedLaborScore>(decoded)
        return cached.score.takeIf { cached.studentId == studentId }
    }

    override suspend fun save(studentId: String, score: LaborScore) {
        val encrypted = secureCrypto.encrypt(json.encodeToString(CachedLaborScore(studentId, score)))
            ?: error("劳动积分缓存加密失败")
        store.edit {
            it[encryptedKey] = encrypted.encryptedData
            it[ivKey] = encrypted.iv
        }
    }
}
