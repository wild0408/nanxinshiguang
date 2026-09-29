package com.wild0408.nanxinshiguang.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PortalUserProfile(
    val name: String,
    val studentId: String,
    val categoryName: String = "",
    val departmentName: String = "",
    val className: String = "",
    val fetchedAt: Long,
) {
    val organizationLine: String
        get() = listOf(departmentName, className)
            .filter { it.isNotBlank() }
            .joinToString(" · ")

    val initial: String
        get() = name.trim().firstOrNull()?.toString() ?: "?"
}
