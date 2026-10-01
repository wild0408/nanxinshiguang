package com.wild0408.nanxinshiguang.data.repository

import com.wild0408.nanxinshiguang.data.model.*
import com.wild0408.nanxinshiguang.data.portal.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class LaborScoreRepositoryTest {
    private val score = LaborScore(null, "12", "3", LaborMajorLive("4", "5", "9"), 1)

    @Test
    fun firstQueryIsCachedAndReopeningOrRecreatingDoesNotFetch() = runBlocking {
        val fixture = Fixture(LaborScoreResult.Success(score))
        fixture.repository.initialize("12345678")
        fixture.repository.initialize("12345678")
        assertEquals(1, fixture.session.calls)
        assertEquals(score, fixture.repository.state.value.score)

        val recreated = fixture.newRepository()
        recreated.initialize("12345678")
        assertEquals(1, fixture.session.calls)
        assertEquals(score, recreated.state.value.score)
        recreated.refresh()
        assertEquals(2, fixture.session.calls)
    }

    @Test
    fun successfulEmptyResultIsCachedAndDoesNotFetchAgain() = runBlocking {
        val fixture = Fixture(LaborScoreResult.Success(LaborScore(null, null, null, null, 1)))
        fixture.repository.initialize("12345678")
        val recreated = fixture.newRepository()
        recreated.initialize("12345678")
        assertEquals(1, fixture.session.calls)
        assertEquals(LaborScoreStatus.Empty, recreated.state.value.status)
        assertNull(recreated.state.value.score)
    }

    @Test
    fun failedManualRefreshRetainsMemoryAndPersistedData() = runBlocking {
        val fixture = Fixture(LaborScoreResult.Success(score))
        fixture.repository.initialize("12345678")
        fixture.session.result = LaborScoreResult.NotLoggedIn("expired")
        fixture.repository.refresh()
        assertEquals(score, fixture.repository.state.value.score)
        assertFalse(fixture.repository.state.value.loading)
        val recreated = fixture.newRepository()
        recreated.initialize("12345678")
        assertEquals(score, recreated.state.value.score)
        assertEquals(2, fixture.session.calls)
    }

    @Test
    fun unbindingDoesNotQueryOrExposeCachedData() = runBlocking {
        val fixture = Fixture(LaborScoreResult.Success(score))
        fixture.repository.initialize("12345678")
        fixture.credentials.state.value = PortalBindingState.Unbound
        fixture.repository.initialize(null)
        assertEquals(1, fixture.session.calls)
        assertNull(fixture.repository.state.value.score)
        assertEquals(LaborScoreStatus.NotLoggedIn, fixture.repository.state.value.status)
    }

    @Test
    fun switchedAccountDoesNotReceiveOldCache() = runBlocking {
        val fixture = Fixture(LaborScoreResult.Success(score))
        fixture.repository.initialize("12345678")
        fixture.credentials.state.value = PortalBindingState.Bound(bundle("ODc2NTQzMjE="))
        fixture.session.result = LaborScoreResult.NetworkError("offline")
        fixture.repository.initialize("87654321")
        assertNull(fixture.repository.state.value.score)
        assertEquals(2, fixture.session.calls)
    }

    @Test
    fun cancelledFirstQueryCanInitializeAgain() = runBlocking {
        val fixture = Fixture(LaborScoreResult.Success(score))
        fixture.session.cancel = true
        try {
            fixture.repository.initialize("12345678")
        } catch (_: CancellationException) {
            // A page's scope can be cancelled while the first request is running.
        }
        fixture.session.cancel = false
        fixture.repository.initialize("12345678")
        assertEquals(score, fixture.repository.state.value.score)
        assertFalse(fixture.repository.state.value.loading)
    }

    @Test
    fun allLaborFieldsSurviveSerialization() {
        val full = score.copy(official = LaborOfficialResult("1", "2", "3", "4", "5", "9", "15", "是", "date", "否", ""))
        assertEquals(full, Json.decodeFromString<LaborScore>(Json.encodeToString(full)))
    }

    private class Fixture(result: LaborScoreResult) {
        val credentials = FakeCredentials()
        val session = FakeSession(result)
        val cache = FakeCache()
        val repository = newRepository()
        fun newRepository() = LaborScoreRepository(session, credentials, cache)
    }

    private class FakeCache : LaborScoreCache {
        private val values = mutableMapOf<String, LaborScore>()
        override suspend fun load(studentId: String) = values[studentId]
        override suspend fun save(studentId: String, score: LaborScore) { values[studentId] = score }
    }

    private class FakeCredentials : PortalCredentialRepository {
        override val state = MutableStateFlow<PortalBindingState>(PortalBindingState.Bound(bundle("MTIzNDU2Nzg=")))
        override val profile = MutableStateFlow<PortalUserProfile?>(null)
        override suspend fun load() = Unit
        override suspend fun save(bundle: PortalPasskeyBundle) { state.value = PortalBindingState.Bound(bundle) }
        override suspend fun saveProfile(profile: PortalUserProfile) { this.profile.value = profile }
        override suspend fun clear() { state.value = PortalBindingState.Unbound }
    }

    private class FakeSession(var result: LaborScoreResult) : PortalSession {
        var calls = 0
        var cancel = false
        override suspend fun fetchLaborScore(): LaborScoreResult {
            calls++
            if (cancel) throw CancellationException()
            return result
        }
        override suspend fun verifyCredential(service: PortalService): PortalLoginResult = error("unused")
        override suspend fun ensureLoggedIn(service: PortalService): PortalLoginResult = error("unused")
        override suspend fun fetchUserProfile(force: Boolean): PortalProfileResult = error("unused")
        override suspend fun fetchAcademicSummary(): AcademicSummaryResult = error("unused")
        override suspend fun fetchGrades(): GradeQueryResult = error("unused")
        override suspend fun fetchCurrentCourseSchedule(): CourseScheduleResult = error("unused")
        override suspend fun clear(includeWebView: Boolean) = Unit
    }

    companion object {
        private fun bundle(userId: String) = PortalPasskeyBundle("test", "", "", userId, "", "test", 0)
    }
}
