package com.wild0408.nanxinshiguang.data.model

import kotlinx.serialization.Serializable
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Serializable
data class PortalPasskeyBundle(
    val rpId: String,
    val credentialId: String,
    val privateKeyPkcs8Pem: String,
    val userId: String,
    val anonBiometricsId: String,
    val deviceName: String,
    val createdAt: Long,
) {
    @OptIn(ExperimentalEncodingApi::class)
    val studentId: String?
        get() = runCatching {
            val normalized = userId.replace('-', '+').replace('_', '/')
                .padEnd(((userId.length + 3) / 4) * 4, '=')
            Base64.decode(normalized).decodeToString().takeIf { it.matches(Regex("\\d{6,20}")) }
        }.getOrNull()
}
