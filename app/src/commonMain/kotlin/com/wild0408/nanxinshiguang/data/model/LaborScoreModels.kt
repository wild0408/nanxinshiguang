package com.wild0408.nanxinshiguang.data.model

import kotlinx.serialization.Serializable
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@Serializable
data class LaborOfficialResult(
    val theory: String, val life: String, val service: String,
    val majorCourse: String, val majorContest: String, val majorTotal: String,
    val total: String, val confirmed: String, val updatedAt: String,
    val filed: String, val filedAt: String,
) {
    val isConfirmed get() = confirmed == "是"
    val isFiled get() = filed == "是"
}

@Serializable
data class LaborMajorLive(val course: String, val contest: String, val total: String)

@Serializable
data class LaborScore(
    val official: LaborOfficialResult?,
    val liveLife: String?,
    val liveService: String?,
    val liveMajor: LaborMajorLive?,
    val fetchedAt: Long,
) {
    val hasOfficialData: Boolean
        get() = official?.let { listOf(it.total, it.theory, it.life, it.service, it.majorCourse, it.majorContest, it.majorTotal).any(String::isNotBlank) } == true

    val hasLiveData: Boolean
        get() = !liveLife.isNullOrBlank() || !liveService.isNullOrBlank() ||
            liveMajor?.let { listOf(it.course, it.contest, it.total).any(String::isNotBlank) } == true

    val hasData: Boolean get() = hasOfficialData || hasLiveData

    val officialTotalDisplay: String get() = official?.total?.trim().orEmpty().ifBlank { "--" }
}

fun LaborScore.updatedAtText(): String = if (fetchedAt <= 0) "--" else
    Instant.fromEpochMilliseconds(fetchedAt).toLocalDateTime(TimeZone.currentSystemDefault())
        .toString().replace('T', ' ').take(16)
