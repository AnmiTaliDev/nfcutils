// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

private const val TEXT_PREVIEW_LIMIT = 256

private fun ByteArray.asciiString(): String = String(this, Charsets.ISO_8859_1)

fun parseSmartPoster(payload: ByteArray): ParsedRecord.SmartPoster? {
    val nested = NdefCodec.decode(payload) ?: return null
    var title: String? = null
    var uri: String? = null
    for (record in nested) {
        if (record.tnf != Tnf.WELL_KNOWN) continue
        when (record.type.asciiString()) {
            "T" -> if (title == null) title = parseTextPayload(record.payload)?.text
            "U" -> if (uri == null) uri = decodeUriPayload(record.payload)
        }
    }
    return ParsedRecord.SmartPoster(title, uri)
}

fun parseRecord(record: NdefRecordData): ParsedRecord {
    val type = record.type.asciiString()
    return when (record.tnf) {
        Tnf.EMPTY -> ParsedRecord.Empty
        Tnf.WELL_KNOWN -> when (type) {
            "T" -> parseTextPayload(record.payload)?.let { ParsedRecord.Text(it) }
            "U" -> decodeUriPayload(record.payload)?.let { ParsedRecord.Uri(it) }
            "Sp" -> parseSmartPoster(record.payload)
            else -> null
        }
        Tnf.MIME -> parseMime(type, record.payload)
        Tnf.ABSOLUTE_URI -> ParsedRecord.Uri(String(record.type, Charsets.UTF_8))
        Tnf.EXTERNAL -> if (type == ANDROID_APP_TYPE && record.payload.isNotEmpty()) {
            ParsedRecord.AndroidApp(String(record.payload, Charsets.UTF_8))
        } else {
            ParsedRecord.External(type)
        }
        else -> null
    } ?: ParsedRecord.Unknown(record.tnf, record.type.toHex(""))
}

private fun parseMime(type: String, payload: ByteArray): ParsedRecord {
    if (isWifiMimeType(type)) {
        parseWifiPayload(payload)?.let { return ParsedRecord.Wifi(it) }
    }
    val preview = if (type.startsWith("text/")) {
        String(payload, 0, minOf(payload.size, TEXT_PREVIEW_LIMIT), Charsets.UTF_8)
    } else {
        null
    }
    return ParsedRecord.Mime(type, preview)
}
