package com.wild0408.nanxinshiguang.data.portal

import com.wild0408.nanxinshiguang.data.model.PortalPasskeyBundle
import com.wild0408.nanxinshiguang.data.model.PortalUserProfile
import com.wild0408.nanxinshiguang.tool.SecureCrypto
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PortalCredentialTransferTest {

    private val transfer = PortalCredentialTransfer(SecureCrypto())
    private val json = Json { ignoreUnknownKeys = true }

    private val bundle = PortalPasskeyBundle(
        rpId = "authserver.nuist.edu.cn",
        credentialId = "credential-1",
        privateKeyPkcs8Pem = "-----BEGIN PRIVATE KEY-----\nMIIB\n-----END PRIVATE KEY-----",
        userId = "MjAyMzAwMDAwMDA=",
        anonBiometricsId = "anon-1",
        deviceName = "拾光(Android)",
        createdAt = 1_700_000_000_000,
    )

    private val profile = PortalUserProfile(
        name = "测试用户",
        studentId = "20230000000",
        departmentName = "计算机学院",
        fetchedAt = 1_700_000_000_000,
    )

    private fun envelopeOf(bytes: ByteArray): MutableMap<String, kotlinx.serialization.json.JsonElement> =
        json.parseToJsonElement(bytes.decodeToString()).jsonObject.toMutableMap()

    private fun encode(map: Map<String, kotlinx.serialization.json.JsonElement>): ByteArray =
        Json.encodeToString(JsonObject(map)).toByteArray(Charsets.UTF_8)

    @Test
    fun exportThenImportRestoresBundleAndProfile() {
        val exported = transfer.export(bundle, profile, "portal-pass-123")
        assertTrue(exported != null && exported.isNotEmpty())

        val result = transfer.import(exported, "portal-pass-123")
        val success = assertIs<PortalCredentialImportResult.Success>(result)
        assertEquals(bundle, success.bundle)
        assertEquals(profile, success.profile)
    }

    @Test
    fun importWithoutProfileKeepsProfileNull() {
        val exported = transfer.export(bundle, null, "portal-pass-123")!!
        val success = assertIs<PortalCredentialImportResult.Success>(
            transfer.import(exported, "portal-pass-123")
        )
        assertEquals(bundle, success.bundle)
        assertNull(success.profile)
    }

    @Test
    fun wrongPasswordIsRejected() {
        val exported = transfer.export(bundle, profile, "portal-pass-123")!!
        assertIs<PortalCredentialImportResult.WrongPassword>(
            transfer.import(exported, "portal-pass-124")
        )
    }

    @Test
    fun tamperedCipherTextIsRejected() {
        val exported = transfer.export(bundle, profile, "portal-pass-123")!!
        val map = envelopeOf(exported)
        map["cipherData"] = JsonPrimitive("AAAA")
        assertIs<PortalCredentialImportResult.WrongPassword>(
            transfer.import(encode(map), "portal-pass-123")
        )
    }

    @Test
    fun foreignJsonFileIsRejected() {
        val exported = transfer.export(bundle, profile, "portal-pass-123")!!
        val map = envelopeOf(exported)
        map["format"] = JsonPrimitive("some.other.app.backup")
        assertIs<PortalCredentialImportResult.Invalid>(
            transfer.import(encode(map), "portal-pass-123")
        )
    }

    @Test
    fun unsupportedVersionIsRejected() {
        val exported = transfer.export(bundle, profile, "portal-pass-123")!!
        val map = envelopeOf(exported)
        map["version"] = JsonPrimitive(PORTAL_BACKUP_VERSION + 1)
        assertIs<PortalCredentialImportResult.Invalid>(
            transfer.import(encode(map), "portal-pass-123")
        )
    }

    @Test
    fun bundleMissingRequiredFieldsIsRejected() {
        val exported = transfer.export(bundle.copy(credentialId = ""), null, "portal-pass-123")!!
        assertIs<PortalCredentialImportResult.Invalid>(
            transfer.import(exported, "portal-pass-123")
        )
    }

    @Test
    fun shortPasswordCannotExport() {
        assertNull(transfer.export(bundle, profile, "1234567"))
    }

    @Test
    fun emptyFileIsRejected() {
        assertIs<PortalCredentialImportResult.Invalid>(transfer.import(ByteArray(0), "portal-pass-123"))
    }

    @Test
    fun defaultFileNameContainsDateAndJsonExtension() {
        val name = transfer.defaultFileName(now = 1_700_000_000_000)
        assertTrue(name.startsWith("nanxinshiguang-portal-key-"), name)
        assertTrue(name.endsWith(".json"), name)
    }
}
