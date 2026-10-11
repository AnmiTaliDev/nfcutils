// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

import java.io.ByteArrayOutputStream

private const val WIFI_MIME_TYPE = "application/vnd.wfa.wsc"

private const val ATTR_AUTH_TYPE = 0x1003
private const val ATTR_CREDENTIAL = 0x100E
private const val ATTR_ENCRYPTION_TYPE = 0x100F
private const val ATTR_MAC_ADDRESS = 0x1020
private const val ATTR_NETWORK_INDEX = 0x1026
private const val ATTR_NETWORK_KEY = 0x1027
private const val ATTR_SSID = 0x1045
private const val ATTR_VERSION = 0x104A

private const val VERSION_VALUE = 0x10
private const val MAX_SSID_BYTES = 32
private const val MIN_PASSPHRASE_LENGTH = 8
private const val MAX_PASSPHRASE_LENGTH = 63
private const val PSK_HEX_LENGTH = 64

enum class WifiAuth(val authType: Int, val encryptionType: Int) {
    Open(0x0001, 0x0001),
    Wpa2Personal(0x0020, 0x0008),
    WpaWpa2Personal(0x0022, 0x000C),
}

class WifiCredential(
    val ssid: String,
    val authType: Int?,
    val encryptionType: Int?,
    val networkKey: String,
) {
    val auth: WifiAuth? =
        WifiAuth.entries.firstOrNull { it.authType == authType && it.encryptionType == encryptionType }
}

private fun ByteArrayOutputStream.writeShort(value: Int) {
    write(value ushr 8)
    write(value)
}

private fun ByteArrayOutputStream.writeAttribute(type: Int, value: ByteArray) {
    writeShort(type)
    writeShort(value.size)
    write(value)
}

fun buildWifiRecord(ssid: String, password: String, auth: WifiAuth): RecordBuild {
    if (ssid.isEmpty()) return RecordBuild.Invalid(FieldKey.WIFI_SSID, FieldError.Empty)
    val ssidBytes = ssid.toByteArray(Charsets.UTF_8)
    if (ssidBytes.size > MAX_SSID_BYTES) return RecordBuild.Invalid(FieldKey.WIFI_SSID, FieldError.TooLong)
    if (auth != WifiAuth.Open) {
        if (password.isEmpty()) return RecordBuild.Invalid(FieldKey.WIFI_PASSWORD, FieldError.Empty)
        if (password.length < MIN_PASSPHRASE_LENGTH) {
            return RecordBuild.Invalid(FieldKey.WIFI_PASSWORD, FieldError.TooShort)
        }
        if (password.length > PSK_HEX_LENGTH) return RecordBuild.Invalid(FieldKey.WIFI_PASSWORD, FieldError.TooLong)
        if (password.length > MAX_PASSPHRASE_LENGTH && !password.all { Character.digit(it, 16) >= 0 }) {
            return RecordBuild.Invalid(FieldKey.WIFI_PASSWORD, FieldError.InvalidFormat)
        }
    }
    val keyBytes = if (auth == WifiAuth.Open) ByteArray(0) else password.toByteArray(Charsets.UTF_8)

    val credential = ByteArrayOutputStream()
    credential.writeAttribute(ATTR_NETWORK_INDEX, byteArrayOf(1))
    credential.writeAttribute(ATTR_SSID, ssidBytes)
    credential.writeAttribute(ATTR_AUTH_TYPE, byteArrayOf((auth.authType ushr 8).toByte(), auth.authType.toByte()))
    credential.writeAttribute(
        ATTR_ENCRYPTION_TYPE,
        byteArrayOf((auth.encryptionType ushr 8).toByte(), auth.encryptionType.toByte()),
    )
    credential.writeAttribute(ATTR_NETWORK_KEY, keyBytes)
    credential.writeAttribute(ATTR_MAC_ADDRESS, ByteArray(6) { 0xFF.toByte() })

    val payload = ByteArrayOutputStream()
    payload.writeAttribute(ATTR_VERSION, byteArrayOf(VERSION_VALUE.toByte()))
    payload.writeAttribute(ATTR_CREDENTIAL, credential.toByteArray())
    return RecordBuild.Ok(mimeRecord(WIFI_MIME_TYPE, payload.toByteArray()))
}

private fun readAttributes(bytes: ByteArray): Map<Int, ByteArray>? {
    val attributes = LinkedHashMap<Int, ByteArray>()
    var position = 0
    while (position < bytes.size) {
        if (position + 4 > bytes.size) return null
        val type = ((bytes[position].toInt() and 0xFF) shl 8) or (bytes[position + 1].toInt() and 0xFF)
        val length = ((bytes[position + 2].toInt() and 0xFF) shl 8) or (bytes[position + 3].toInt() and 0xFF)
        position += 4
        if (position + length > bytes.size) return null
        attributes.putIfAbsent(type, bytes.copyOfRange(position, position + length))
        position += length
    }
    return attributes
}

private fun ByteArray.toUnsignedShort(): Int? =
    if (size == 2) ((this[0].toInt() and 0xFF) shl 8) or (this[1].toInt() and 0xFF) else null

fun parseWifiPayload(payload: ByteArray): WifiCredential? {
    val outer = readAttributes(payload) ?: return null
    val credential = readAttributes(outer[ATTR_CREDENTIAL] ?: return null) ?: return null
    val ssid = credential[ATTR_SSID] ?: return null
    return WifiCredential(
        ssid = String(ssid, Charsets.UTF_8),
        authType = credential[ATTR_AUTH_TYPE]?.toUnsignedShort(),
        encryptionType = credential[ATTR_ENCRYPTION_TYPE]?.toUnsignedShort(),
        networkKey = credential[ATTR_NETWORK_KEY]?.let { String(it, Charsets.UTF_8) }.orEmpty(),
    )
}

fun isWifiMimeType(mimeType: String): Boolean = mimeType == WIFI_MIME_TYPE
