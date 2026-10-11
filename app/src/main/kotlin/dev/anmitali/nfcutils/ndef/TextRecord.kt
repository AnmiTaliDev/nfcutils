// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

private const val MAX_LANGUAGE_LENGTH = 63
private const val FLAG_UTF16 = 0x80
private const val MASK_LANGUAGE_LENGTH = 0x3F

class TextContent(val languageCode: String, val utf16: Boolean, val text: String)

fun buildTextRecord(text: String, languageCode: String, utf16: Boolean): RecordBuild {
    if (text.isEmpty()) return RecordBuild.Invalid(FieldKey.TEXT, FieldError.Empty)
    val language = languageCode.trim()
    if (language.isEmpty()) return RecordBuild.Invalid(FieldKey.LANGUAGE, FieldError.Empty)
    if (language.length > MAX_LANGUAGE_LENGTH) return RecordBuild.Invalid(FieldKey.LANGUAGE, FieldError.TooLong)
    if (!language.all { it in 'A'..'Z' || it in 'a'..'z' || it in '0'..'9' || it == '-' }) {
        return RecordBuild.Invalid(FieldKey.LANGUAGE, FieldError.InvalidFormat)
    }
    val languageBytes = language.toByteArray(Charsets.US_ASCII)
    val textBytes = text.toByteArray(if (utf16) Charsets.UTF_16 else Charsets.UTF_8)
    val status = (if (utf16) FLAG_UTF16 else 0) or languageBytes.size
    val payload = byteArrayOf(status.toByte()) + languageBytes + textBytes
    return RecordBuild.Ok(wellKnownRecord("T", payload))
}

fun parseTextPayload(payload: ByteArray): TextContent? {
    if (payload.isEmpty()) return null
    val status = payload[0].toInt() and 0xFF
    val languageLength = status and MASK_LANGUAGE_LENGTH
    if (payload.size < 1 + languageLength) return null
    val utf16 = status and FLAG_UTF16 != 0
    val language = String(payload, 1, languageLength, Charsets.US_ASCII)
    val charset = if (utf16) Charsets.UTF_16 else Charsets.UTF_8
    val text = String(payload, 1 + languageLength, payload.size - 1 - languageLength, charset)
    return TextContent(language, utf16, text)
}
