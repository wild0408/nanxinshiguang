package com.wild0408.nanxinshiguang.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.wild0408.nanxinshiguang.data.model.PortalPasskeyBundle
import com.wild0408.nanxinshiguang.data.model.PortalUserProfile
import com.wild0408.nanxinshiguang.tool.SecureCrypto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

sealed interface PortalBindingState {
    data object Loading : PortalBindingState
    data object Unbound : PortalBindingState
    data class Bound(val bundle: PortalPasskeyBundle) : PortalBindingState
    data class Invalid(val message: String) : PortalBindingState
    data class Error(val message: String) : PortalBindingState
}

interface PortalCredentialRepository {
    val state: StateFlow<PortalBindingState>
    val profile: StateFlow<PortalUserProfile?>
    suspend fun load()
    suspend fun save(bundle: PortalPasskeyBundle)
    suspend fun saveProfile(profile: PortalUserProfile)
    suspend fun clear()
}

@Single
class DataStorePortalCredentialRepository(
    @Named("PortalAuth") private val dataStore: DataStore<Preferences>,
    private val secureCrypto: SecureCrypto,
) : PortalCredentialRepository {
    private val _state = MutableStateFlow<PortalBindingState>(PortalBindingState.Loading)
    override val state: StateFlow<PortalBindingState> = _state.asStateFlow()
    private val _profile = MutableStateFlow<PortalUserProfile?>(null)
    override val profile: StateFlow<PortalUserProfile?> = _profile.asStateFlow()

    private object Keys {
        val encrypted = stringPreferencesKey("portal_bundle_ciphertext")
        val iv = stringPreferencesKey("portal_bundle_iv")
        val profileEncrypted = stringPreferencesKey("portal_profile_ciphertext")
        val profileIv = stringPreferencesKey("portal_profile_iv")
    }

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun load() {
        val prefs = dataStore.data.first()
        val encrypted = prefs[Keys.encrypted]
        val iv = prefs[Keys.iv]
        if (encrypted.isNullOrBlank() || iv.isNullOrBlank()) {
            _profile.value = null
            _state.value = PortalBindingState.Unbound
            return
        }
        val decoded = secureCrypto.decrypt(encrypted, iv)
        if (decoded.isNullOrBlank()) {
            _profile.value = null
            _state.value = PortalBindingState.Invalid("统一门户凭据无法解密，请重新绑定")
            return
        }
        _state.value = runCatching {
            PortalBindingState.Bound(json.decodeFromString<PortalPasskeyBundle>(decoded))
        }.getOrElse { PortalBindingState.Invalid("统一门户凭据格式异常，请重新绑定") }
        val profileEncrypted = prefs[Keys.profileEncrypted]
        val profileIv = prefs[Keys.profileIv]
        _profile.value = if (!profileEncrypted.isNullOrBlank() && !profileIv.isNullOrBlank()) {
            secureCrypto.decrypt(profileEncrypted, profileIv)?.let { value ->
                runCatching { json.decodeFromString<PortalUserProfile>(value) }.getOrNull()
            }
        } else {
            null
        }
    }

    override suspend fun save(bundle: PortalPasskeyBundle) {
        val encrypted = secureCrypto.encrypt(json.encodeToString(bundle))
            ?: error("统一门户凭据保存失败")
        dataStore.edit { prefs ->
            prefs[Keys.encrypted] = encrypted.encryptedData
            prefs[Keys.iv] = encrypted.iv
            prefs.remove(Keys.profileEncrypted)
            prefs.remove(Keys.profileIv)
        }
        _profile.value = null
        _state.value = PortalBindingState.Bound(bundle)
    }

    override suspend fun saveProfile(profile: PortalUserProfile) {
        val encrypted = secureCrypto.encrypt(json.encodeToString(profile))
            ?: error("统一门户身份信息保存失败")
        dataStore.edit { prefs ->
            prefs[Keys.profileEncrypted] = encrypted.encryptedData
            prefs[Keys.profileIv] = encrypted.iv
        }
        _profile.value = profile
    }

    override suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.encrypted)
            prefs.remove(Keys.iv)
            prefs.remove(Keys.profileEncrypted)
            prefs.remove(Keys.profileIv)
        }
        _profile.value = null
        _state.value = PortalBindingState.Unbound
    }
}
