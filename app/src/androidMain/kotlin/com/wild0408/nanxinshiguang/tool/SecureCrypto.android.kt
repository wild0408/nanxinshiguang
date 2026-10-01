package com.wild0408.nanxinshiguang.tool

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.koin.core.annotation.Single
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

@Single
actual class SecureCrypto {

    private val alias = "ShiguangApiCryptoKeyAlias"
    private val provider = "AndroidKeyStore"
    private val transformation = "AES/GCM/NoPadding"

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(provider).apply { load(null) }
        keyStore.getKey(alias, null)?.let { return it as SecretKey }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, provider
        )
        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    actual fun encrypt(data: String): CryptoResult? {
        if (data.isEmpty()) return null
        return try {
            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))

            CryptoResult(
                encryptedData = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP),
                iv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
            )
        } catch (e: Exception) {
            null
        }
    }

    actual fun decrypt(encryptedData: String, ivString: String): String? {
        if (encryptedData.isEmpty() || ivString.isEmpty()) return null
        return try {
            val cipher = Cipher.getInstance(transformation)
            val ivBytes = Base64.decode(ivString, Base64.NO_WRAP)
            val gcmSpec = GCMParameterSpec(128, ivBytes)

            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), gcmSpec)
            val decryptedBytes = cipher.doFinal(Base64.decode(encryptedData, Base64.NO_WRAP))
            String(decryptedBytes, Charsets.UTF_8)
                .replace("\u0000", "")
                .trim()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 设备密钥库里的密钥无法导出，因此跨设备迁移改用口令派生密钥。
     * 这里使用 JDK 的 [java.util.Base64]（minSdk 26 起可用），与设备密钥库路径的
     * `android.util.Base64` 相互独立，便于在 JVM 单元测试中直接验证往返结果。
     */
    actual fun encryptWithPassword(data: String, password: String, iterations: Int): PasswordCryptoResult? {
        if (data.isEmpty() || password.isEmpty()) return null
        return try {
            val salt = ByteArray(PASSWORD_SALT_SIZE).also { SecureRandom().nextBytes(it) }
            val key = derivePasswordKey(password, salt, iterations)
            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val encryptedBytes = cipher.doFinal(data.toByteArray(Charsets.UTF_8))

            PasswordCryptoResult(
                encryptedData = java.util.Base64.getEncoder().encodeToString(encryptedBytes),
                iv = java.util.Base64.getEncoder().encodeToString(cipher.iv),
                salt = java.util.Base64.getEncoder().encodeToString(salt),
                iterations = iterations
            )
        } catch (e: Exception) {
            null
        }
    }

    actual fun decryptWithPassword(
        encryptedData: String,
        ivString: String,
        salt: String,
        iterations: Int,
        password: String
    ): String? {
        if (encryptedData.isEmpty() || ivString.isEmpty() || salt.isEmpty() || password.isEmpty()) return null
        if (iterations <= 0) return null
        return try {
            val decoder = java.util.Base64.getDecoder()
            val key = derivePasswordKey(password, decoder.decode(salt), iterations)
            val cipher = Cipher.getInstance(transformation)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, decoder.decode(ivString)))
            // 这里不做 trim 或补零清理：解密结果是 JSON，任何裁剪都会破坏载荷。
            String(cipher.doFinal(decoder.decode(encryptedData)), Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    private fun derivePasswordKey(password: String, salt: ByteArray, iterations: Int): SecretKey {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, PASSWORD_KEY_BITS)
        val factory = SecretKeyFactory.getInstance(PASSWORD_KEY_ALGORITHM)
        return SecretKeySpec(factory.generateSecret(spec).encoded, KeyProperties.KEY_ALGORITHM_AES)
    }

    private companion object {
        const val PASSWORD_KEY_ALGORITHM = "PBKDF2WithHmacSHA256"
        const val PASSWORD_KEY_BITS = 256
        const val PASSWORD_SALT_SIZE = 16
    }
}
