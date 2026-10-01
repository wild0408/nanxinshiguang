package com.wild0408.nanxinshiguang.data.portal

import android.util.Base64
import android.util.Log
import com.wild0408.nanxinshiguang.data.model.CourseImportExport
import com.wild0408.nanxinshiguang.data.model.parseAcademicSummary
import com.wild0408.nanxinshiguang.data.parser.GradeParseResult
import com.wild0408.nanxinshiguang.data.parser.parseEmapGradeRecordsResult
import com.wild0408.nanxinshiguang.data.parser.parseLaborScore
import com.wild0408.nanxinshiguang.data.model.PortalUserProfile
import com.wild0408.nanxinshiguang.data.repository.PortalBindingState
import com.wild0408.nanxinshiguang.data.repository.PortalCredentialRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.URLEncoder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec

private const val AUTH_SERVER = "https://authserver.nuist.edu.cn"
private const val JWXT_BASE = "https://jwxt.nuist.edu.cn"
private const val JWXT_INDEX_URL = "$JWXT_BASE/jwapp/sys/cjcx/*default/index.do?EMAP_LANG=zh"
private const val JWXT_QUERY_URL = "$JWXT_BASE/jwapp/sys/cjcx/modules/cjcx/xscjcx.do"
private const val LABOR_BASE = "https://labor.nuist.edu.cn"
private const val WDKB_INDEX_URL = "$JWXT_BASE/jwapp/sys/wdkb/*default/index.do?EMAP_LANG=zh"
private const val WDKB_MODULE_BASE = "$JWXT_BASE/jwapp/sys/wdkb/modules"
private const val LOGIN_PATH = "/authserver/login"
private const val ASSERTION_PATH = "/authserver/startAssertion"
private const val ORIGIN = AUTH_SERVER
private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:155.0) Gecko/20100101 Firefox/155.0"
private const val LOG_TAG = "PortalSession"

private class AndroidPortalSession(
    private val repository: PortalCredentialRepository,
) : PortalSession {
    private val cookies = linkedMapOf<String, String>()
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun verifyCredential(service: PortalService): PortalLoginResult = login(service, force = true)

    override suspend fun ensureLoggedIn(service: PortalService): PortalLoginResult = login(service, force = false)

    override suspend fun fetchUserProfile(force: Boolean): PortalProfileResult =
        withContext(Dispatchers.IO) {
            val login = if (force) {
                verifyCredential(PortalService.IPORTAL)
            } else {
                ensureLoggedIn(PortalService.IPORTAL)
            }
            if (login !is PortalLoginResult.Success) {
                return@withContext when (login) {
                    is PortalLoginResult.NetworkError -> PortalProfileResult.NetworkError(login.message)
                    is PortalLoginResult.CredentialInvalid -> PortalProfileResult.NotLoggedIn(login.message)
                    is PortalLoginResult.LoginError -> PortalProfileResult.Error(login.message)
                    is PortalLoginResult.Success -> error("unreachable")
                }
            }
            try {
                val response = follow(
                    HttpRequest.getJson(
                        "https://i.nuist.edu.cn/getLoginUserAndGuest?_t=${kotlin.random.Random.nextDouble()}",
                    ),
                )
                if (response.status == 401 || response.status == 403 ||
                    response.url.contains("/login", ignoreCase = true)
                ) {
                    if (!force) return@withContext fetchUserProfile(force = true)
                    return@withContext PortalProfileResult.NotLoggedIn("信息门户登录状态已失效，请重新绑定")
                }
                if (response.status >= 400) {
                    return@withContext PortalProfileResult.Error("信息门户返回 HTTP ${response.status}")
                }
                // 参考项目将“返回 HTML”视为未登录/会话未建立，而不是直接尝试
                // JSON 解析。门户在缺少 Accept: application/json 时会返回首页 HTML。
                if (response.body.trimStart().startsWith("<!doctype", ignoreCase = true) ||
                    response.body.trimStart().startsWith("<html", ignoreCase = true)
                ) {
                    if (!force) return@withContext fetchUserProfile(force = true)
                    return@withContext PortalProfileResult.NotLoggedIn("信息门户未返回身份 JSON，请重新登录后重试")
                }
                val body = json.parseToJsonElement(response.body).jsonObject
                val data = body["data"]?.jsonObject
                val studentId = data?.stringValue("userAccount").orEmpty()
                if (studentId.isBlank()) {
                    if (!force) return@withContext fetchUserProfile(force = true)
                    return@withContext PortalProfileResult.NotLoggedIn("信息门户未返回当前登录人信息")
                }
                val category = data.stringValue("categoryName")
                val departmentPath = data.stringValue("deptName")
                val path = departmentPath
                    .removePrefix(category)
                    .split('/')
                    .map(String::trim)
                    .filter(String::isNotBlank)
                val department = path.dropLast(1).joinToString(" · ").ifBlank {
                    path.firstOrNull().orEmpty()
                }
                val className = if (path.size > 1) path.last() else ""
                PortalProfileResult.Success(
                    PortalUserProfile(
                        name = data.stringValue("userName"),
                        studentId = studentId,
                        categoryName = category,
                        departmentName = department,
                        className = className,
                        fetchedAt = kotlin.time.Clock.System.now().toEpochMilliseconds(),
                    ),
                )
            } catch (e: IOException) {
                Log.w(LOG_TAG, "profile network error: ${e::class.simpleName}")
                PortalProfileResult.NetworkError("无法读取信息门户身份信息，请检查网络")
            } catch (e: Exception) {
                Log.e(LOG_TAG, "profile request failed: ${e::class.simpleName}", e)
                PortalProfileResult.Error("信息门户身份信息读取失败")
            }
        }

    override suspend fun fetchAcademicSummary(): AcademicSummaryResult = withContext(Dispatchers.IO) {
        val expectedStudentId = (repository.state.value as? PortalBindingState.Bound)?.bundle?.studentId
        var accountMismatch = false
        for (force in listOf(false, true)) {
            val login = if (force) verifyCredential(PortalService.IPORTAL)
            else ensureLoggedIn(PortalService.IPORTAL)
            if (login !is PortalLoginResult.Success) {
                return@withContext when (login) {
                    is PortalLoginResult.CredentialInvalid -> AcademicSummaryResult.NotLoggedIn(login.message)
                    is PortalLoginResult.NetworkError -> AcademicSummaryResult.NetworkError(login.message)
                    is PortalLoginResult.LoginError -> AcademicSummaryResult.Error(login.message)
                    is PortalLoginResult.Success -> error("unreachable")
                }
            }
            try {
                val response = follow(HttpRequest.getJson("https://i.nuist.edu.cn/cus/jxmh/czjl?xh="))
                if (response.status >= 400 && response.status != 401 && response.status != 403) {
                    return@withContext AcademicSummaryResult.Error("学业概览接口返回 HTTP ${response.status}")
                }
                if (response.status == 200 && !response.url.contains("/login", ignoreCase = true)) {
                    parseAcademicSummary(
                        response.body,
                        kotlin.time.Clock.System.now().toEpochMilliseconds(),
                    )?.let { summary ->
                        if (expectedStudentId == summary.studentId) {
                            return@withContext AcademicSummaryResult.Success(summary)
                        }
                        accountMismatch = true
                    }
                }
            } catch (e: IOException) {
                Log.w(LOG_TAG, "academic summary network error: ${e::class.simpleName}")
                return@withContext AcademicSummaryResult.NetworkError("无法读取学业概览，请检查网络")
            } catch (e: Exception) {
                Log.e(LOG_TAG, "academic summary request failed: ${e::class.simpleName}", e)
                return@withContext AcademicSummaryResult.Error("学业概览读取失败")
            }
        }
        AcademicSummaryResult.NotLoggedIn(
            if (accountMismatch) "门户返回的学号与绑定账号不一致，请重新绑定"
            else "信息门户未返回学业概览，请重新登录后重试",
        )
    }

    override suspend fun fetchGrades(): GradeQueryResult = withContext(Dispatchers.IO) {
        for (force in listOf(false, true)) {
            val login = if (force) verifyCredential(PortalService.JWXT)
            else ensureLoggedIn(PortalService.JWXT)
            if (login !is PortalLoginResult.Success) {
                return@withContext when (login) {
                    is PortalLoginResult.CredentialInvalid -> GradeQueryResult.NotLoggedIn(login.message)
                    is PortalLoginResult.NetworkError -> GradeQueryResult.NetworkError(login.message)
                    is PortalLoginResult.LoginError -> GradeQueryResult.Error(login.message)
                    is PortalLoginResult.Success -> error("unreachable")
                }
            }
            try {
                val index = follow(
                    HttpRequest.get(JWXT_INDEX_URL),
                )
                if (isLoginResponse(index)) {
                    if (!force) continue
                    return@withContext GradeQueryResult.NotLoggedIn("教务系统登录状态已失效，请重新绑定")
                }
                if (index.status >= 400) {
                    return@withContext GradeQueryResult.Error("教务成绩页面返回 HTTP ${index.status}")
                }
                val response = follow(
                    HttpRequest.postForm(
                        JWXT_QUERY_URL,
                        mapOf(
                            "querySetting" to """[{"name":"SFYX","caption":"是否有效","builder":"m_value_equal","linkOpt":"AND","value":"1"}]""",
                            "*json" to "1",
                            "*order" to "-XNXQDM,-KCH,-KXH",
                            "pageSize" to "1000",
                            "pageNumber" to "1",
                        ),
                        JWXT_INDEX_URL,
                        origin = JWXT_BASE,
                        accept = "application/json, text/javascript, */*; q=0.01",
                    ),
                )
                if (isLoginResponse(response)) {
                    if (!force) continue
                    return@withContext GradeQueryResult.NotLoggedIn("教务系统登录状态已失效，请重新绑定")
                }
                if (response.status >= 400) {
                    return@withContext GradeQueryResult.Error("成绩接口返回 HTTP ${response.status}")
                }
                val records = when (val parseResult = parseEmapGradeRecordsResult(response.body)) {
                    is GradeParseResult.Success -> parseResult.records
                    is GradeParseResult.Invalid -> {
                        // 教务未返回 JSON 时通常是登录页失效，而不是"没有成绩"。
                        val trimmed = response.body.trimStart()
                        if (trimmed.startsWith("<!doctype", ignoreCase = true) ||
                            trimmed.startsWith("<html", ignoreCase = true)
                        ) {
                            if (!force) continue
                            return@withContext GradeQueryResult.NotLoggedIn("教务系统未返回成绩数据，请重新绑定")
                        }
                        Log.w(LOG_TAG, "grade parse failed: ${parseResult.reason}")
                        return@withContext GradeQueryResult.Error("成绩数据解析失败，可能教务接口已变更")
                    }
                }
                return@withContext GradeQueryResult.Success(records)
            } catch (e: IOException) {
                Log.w(LOG_TAG, "grade network error: ${e::class.simpleName}")
                return@withContext GradeQueryResult.NetworkError("无法连接教务系统，请检查网络")
            } catch (e: Exception) {
                Log.e(LOG_TAG, "grade request failed: ${e::class.simpleName}", e)
                return@withContext GradeQueryResult.Error("成绩读取失败")
            }
        }
        GradeQueryResult.NotLoggedIn("教务系统登录状态已失效，请重新绑定")
    }

    override suspend fun fetchLaborScore(): LaborScoreResult = withContext(Dispatchers.IO) {
        for (force in listOf(false, true)) {
            val login = if (force) verifyCredential(PortalService.LABOR) else ensureLoggedIn(PortalService.LABOR)
            if (login !is PortalLoginResult.Success) return@withContext when (login) {
                is PortalLoginResult.CredentialInvalid -> LaborScoreResult.NotLoggedIn(login.message)
                is PortalLoginResult.NetworkError -> LaborScoreResult.NetworkError(login.message)
                is PortalLoginResult.LoginError -> LaborScoreResult.Error(login.message)
                is PortalLoginResult.Success -> error("unreachable")
            }
            try {
                val result = follow(HttpRequest.get("$LABOR_BASE/ResultManage/StudentResult"))
                if (isLaborLoginResponse(result)) { if (!force) continue; return@withContext LaborScoreResult.NotLoggedIn("劳动教育平台登录状态已失效，请重新绑定") }
                val pages = listOf(
                    follow(HttpRequest.get("$LABOR_BASE/Activity/StudentJiFen/Index?pc=%E7%94%9F%E6%B4%BB")),
                    follow(HttpRequest.get("$LABOR_BASE/Activity/StudentJiFen/Index?pc=%E6%9C%8D%E5%8A%A1")),
                    follow(HttpRequest.get("$LABOR_BASE/ResultManage/ZYLDScoreHZ/Index4DefaultGrades")),
                )
                if (pages.any(::isLaborLoginResponse)) { if (!force) continue; return@withContext LaborScoreResult.NotLoggedIn("劳动教育平台登录状态已失效，请重新绑定") }
                return@withContext LaborScoreResult.Success(parseLaborScore(result.body, pages[0].body, pages[1].body, pages[2].body, kotlin.time.Clock.System.now().toEpochMilliseconds()))
            } catch (e: IOException) {
                return@withContext LaborScoreResult.NetworkError("无法连接劳动教育平台，请检查网络")
            } catch (e: Exception) {
                Log.e(LOG_TAG, "labor score request failed", e)
                return@withContext LaborScoreResult.Error("劳动积分读取失败")
            }
        }
        LaborScoreResult.NotLoggedIn("劳动教育平台登录状态已失效，请重新绑定")
    }

    override suspend fun fetchCurrentCourseSchedule(): CourseScheduleResult = withContext(Dispatchers.IO) {
        attempt@ for (force in listOf(false, true)) {
            val login = if (force) verifyCredential(PortalService.JWXT)
            else ensureLoggedIn(PortalService.JWXT)
            if (login !is PortalLoginResult.Success) {
                return@withContext when (login) {
                    is PortalLoginResult.CredentialInvalid -> CourseScheduleResult.NotLoggedIn(login.message)
                    is PortalLoginResult.NetworkError -> CourseScheduleResult.NetworkError(login.message)
                    is PortalLoginResult.LoginError -> CourseScheduleResult.Error(login.message)
                    is PortalLoginResult.Success -> error("unreachable")
                }
            }
            try {
                val index = follow(HttpRequest.get(WDKB_INDEX_URL))
                if (isLoginResponse(index)) {
                    if (!force) continue@attempt
                    return@withContext CourseScheduleResult.NotLoggedIn("教务系统登录状态已失效，请重新绑定")
                }
                if (index.status >= 400) {
                    return@withContext CourseScheduleResult.Error("教务课表页面返回 HTTP ${index.status}")
                }

                fun postModule(path: String, form: Map<String, String?> = emptyMap()): HttpResponse =
                    follow(
                        HttpRequest.postForm(
                            "$WDKB_MODULE_BASE/$path",
                            form,
                            WDKB_INDEX_URL,
                            origin = JWXT_BASE,
                            accept = "application/json, text/javascript, */*; q=0.01",
                        ),
                    )

                val semesterResponse = postModule("jshkcb/dqxnxq.do")
                if (isLoginResponse(semesterResponse)) {
                    if (!force) continue@attempt
                    return@withContext CourseScheduleResult.NotLoggedIn("教务系统未返回当前学期，请重新绑定")
                }
                val semesterRoot = parseJsonObject(semesterResponse.body)
                    ?: return@withContext CourseScheduleResult.Error("当前学期接口返回数据格式异常")
                val semester = semesterRoot.sectionRows("dqxnxq")?.firstOrNull() as? JsonObject
                    ?: return@withContext CourseScheduleResult.Error("教务系统未返回当前学期")
                val semesterCode = semester.rawText("DM")
                    ?: return@withContext CourseScheduleResult.Error("当前学期缺少学期代码")
                val semesterName = semester.text("MC", "DM") ?: semesterCode

                val coursesResponse = postModule(
                    "xskcb/cxxszhxqkb.do",
                    mapOf("XNXQDM" to semesterCode),
                )
                if (isLoginResponse(coursesResponse)) {
                    if (!force) continue@attempt
                    return@withContext CourseScheduleResult.NotLoggedIn("教务系统未返回课表，请重新绑定")
                }
                val coursesRoot = parseJsonObject(coursesResponse.body)
                    ?: return@withContext CourseScheduleResult.Error("课表接口返回数据格式异常")
                val courseSection = coursesRoot.section("cxxszhxqkb")
                    ?: return@withContext CourseScheduleResult.Error("教务系统未返回课表数据")
                val extParams = courseSection["extParams"] as? JsonObject
                if (extParams?.rawText("code")?.toIntOrNull()?.let { it != 1 } == true) {
                    return@withContext CourseScheduleResult.Error(
                        extParams.text("msg") ?: "教务系统未发布当前学期课表",
                    )
                }
                val courses = (courseSection["rows"] as? JsonArray).orEmpty()
                    .mapNotNull { element ->
                        (element as? JsonObject)?.toImportCourse()
                    }
                if (courses.isEmpty()) {
                    return@withContext CourseScheduleResult.Error("当前学期没有可导入的课程")
                }

                val timeSlots = runCatching {
                    val response = postModule("jshkcb/jc.do")
                    parseJsonObject(response.body)?.sectionRows("jc").orEmpty()
                        .mapNotNull { element ->
                            val row = element as? JsonObject ?: return@mapNotNull null
                            val number = row.rawText("DM")?.toIntOrNull() ?: return@mapNotNull null
                            val start = row.rawText("KSSJ") ?: return@mapNotNull null
                            val end = row.rawText("JSSJ") ?: return@mapNotNull null
                            CourseImportExport.TimeSlotJsonModel(number, start, end)
                        }
                        .sortedBy { it.number }
                        .ifEmpty { defaultNuistTimeSlots() }
                }.getOrElse { defaultNuistTimeSlots() }

                val parts = semesterCode.split('-')
                val config = if (parts.size >= 3) {
                    runCatching {
                        val response = postModule(
                            "jshkcb/cxjcs.do",
                            mapOf("XN" to "${parts[0]}-${parts[1]}", "XQ" to parts[2]),
                        )
                        val row = parseJsonObject(response.body)?.sectionRows("cxjcs")
                            ?.firstOrNull() as? JsonObject
                        val startDate = row?.rawText("XQKSRQ", "XQKSRQ_DISPLAY", "KSRQ")
                            ?.substringBefore(' ')
                            ?.substringBefore('T')
                            ?.takeIf { it.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) }
                        val weeks = row?.rawText("ZZC", "ZCZ", "ZC")?.toIntOrNull()
                            ?.takeIf { it > 0 } ?: 20
                        CourseImportExport.CourseConfigJsonModel(
                            semesterStartDate = startDate,
                            semesterTotalWeeks = weeks,
                            defaultClassDuration = 45,
                            defaultBreakDuration = 10,
                            firstDayOfWeek = 1,
                        )
                    }.getOrElse { CourseImportExport.CourseConfigJsonModel() }
                } else {
                    CourseImportExport.CourseConfigJsonModel()
                }

                return@withContext CourseScheduleResult.Success(
                    semesterName = semesterName,
                    schedule = CourseImportExport.CourseTableImportModel(
                        courses = courses,
                        timeSlots = timeSlots,
                        config = config,
                    ),
                )
            } catch (e: IOException) {
                Log.w(LOG_TAG, "schedule network error: ${e::class.simpleName}")
                return@withContext CourseScheduleResult.NetworkError("无法连接教务系统，请检查网络")
            } catch (e: Exception) {
                Log.e(LOG_TAG, "schedule request failed: ${e::class.simpleName}", e)
                return@withContext CourseScheduleResult.Error("课表读取失败")
            }
        }
        CourseScheduleResult.NotLoggedIn("教务系统登录状态已失效，请重新绑定")
    }

    override suspend fun clear(includeWebView: Boolean) {
        cookies.clear()
        if (includeWebView) android.webkit.CookieManager.getInstance().removeAllCookies(null)
    }

    private suspend fun login(service: PortalService, force: Boolean): PortalLoginResult =
        withContext(Dispatchers.IO) {
            val bundle = (repository.state.value as? PortalBindingState.Bound)?.bundle
                ?: return@withContext PortalLoginResult.CredentialInvalid("尚未绑定统一门户")
            try {
                if (force) cookies.clear()
                val loginUrl = "$AUTH_SERVER$LOGIN_PATH?service=${encodeService(service.landingUrl)}"
                val loginPage = follow(HttpRequest.get(loginUrl))
                Log.d(LOG_TAG, "login page status=${loginPage.status} redirected=${!loginPage.url.contains(LOGIN_PATH)}")
                if (loginPage.status >= 400) {
                    return@withContext PortalLoginResult.LoginError("统一门户认证页返回 HTTP ${loginPage.status}")
                }
                if (!loginPage.url.contains(LOGIN_PATH)) {
                    return@withContext PortalLoginResult.Success(loginPage.url)
                }

                val execution = Regex("name=[\\\"']execution[\\\"'][^>]*value=[\\\"']([^\\\"']+)", RegexOption.IGNORE_CASE)
                    .find(loginPage.body)?.groupValues?.getOrNull(1)?.let(::unescapeHtml) ?: "e1s1"

                val assertion = requestAssertion(bundle, loginUrl)
                val request = assertion["result"]?.jsonObject?.get("request")?.jsonObject
                    ?: assertion["datas"]?.jsonObject?.get("request")?.jsonObject
                    ?: return@withContext classifyCredential(assertion)
                val requestId = request["requestId"]?.jsonPrimitive?.content
                    ?.takeIf { it.isNotBlank() }
                    ?: return@withContext classifyCredential(assertion)
                val credential = makeAssertion(bundle, request)
                val form = linkedMapOf(
                    "_eventId" to "submit",
                    "username" to bundle.userId,
                    "responseJson" to buildJsonObject {
                        put("requestId", requestId)
                        put("credential", credential)
                        put("sessionToken", JsonNull)
                    }.toString(),
                    "cllt" to "fidoLogin",
                    "dllt" to "generalLogin",
                    "lt" to "",
                    "execution" to execution,
                )
                val submitted = request(HttpRequest.postForm(loginUrl, form, loginUrl))
                if (submitted.status !in 300..399) {
                    return@withContext if (submitted.status == 401 || submitted.status == 403) {
                        PortalLoginResult.CredentialInvalid("统一门户拒绝了通行密钥，请重新绑定")
                    } else {
                        PortalLoginResult.LoginError("登录未返回重定向（HTTP ${submitted.status}）")
                    }
                }
                val location = submitted.headers["location"]
                    ?: return@withContext PortalLoginResult.LoginError("登录重定向缺少 Location")
                if (service == PortalService.AUTH_SERVER) {
                    // 测试登录只需要确认 CAS 接受了断言；不再访问一个可能
                    // 暂停或仅校内可达的业务站点，避免把网络问题误报成凭据失效。
                    return@withContext PortalLoginResult.Success(
                        URI(loginUrl).resolve(location).toString(),
                    )
                }
                val landed = follow(
                    HttpRequest.get(URI(loginUrl).resolve(location).toString()),
                    stopAtTicket = service == PortalService.ICARD,
                )
                if (landed.url.contains(LOGIN_PATH)) {
                    PortalLoginResult.LoginError("登录后又跳回认证页，请检查服务地址")
                } else {
                    PortalLoginResult.Success(landed.url)
                }
            } catch (e: CredentialException) {
                Log.w(LOG_TAG, "credential rejected: ${e.message}")
                PortalLoginResult.CredentialInvalid(e.message ?: "通行密钥已失效")
            } catch (e: IOException) {
                Log.w(LOG_TAG, "network error: ${e::class.simpleName}: ${e.message}")
                PortalLoginResult.NetworkError("无法连接统一门户，请检查网络")
            } catch (e: Exception) {
                Log.e(LOG_TAG, "portal login failed: ${e::class.simpleName}: ${e.message}", e)
                PortalLoginResult.LoginError(e.message ?: "统一门户登录失败（${e::class.simpleName ?: "未知错误"}）")
            }
        }

    private fun requestAssertion(bundle: com.wild0408.nanxinshiguang.data.model.PortalPasskeyBundle, loginUrl: String): JsonObject {
        val response = request(
            HttpRequest.postJson(
                "$AUTH_SERVER$ASSERTION_PATH",
                "{\"userId\":${jsonString(bundle.userId)},\"id\":${jsonString(bundle.anonBiometricsId)}}",
                loginUrl,
            )
        )
        if (response.status >= 400) {
            throw CredentialException("门户断言请求失败（HTTP ${response.status}）")
        }
        val body = json.parseToJsonElement(response.body).jsonObject
        Log.d(
            LOG_TAG,
            "startAssertion status=${response.status} hasResult=${body["result"] != null} hasDatas=${body["datas"] != null} hasMessage=${body["message"] != null}",
        )
        if (body["success"]?.jsonPrimitive?.content == "false") throw CredentialException(body["message"]?.jsonPrimitive?.content ?: "门户拒绝了凭据")
        return body
    }

    private fun makeAssertion(
        bundle: com.wild0408.nanxinshiguang.data.model.PortalPasskeyBundle,
        request: JsonObject,
    ): JsonObject {
        val options = request["publicKeyCredentialRequestOptions"]?.jsonObject
            ?: throw CredentialException("门户未返回有效的断言参数")
        val challenge = options["challenge"]?.jsonPrimitive?.content
            ?.takeIf { it.isNotBlank() }
            ?: throw CredentialException("门户未返回 challenge")
        val rpId = options["rpId"]?.jsonPrimitive?.content
            ?.takeIf { it.isNotBlank() }
            ?: bundle.rpId.takeIf { it.isNotBlank() }
            ?: throw CredentialException("门户未返回 rpId")
        val allowed = options["allowCredentials"]?.jsonArray
        // WebAuthn 允许服务端省略 allowCredentials 或返回空列表，空列表表示
        // “不限制凭据”，不能把它误判成本机凭据已失效。
        if (allowed != null && allowed.isNotEmpty() &&
            allowed.none { it.jsonObject["id"]?.jsonPrimitive?.content == bundle.credentialId }
        ) {
            throw CredentialException("本机凭据不在服务端可用列表中")
        }
        val clientData = "{\"type\":\"webauthn.get\",\"challenge\":${jsonString(challenge)},\"origin\":${jsonString(ORIGIN)},\"crossOrigin\":false}"
        val authData = sha256(rpId.toByteArray(StandardCharsets.UTF_8)) + byteArrayOf(0x05, 0, 0, 0, 0)
        val signature = sign(bundle.privateKeyPkcs8Pem, authData + sha256(clientData.toByteArray(StandardCharsets.UTF_8)))
        return buildJsonObject {
            put("type", "public-key")
            put("id", bundle.credentialId)
            putJsonObject("response") {
                put("authenticatorData", base64Url(authData))
                put("clientDataJSON", base64Url(clientData.toByteArray(StandardCharsets.UTF_8)))
                put("signature", base64Url(signature))
            }
            putJsonObject("clientExtensionResults") {
                put("appid", false)
            }
        }
    }

    private fun sign(pem: String, data: ByteArray): ByteArray {
        val encoded = pem.replace(Regex("-----BEGIN [^-]+-----|-----END [^-]+-----|\\s"), "")
        val key = KeyFactory.getInstance("EC").generatePrivate(PKCS8EncodedKeySpec(Base64.decode(encoded, Base64.DEFAULT)))
        return Signature.getInstance("SHA256withECDSA").apply {
            initSign(key)
            update(data)
        }.sign()
    }

    private fun follow(initial: HttpRequest, stopAtTicket: Boolean = false): HttpResponse {
        var response = request(initial)
        repeat(10) {
            if (response.status !in 300..399) return response
            val location = response.headers["location"] ?: return response
            val nextUrl = URI(response.url).resolve(location).toString()
            if (stopAtTicket && containsTicket(nextUrl)) {
                Log.d(LOG_TAG, "captured service ticket before final landing")
                return response.copy(url = nextUrl)
            }
            Log.d(LOG_TAG, "redirect location=" + nextUrl.substringBefore('?') + " queryKeys=" + queryKeys(nextUrl))
            response = request(HttpRequest.get(nextUrl))
        }
        return response
    }

    private fun containsTicket(url: String): Boolean =
        runCatching {
            val decoded = URLDecoder.decode(url, StandardCharsets.UTF_8.name())
            Regex("(?:[?&#]|%3f|%26)(?:ticket|synjones-auth)(?:=|%3d)", RegexOption.IGNORE_CASE).containsMatchIn(decoded)
        }.getOrDefault(false)

    private fun queryKeys(url: String): String =
        runCatching {
            URI(url).rawQuery.orEmpty().split('&')
                .mapNotNull { it.substringBefore('=').takeIf(String::isNotBlank) }
                .joinToString(",")
                .ifBlank { "-" }
        }.getOrDefault("?")

    private fun isLoginResponse(response: HttpResponse): Boolean =
        response.status == 401 || response.status == 403 ||
            response.url.contains("/authserver/login", ignoreCase = true) ||
            response.body.contains("name=\"execution\"", ignoreCase = true) &&
            response.body.contains("authserver/login", ignoreCase = true)

    private fun isLaborLoginResponse(response: HttpResponse): Boolean =
        response.status == 401 || response.status == 403 ||
            response.url.contains("/AuthServer/Login", ignoreCase = true) ||
            response.url.contains("/UnifiedAuth", ignoreCase = true) ||
            response.body.contains("统一身份认证登录")

    private fun request(request: HttpRequest): HttpResponse {
        val connection = URL(request.url).openConnection() as HttpURLConnection
        connection.instanceFollowRedirects = false
        connection.connectTimeout = 30_000
        connection.readTimeout = 30_000
        connection.requestMethod = request.method
        connection.setRequestProperty("User-Agent", USER_AGENT)
        connection.setRequestProperty("Accept-Encoding", "identity")
        connection.setRequestProperty("Accept", request.accept)
        if (request.requestedWith) {
            connection.setRequestProperty("X-Requested-With", "XMLHttpRequest")
        }
        if (request.upgradeInsecureRequests) {
            connection.setRequestProperty("Upgrade-Insecure-Requests", "1")
        }
        if (cookies.isNotEmpty()) {
            connection.setRequestProperty(
                "Cookie",
                cookies.entries.joinToString("; ") { "${it.key}=${it.value}" },
            )
        }
        request.referer?.let { connection.setRequestProperty("Referer", it) }
        request.origin?.let { connection.setRequestProperty("Origin", it) }
        if (request.body != null) {
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", request.contentType)
            connection.outputStream.use { it.write(request.body.toByteArray(StandardCharsets.UTF_8)) }
        }
        val status = connection.responseCode
        connection.headerFields.entries
            .firstOrNull { it.key?.equals("Set-Cookie", ignoreCase = true) == true }
            ?.value
            .orEmpty()
            .forEach { raw ->
                raw.substringBefore(';')
                    .split('=', limit = 2)
                    .takeIf { it.size == 2 }
                    ?.let { cookies[it[0]] = it[1] }
            }
        val stream = if (status >= 400) connection.errorStream else connection.inputStream
        val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        val headers = connection.headerFields.entries.mapNotNull { (key, values) -> key?.lowercase()?.let { it to values.firstOrNull().orEmpty() } }.toMap()
        Log.d(LOG_TAG, "http ${request.method} ${request.url.substringBefore('?')} -> $status location=${headers["location"] != null}")
        return HttpResponse(status, body, headers, connection.url.toString())
    }

    private fun classifyCredential(body: JsonObject): Nothing {
        val message = body["message"]?.jsonPrimitive?.content
        throw CredentialException(message ?: "服务器未返回断言参数")
    }

    private fun parseJsonObject(body: String): JsonObject? =
        runCatching { json.parseToJsonElement(body) as? JsonObject }.getOrNull()

    private fun JsonObject.section(name: String): JsonObject? =
        ((this["datas"] as? JsonObject)?.get(name) as? JsonObject)

    private fun JsonObject.sectionRows(name: String): JsonArray? =
        section(name)?.get("rows") as? JsonArray

    private fun JsonObject.text(vararg keys: String): String? = keys.asSequence()
        .mapNotNull { key ->
            sequenceOf("${key}_DISPLAY", key).mapNotNull { candidate ->
                runCatching { this[candidate]?.jsonPrimitive?.content?.trim() }.getOrNull()
            }.firstOrNull { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
        }
        .firstOrNull()

    private fun JsonObject.rawText(vararg keys: String): String? = keys.asSequence()
        .mapNotNull { key ->
            runCatching { this[key]?.jsonPrimitive?.content?.trim() }.getOrNull()
        }
        .firstOrNull { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }

    private fun JsonObject.toImportCourse(): CourseImportExport.ImportCourseJsonModel? {
        val name = text("KCM") ?: return null
        val day = rawText("SKXQ")?.toIntOrNull()?.takeIf { it in 1..7 } ?: return null
        val startSection = rawText("KSJC")?.toIntOrNull()?.takeIf { it > 0 } ?: return null
        val endSection = rawText("JSJC")?.toIntOrNull()?.takeIf { it >= startSection } ?: return null
        val weeks = rawText("SKZC").orEmpty().mapIndexedNotNull { weekIndex, value ->
            (weekIndex + 1).takeIf { value == '1' }
        }
        if (weeks.isEmpty()) return null
        val campus = text("XXXQDM").orEmpty()
        val room = text("JASMC").orEmpty()
        val position = when {
            campus.isNotBlank() && room.isNotBlank() -> "$room（$campus）"
            room.isNotBlank() -> room
            campus.isNotBlank() -> campus
            else -> "待定"
        }
        val teacher = text("SKJS").orEmpty()
            .split(Regex("[\\/、,，]"))
            .firstOrNull()
            ?.trim()
            .orEmpty()
            .ifBlank { "未知" }
        return CourseImportExport.ImportCourseJsonModel(
            id = null,
            name = name,
            teacher = teacher,
            position = position,
            day = day,
            startSection = startSection,
            endSection = endSection,
            weeks = weeks,
        )
    }

    private fun defaultNuistTimeSlots(): List<CourseImportExport.TimeSlotJsonModel> = listOf(
        CourseImportExport.TimeSlotJsonModel(1, "08:00", "08:45"),
        CourseImportExport.TimeSlotJsonModel(2, "08:55", "09:40"),
        CourseImportExport.TimeSlotJsonModel(3, "10:10", "10:55"),
        CourseImportExport.TimeSlotJsonModel(4, "11:05", "11:50"),
        CourseImportExport.TimeSlotJsonModel(5, "13:45", "14:30"),
        CourseImportExport.TimeSlotJsonModel(6, "14:40", "15:25"),
        CourseImportExport.TimeSlotJsonModel(7, "15:55", "16:40"),
        CourseImportExport.TimeSlotJsonModel(8, "16:50", "17:35"),
        CourseImportExport.TimeSlotJsonModel(9, "18:45", "19:30"),
        CourseImportExport.TimeSlotJsonModel(10, "19:40", "20:25"),
        CourseImportExport.TimeSlotJsonModel(11, "20:35", "21:20"),
        CourseImportExport.TimeSlotJsonModel(12, "21:25", "22:00"),
    )

    private fun JsonObject?.stringValue(key: String): String =
        this?.get(key)?.jsonPrimitive?.content?.trim().orEmpty()

    private data class HttpRequest(
        val method: String,
        val url: String,
        val body: String? = null,
        val contentType: String = "",
        val referer: String? = null,
        val origin: String? = null,
        val accept: String = "*/*",
        val requestedWith: Boolean = false,
        val upgradeInsecureRequests: Boolean = false,
    ) {
        companion object {
            fun get(url: String) = HttpRequest("GET", url, accept = "text/html,application/xhtml+xml")
            fun getJson(url: String) = HttpRequest("GET", url, accept = "application/json")
            fun postJson(url: String, body: String, referer: String) = HttpRequest("POST", url, body, "application/json;charset=utf-8", referer, ORIGIN, "application/json, text/javascript, */*; q=0.01", requestedWith = true)
            fun postForm(
                url: String,
                form: Map<String, String?>,
                referer: String,
                origin: String = ORIGIN,
                accept: String = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
            ) = HttpRequest(
                "POST",
                url,
                form.entries.joinToString("&") { "${encode(it.key)}=${encode(it.value.orEmpty())}" },
                "application/x-www-form-urlencoded; charset=UTF-8",
                referer,
                origin,
                accept,
                requestedWith = true,
            )
            private fun encode(value: String) = URLEncoder.encode(value, StandardCharsets.UTF_8.name())
        }
    }

    private data class HttpResponse(val status: Int, val body: String, val headers: Map<String, String>, val url: String)
    private class CredentialException(message: String) : IOException(message)

    private fun jsonString(value: String) = Json.encodeToString(value)
    private fun sha256(value: ByteArray) = MessageDigest.getInstance("SHA-256").digest(value)
    private fun base64Url(value: ByteArray) = Base64.encodeToString(value, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    private fun encodeService(value: String) = URLEncoder.encode(value, StandardCharsets.UTF_8.name()).replace("%3A", ":").replace("%2F", "/")
    private fun unescapeHtml(value: String) = value.replace("&amp;", "&").replace("&quot;", "\"").replace("&#39;", "'").replace("&#x2F;", "/")
}

actual fun createPortalSession(repository: PortalCredentialRepository): PortalSession = AndroidPortalSession(repository)
