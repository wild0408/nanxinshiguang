package com.wild0408.nanxinshiguang.data.model

import kotlinx.serialization.Serializable

/**
 * 统一门户密钥的导出信封。
 *
 * 信封本身只有 KDF 参数与密文，明文凭据全部位于 [cipherData] 内，
 * 因此文件可以直接通过聊天工具或网盘迁移到另一台设备。
 */
@Serializable
data class PortalCredentialBackupEnvelope(
    val format: String,
    val version: Int,
    val createdAt: Long,
    val kdfAlgorithm: String,
    val kdfIterations: Int,
    val kdfSalt: String,
    val cipherAlgorithm: String,
    val cipherIv: String,
    val cipherData: String
)

/**
 * 信封解密后的载荷：通行密钥凭据 + 可选的本地缓存身份信息。
 * 带上身份信息可以让新设备在离线状态下也立刻显示账号，不需要重新请求门户。
 */
@Serializable
data class PortalCredentialBackupPayload(
    val bundle: PortalPasskeyBundle,
    val profile: PortalUserProfile? = null
)
