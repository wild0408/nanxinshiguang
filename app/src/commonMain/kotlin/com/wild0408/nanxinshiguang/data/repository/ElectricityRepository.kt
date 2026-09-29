package com.wild0408.nanxinshiguang.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.wild0408.nanxinshiguang.data.api.electricity.ElectricityApi
import com.wild0408.nanxinshiguang.data.api.electricity.ElectricityLocation
import com.wild0408.nanxinshiguang.data.api.electricity.extractIcardSsoTicket
import com.wild0408.nanxinshiguang.data.db.main.ElectricityHistory
import com.wild0408.nanxinshiguang.data.db.main.ElectricityHistoryDao
import com.wild0408.nanxinshiguang.data.portal.PortalLoginResult
import com.wild0408.nanxinshiguang.data.portal.PortalService
import com.wild0408.nanxinshiguang.data.portal.PortalSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import kotlin.time.Clock
import kotlin.math.abs

data class ElectricityConfig(
    val studentId: String,
    val campus: ElectricityLocation,
    val building: ElectricityLocation,
    val room: ElectricityLocation,
    val token: String = "",
    val cachedBalance: Double? = null,
    val lastUpdated: Long? = null,
)

data class ElectricityPortalAuth(
    val studentId: String,
    val token: String,
)

data class ElectricityQueryResult(
    val balance: Double,
    val token: String,
    val queriedAt: Long,
)

@Single
class ElectricityRepository(
    @Named("ApiConfig") private val store: DataStore<Preferences>,
    private val historyDao: ElectricityHistoryDao,
    private val api: ElectricityApi,
    private val portalCredentialRepository: PortalCredentialRepository,
    private val portalSession: PortalSession,
) {
    private object K {
        val student = stringPreferencesKey("electricity_student")
        val password = stringPreferencesKey("electricity_pwd")
        val passwordIv = stringPreferencesKey("electricity_iv")
        val campus = stringPreferencesKey("electricity_campus")
        val campusName = stringPreferencesKey("electricity_campus_name")
        val building = stringPreferencesKey("electricity_building")
        val buildingName = stringPreferencesKey("electricity_building_name")
        val room = stringPreferencesKey("electricity_room")
        val roomName = stringPreferencesKey("electricity_room_name")
        val token = stringPreferencesKey("electricity_token")
        val campuses = stringPreferencesKey("electricity_campuses")
        val buildings = stringPreferencesKey("electricity_buildings")
        val buildingsParent = stringPreferencesKey("electricity_buildings_parent")
        val rooms = stringPreferencesKey("electricity_rooms")
        val roomsParent = stringPreferencesKey("electricity_rooms_parent")
        val lastBalance = stringPreferencesKey("electricity_last_balance")
        val lastUpdated = stringPreferencesKey("electricity_last_updated")
    }

    val configFlow: Flow<ElectricityConfig?> = store.data.map { preferences ->
        val studentId = preferences[K.student]
        val campus = preferences[K.campus]
        val building = preferences[K.building]
        val room = preferences[K.room]
        if (studentId.isNullOrBlank() || campus.isNullOrBlank() || building.isNullOrBlank() || room.isNullOrBlank()) {
            null
        } else {
            ElectricityConfig(
                studentId = studentId,
                campus = ElectricityLocation(preferences[K.campusName].orEmpty(), campus),
                building = ElectricityLocation(preferences[K.buildingName].orEmpty(), building),
                room = ElectricityLocation(preferences[K.roomName].orEmpty(), room),
                token = preferences[K.token].orEmpty(),
                cachedBalance = preferences[K.lastBalance]?.toDoubleOrNull(),
                lastUpdated = preferences[K.lastUpdated]?.toLongOrNull(),
            )
        }
    }

    suspend fun currentPortalStudentId(): String? {
        clearLegacyPassword()
        portalCredentialRepository.load()
        return (portalCredentialRepository.state.value as? PortalBindingState.Bound)?.bundle?.studentId
    }

    suspend fun authenticateWithPortal(force: Boolean = false): ElectricityPortalAuth {
        clearLegacyPassword()
        portalCredentialRepository.load()
        val bound = portalCredentialRepository.state.value as? PortalBindingState.Bound
            ?: error("请先绑定统一认证")
        val studentId = bound.bundle.studentId ?: error("统一认证凭据中缺少学号，请重新绑定")

        var lastMessage = "一卡通统一认证未返回登录票据"
        for (shouldForce in listOf(force, true).distinct()) {
            when (val result = if (shouldForce) {
                portalSession.verifyCredential(PortalService.ICARD)
            } else {
                portalSession.ensureLoggedIn(PortalService.ICARD)
            }) {
                is PortalLoginResult.Success -> {
                    val ticket = extractIcardSsoTicket(result.landingUrl)
                    if (ticket != null) {
                        return ElectricityPortalAuth(studentId, api.loginWithSsoTicket(ticket))
                    }
                    lastMessage = "一卡通统一认证未返回登录票据"
                }
                is PortalLoginResult.CredentialInvalid -> error(result.message)
                is PortalLoginResult.NetworkError -> error(result.message)
                is PortalLoginResult.LoginError -> error(result.message)
            }
        }
        error(lastMessage)
    }

    suspend fun save(config: ElectricityConfig) {
        store.edit { preferences ->
            preferences[K.student] = config.studentId
            preferences[K.campus] = config.campus.value
            preferences[K.campusName] = config.campus.name
            preferences[K.building] = config.building.value
            preferences[K.buildingName] = config.building.name
            preferences[K.room] = config.room.value
            preferences[K.roomName] = config.room.name
            if (config.token.isNotBlank()) preferences[K.token] = config.token else preferences.remove(K.token)
            // 统一认证接入后不再保留旧版一卡通独立密码。
            preferences.remove(K.password)
            preferences.remove(K.passwordIv)
        }
    }

    private suspend fun clearLegacyPassword() {
        store.edit { preferences ->
            preferences.remove(K.password)
            preferences.remove(K.passwordIv)
        }
    }

    suspend fun clear(config: ElectricityConfig?) {
        store.edit { preferences ->
            listOf(
                K.student,
                K.password,
                K.passwordIv,
                K.campus,
                K.campusName,
                K.building,
                K.buildingName,
                K.room,
                K.roomName,
                K.token,
                K.campuses,
                K.buildings,
                K.buildingsParent,
                K.rooms,
                K.roomsParent,
                K.lastBalance,
                K.lastUpdated,
            ).forEach(preferences::remove)
        }
        config?.let { historyDao.delete(it.studentId, it.room.value) }
    }

    private fun encode(items: List<ElectricityLocation>): String =
        items.joinToString("\u001e") { "${it.value}\u001f${it.name}" }

    private fun decode(raw: String?): List<ElectricityLocation> = raw.orEmpty()
        .split("\u001e")
        .mapNotNull { row ->
            row.split("\u001f", limit = 2)
                .takeIf { it.size == 2 }
                ?.let { ElectricityLocation(it[1], it[0]) }
        }

    suspend fun storedCampuses(): List<ElectricityLocation> = decode(store.data.first()[K.campuses])

    suspend fun storedBuildings(parent: String): List<ElectricityLocation> {
        val preferences = store.data.first()
        return if (preferences[K.buildingsParent] == parent) decode(preferences[K.buildings]) else emptyList()
    }

    suspend fun storedRooms(parent: String): List<ElectricityLocation> {
        val preferences = store.data.first()
        return if (preferences[K.roomsParent] == parent) decode(preferences[K.rooms]) else emptyList()
    }

    suspend fun saveLocations(level: Int, items: List<ElectricityLocation>, parent: String = "") {
        store.edit { preferences ->
            when (level) {
                0 -> preferences[K.campuses] = encode(items)
                1 -> {
                    preferences[K.buildings] = encode(items)
                    preferences[K.buildingsParent] = parent
                }
                2 -> {
                    preferences[K.rooms] = encode(items)
                    preferences[K.roomsParent] = parent
                }
            }
        }
    }

    suspend fun campuses(token: String) = api.locations(token, 0)
    suspend fun buildings(token: String, campus: String) = api.locations(token, 1, campus)
    suspend fun rooms(token: String, campus: String, building: String) = api.locations(token, 2, campus, building)

    suspend fun query(config: ElectricityConfig, token: String): ElectricityQueryResult {
        val balance = api.balance(token, config.campus.value, config.building.value, config.room.value)
        val queriedAt = Clock.System.now().toEpochMilliseconds()
        store.edit { preferences ->
            preferences[K.lastBalance] = balance.toString()
            preferences[K.lastUpdated] = queriedAt.toString()
        }
        val latest = historyDao.latest(config.studentId, config.room.value)
        if (latest == null || abs(latest.balance - balance) >= 0.005) {
            historyDao.insert(
                ElectricityHistory(
                    studentId = config.studentId,
                    roomKey = config.room.value,
                    recordedAt = queriedAt,
                    balance = balance,
                ),
            )
        }
        historyDao.trim(config.studentId, config.room.value)
        return ElectricityQueryResult(balance, token, queriedAt)
    }

    fun history(config: ElectricityConfig): Flow<List<ElectricityHistory>> =
        historyDao.observe(config.studentId, config.room.value).map(::removeConsecutiveDuplicates)

    private fun removeConsecutiveDuplicates(items: List<ElectricityHistory>): List<ElectricityHistory> =
        items.fold(mutableListOf()) { result, item ->
            if (result.lastOrNull()?.balance?.let { abs(it - item.balance) < 0.005 } != true) {
                result += item
            }
            result
        }
}
