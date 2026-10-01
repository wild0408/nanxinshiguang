package com.wild0408.nanxinshiguang.data.portal

import com.wild0408.nanxinshiguang.data.model.PortalCredentialBackupEnvelope
import com.wild0408.nanxinshiguang.data.model.PortalCredentialBackupPayload
import com.wild0408.nanxinshiguang.data.model.PortalPasskeyBundle
import com.wild0408.nanxinshiguang.data.model.PortalUserProfile
import com.wild0408.nanxinshiguang.tool.DEFAULT_PBKDF2_ITERATIONS
import com.wild0408.nanxinshiguang.tool.SecureCrypto
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import kotlin.time.Clock
import kotlin.time.Instant

/** 密钥文件格式标识，用于在导入时拒绝无关的 JSON 文件。 */
const val PORTAL_BACKUP_FORMAT: String = "nanxinshiguang.portal.credential"
const val PORTAL_BACKUP_VERSION: Int = 1

/** 导出密码的最短长度；过短的密码会让离线暴力破解变得廉价。 */
const val PORTAL_BACKUP_MIN_PASSWORD_LENGTH: Int = 8

/** 单次解密允许的最大迭代次数，避免恶意文件用超高迭代拖死导入流程。 */
private const val PORTAL_BACKUP_MAX_ITERATIONS: Int = 5_000_000

private const val PORTAL_BACKUP_KDF_ALGORITHM: String = "PBKDF2WithHmacSHA256"
private const val PORTAL_BACKUP_CIPHER_ALGORITHM: String = "AES/GCM/NoPadding"

sealed interface PortalCredentialImportResult {
    data class Success(
        val bundle: PortalPasskeyBundle,
        val profile: PortalUserProfile?
    ) : PortalCredentialImportResult

    /** 口令错误，或文件在传输过程中损坏（GCM 校验失败无法区分两者）。 */
    data object WrongPassword : PortalCredentialImportResult

    data class Invalid(val message: String) : PortalCredentialImportResult
}

/**
 * 统一门户密钥的跨设备导出与导入。
 *
 * 本机保存的凭据由设备密钥库加密，换设备无法直接复用；导出时改用调用方设置的口令
 * 派生密钥（PBKDF2 + AES-GCM）重新加密，导入时再用本机密钥库重新落库。
 */
@Single
class PortalCredentialTransfer(
    private val secureCrypto: SecureCrypto
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    /** 导出文件名，含日期便于用户区分多次导出。 */
    fun defaultFileName(now: Long = Clock.System.now().toEpochMilliseconds()): String {
        val date = Instant.fromEpochMilliseconds(now)
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
        return "nanxinshiguang-portal-key-$date.json"
    }

    /** 生成可直接写入文件的导出数据；加密失败时返回 null。 */
    fun export(
        bundle: PortalPasskeyBundle,
        profile: PortalUserProfile?,
        password: String
    ): ByteArray? {
        if (password.length < PORTAL_BACKUP_MIN_PASSWORD_LENGTH) return null
        val payload = PortalCredentialBackupPayload(bundle = bundle, profile = profile)
        val plain = json.encodeToString(payload)
        val encrypted = secureCrypto.encryptWithPassword(
            data = plain,
            password = password,
            iterations = DEFAULT_PBKDF2_ITERATIONS
        ) ?: return null

        val envelope = PortalCredentialBackupEnvelope(
            format = PORTAL_BACKUP_FORMAT,
            version = PORTAL_BACKUP_VERSION,
            createdAt = Clock.System.now().toEpochMilliseconds(),
            kdfAlgorithm = PORTAL_BACKUP_KDF_ALGORITHM,
            kdfIterations = encrypted.iterations,
            kdfSalt = encrypted.salt,
            cipherAlgorithm = PORTAL_BACKUP_CIPHER_ALGORITHM,
            cipherIv = encrypted.iv,
            cipherData = encrypted.encryptedData
        )
        return json.encodeToString(envelope).toByteArray(Charsets.UTF_8)
    }

    fun import(bytes: ByteArray, password: String): PortalCredentialImportResult {
        if (bytes.isEmpty()) return PortalCredentialImportResult.Invalid("密钥文件为空")
        val text = bytes.decodeToString().removePrefix("\uFEFF").trim()
        if (text.isEmpty()) return PortalCredentialImportResult.Invalid("密钥文件为空")

        val envelope = runCatching {
            json.decodeFromString<PortalCredentialBackupEnvelope>(text)
        }.getOrElse {
            return PortalCredentialImportResult.Invalid("不是有效的南信拾光门户密钥文件")
        }

        if (envelope.format != PORTAL_BACKUP_FORMAT) {
            return PortalCredentialImportResult.Invalid("不是南信拾光的门户密钥文件")
        }
        if (envelope.version <= 0 || envelope.version > PORTAL_BACKUP_VERSION) {
            return PortalCredentialImportResult.Invalid("密钥文件版本不受支持，请先升级应用")
        }
        if (envelope.kdfAlgorithm != PORTAL_BACKUP_KDF_ALGORITHM ||
            envelope.cipherAlgorithm != PORTAL_BACKUP_CIPHER_ALGORITHM
        ) {
            return PortalCredentialImportResult.Invalid("密钥文件使用了不受支持的加密方式")
        }
        if (envelope.kdfIterations <= 0 || envelope.kdfIterations > PORTAL_BACKUP_MAX_ITERATIONS) {
            return PortalCredentialImportResult.Invalid("密钥文件的加密参数异常")
        }

        val plain = secureCrypto.decryptWithPassword(
            encryptedData = envelope.cipherData,
            ivString = envelope.cipherIv,
            salt = envelope.kdfSalt,
            iterations = envelope.kdfIterations,
            password = password
        ) ?: return PortalCredentialImportResult.WrongPassword

        val payload = runCatching {
            json.decodeFromString<PortalCredentialBackupPayload>(plain)
        }.getOrElse {
            return PortalCredentialImportResult.Invalid("密钥内容无法解析")
        }

        val bundle = payload.bundle
        if (bundle.rpId.isBlank() ||
            bundle.credentialId.isBlank() ||
            bundle.privateKeyPkcs8Pem.isBlank() ||
            bundle.userId.isBlank()
        ) {
            return PortalCredentialImportResult.Invalid("密钥缺少必要凭据")
        }
        return PortalCredentialImportResult.Success(bundle = bundle, profile = payload.profile)
    }
}
