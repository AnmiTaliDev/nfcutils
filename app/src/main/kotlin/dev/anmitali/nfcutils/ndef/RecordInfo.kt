// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

private const val SUMMARY_LIMIT = 80
private const val ELLIPSIS = "…"

class RecordInfo(
    val kind: ParsedKind,
    val summary: String,
    val detail: String,
    val payloadHex: String,
)

fun truncate(value: String, limit: Int = SUMMARY_LIMIT): String {
    val singleLine = value.replace(Regex("\\s+"), " ").trim()
    return if (singleLine.length <= limit) singleLine else singleLine.take(limit - 1) + ELLIPSIS
}

fun recordSummary(parsed: ParsedRecord): String = when (parsed) {
    ParsedRecord.Empty -> ""
    is ParsedRecord.Text -> truncate(parsed.content.text)
    is ParsedRecord.Uri -> truncate(parsed.uri)
    is ParsedRecord.Mime -> parsed.mimeType
    is ParsedRecord.Wifi -> truncate(parsed.credential.ssid)
    is ParsedRecord.SmartPoster -> truncate(parsed.title ?: parsed.uri.orEmpty())
    is ParsedRecord.AndroidApp -> parsed.packageName
    is ParsedRecord.External -> parsed.type
    is ParsedRecord.Unknown -> parsed.type
}

fun recordDetail(parsed: ParsedRecord): String = when (parsed) {
    is ParsedRecord.Text -> "${parsed.content.languageCode}, ${if (parsed.content.utf16) "UTF-16" else "UTF-8"}"
    else -> ""
}

fun describeRecord(record: NdefRecordData): RecordInfo {
    val parsed = parseRecord(record)
    return RecordInfo(
        kind = parsed.kind,
        summary = recordSummary(parsed),
        detail = recordDetail(parsed),
        payloadHex = record.payload.toHex(),
    )
}

fun entrySummary(records: List<RecordInfo>): String =
    records.map { it.summary }.filter { it.isNotEmpty() }.joinToString("; ")
