package com.wild0408.nanxinshiguang.data.portal

import com.wild0408.nanxinshiguang.data.repository.PortalCredentialRepository
import com.wild0408.nanxinshiguang.data.model.PortalUserProfile
import com.wild0408.nanxinshiguang.data.model.AcademicSummary
import com.wild0408.nanxinshiguang.data.model.CourseImportExport
import com.wild0408.nanxinshiguang.data.model.GradeRecord
import com.wild0408.nanxinshiguang.data.model.LaborScore

enum class PortalService(val landingUrl: String) {
    AUTH_SERVER("https://authserver.nuist.edu.cn/authserver/index"),
    JWXT("https://jwxt.nuist.edu.cn/jwapp/sys/emaphome/portal/index.do"),
    ICARD("https://icard.nuist.edu.cn/berserker-auth/cas/login/wisedu?targetUrl=https://icard.nuist.edu.cn/plat-pc/?name=loginTransit"),
    IPORTAL("https://i.nuist.edu.cn/login"),
    CXYJY("https://cxcyjy.nuist.edu.cn/pt/HomePage/UnifiedAuthenticationLogin"),
    LABOR("https://Labor.nuist.edu.cn/UnifiedAuth/CASLogin"),
}

sealed interface PortalLoginResult {
    data class Success(val landingUrl: String) : PortalLoginResult
    data class CredentialInvalid(val message: String) : PortalLoginResult
    data class NetworkError(val message: String) : PortalLoginResult
    data class LoginError(val message: String) : PortalLoginResult
}

sealed interface PortalProfileResult {
    data class Success(val profile: PortalUserProfile) : PortalProfileResult
    data class NotLoggedIn(val message: String) : PortalProfileResult
    data class NetworkError(val message: String) : PortalProfileResult
    data class Error(val message: String) : PortalProfileResult
}

sealed interface AcademicSummaryResult {
    data class Success(val summary: AcademicSummary) : AcademicSummaryResult
    data class NotLoggedIn(val message: String) : AcademicSummaryResult
    data class NetworkError(val message: String) : AcademicSummaryResult
    data class Error(val message: String) : AcademicSummaryResult
}

sealed interface GradeQueryResult {
    data class Success(val records: List<GradeRecord>) : GradeQueryResult
    data class NotLoggedIn(val message: String) : GradeQueryResult
    data class NetworkError(val message: String) : GradeQueryResult
    data class Error(val message: String) : GradeQueryResult
}

sealed interface LaborScoreResult {
    data class Success(val score: LaborScore) : LaborScoreResult
    data class NotLoggedIn(val message: String) : LaborScoreResult
    data class NetworkError(val message: String) : LaborScoreResult
    data class Error(val message: String) : LaborScoreResult
}

sealed interface CourseScheduleResult {
    data class Success(
        val semesterName: String,
        val schedule: CourseImportExport.CourseTableImportModel,
    ) : CourseScheduleResult
    data class NotLoggedIn(val message: String) : CourseScheduleResult
    data class NetworkError(val message: String) : CourseScheduleResult
    data class Error(val message: String) : CourseScheduleResult
}

interface PortalSession {
    /**
     * 强制走一次完整的门户 Passkey 登录，用教务服务作为稳定的认证探针。
     *
     * 不使用一卡通作为默认探针：一卡通落地页还可能有业务侧二次跳转，
     * 会把“门户凭据有效”与“业务服务登录完成”混在一起。
     */
    suspend fun verifyCredential(service: PortalService = PortalService.AUTH_SERVER): PortalLoginResult
    suspend fun ensureLoggedIn(service: PortalService): PortalLoginResult
    suspend fun fetchUserProfile(force: Boolean = false): PortalProfileResult
    suspend fun fetchAcademicSummary(): AcademicSummaryResult
    suspend fun fetchGrades(): GradeQueryResult
    suspend fun fetchLaborScore(): LaborScoreResult
    suspend fun fetchCurrentCourseSchedule(): CourseScheduleResult
    suspend fun clear(includeWebView: Boolean = false)
}

expect fun createPortalSession(repository: PortalCredentialRepository): PortalSession
