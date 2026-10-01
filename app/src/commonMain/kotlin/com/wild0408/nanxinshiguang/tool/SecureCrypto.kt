package com.wild0408.nanxinshiguang.tool

data class CryptoResult(
    val encryptedData: String,
    val iv: String
)

/**
 * 使用调用方口令派生的对称加密结果。
 * 与 [CryptoResult] 不同，这里的密钥不依赖设备密钥库，因此结果可以随文件迁移到其他设备。
 */
data class PasswordCryptoResult(
    val encryptedData: String,
    val iv: String,
    val salt: String,
    val iterations: Int
)

/** 口令派生默认迭代次数，导入时以文件内记录的值为准。 */
const val DEFAULT_PBKDF2_ITERATIONS: Int = 120_000

expect class SecureCrypto() {
    fun encrypt(data: String): CryptoResult?
    fun decrypt(encryptedData: String, ivString: String): String?

    /**
     * 使用口令加密数据，结果可在其他设备上通过 [decryptWithPassword] 解密。
     * 用于导出需要在设备之间迁移的凭据；口令不会以任何形式保存。
     */
    fun encryptWithPassword(
        data: String,
        password: String,
        iterations: Int = DEFAULT_PBKDF2_ITERATIONS
    ): PasswordCryptoResult?

    /**
     * 解密由 [encryptWithPassword] 生成的数据。
     * 口令错误、盐值或迭代次数不匹配、密文被篡改时返回 null。
     */
    fun decryptWithPassword(
        encryptedData: String,
        ivString: String,
        salt: String,
        iterations: Int,
        password: String
    ): String?
}
