// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

private val UriPrefixes = listOf(
    "",
    "http://www.",
    "https://www.",
    "http://",
    "https://",
    "tel:",
    "mailto:",
    "ftp://anonymous:anonymous@",
    "ftp://ftp.",
    "ftps://",
    "sftp://",
    "smb://",
    "nfs://",
    "ftp://",
    "dav://",
    "news:",
    "telnet://",
    "imap:",
    "rtsp://",
    "urn:",
    "pop:",
    "sip:",
    "sips:",
    "tftp:",
    "btspp://",
    "btl2cap://",
    "btgoep://",
    "tcpobex://",
    "irdaobex://",
    "file://",
    "urn:epc:id:",
    "urn:epc:tag:",
    "urn:epc:pat:",
    "urn:epc:raw:",
    "urn:epc:",
    "urn:nfc:",
)

private val SchemePattern = Regex("^[A-Za-z][A-Za-z0-9+.-]*:.+")

fun encodeUriPayload(uri: String): ByteArray {
    var bestIndex = 0
    for (index in 1 until UriPrefixes.size) {
        if (uri.startsWith(UriPrefixes[index]) && UriPrefixes[index].length > UriPrefixes[bestIndex].length) {
            bestIndex = index
        }
    }
    val rest = uri.substring(UriPrefixes[bestIndex].length)
    return byteArrayOf(bestIndex.toByte()) + rest.toByteArray(Charsets.UTF_8)
}

fun decodeUriPayload(payload: ByteArray): String? {
    if (payload.isEmpty()) return null
    val index = payload[0].toInt() and 0xFF
    if (index >= UriPrefixes.size) return null
    return UriPrefixes[index] + String(payload, 1, payload.size - 1, Charsets.UTF_8)
}

fun uriRecord(uri: String): NdefRecordData = wellKnownRecord("U", encodeUriPayload(uri))

fun buildUriRecord(uri: String): RecordBuild {
    val trimmed = uri.trim()
    if (trimmed.isEmpty()) return RecordBuild.Invalid(FieldKey.URL, FieldError.Empty)
    if (!SchemePattern.matches(trimmed) || trimmed.any { it.isWhitespace() }) {
        return RecordBuild.Invalid(FieldKey.URL, FieldError.InvalidFormat)
    }
    return RecordBuild.Ok(uriRecord(trimmed))
}
