// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

import java.io.ByteArrayOutputStream

private fun draft(kind: DraftKind, vararg fields: Pair<String, String>) = RecordDraft(kind, mapOf(*fields))

fun percentDecode(value: String): String {
    val out = ByteArrayOutputStream()
    var index = 0
    while (index < value.length) {
        val char = value[index]
        val high = if (char == '%' && index + 2 < value.length) Character.digit(value[index + 1], 16) else -1
        val low = if (high >= 0) Character.digit(value[index + 2], 16) else -1
        if (high >= 0 && low >= 0) {
            out.write((high shl 4) or low)
            index += 3
        } else {
            out.write(char.toString().toByteArray(Charsets.UTF_8))
            index++
        }
    }
    return String(out.toByteArray(), Charsets.UTF_8)
}

private fun queryParameter(query: String, name: String): String {
    for (pair in query.split('&')) {
        if (pair.substringBefore('=').equals(name, ignoreCase = true)) {
            return percentDecode(pair.substringAfter('=', ""))
        }
    }
    return ""
}

private fun uriDraft(uri: String): RecordDraft {
    val urlDraft = draft(DraftKind.Url, FieldKey.URL to uri)
    if (uri.startsWith("tel:", ignoreCase = true)) {
        val number = cleanPhoneNumber(uri.substring(4))
        return if (isValidPhoneNumber(number)) draft(DraftKind.Phone, FieldKey.PHONE to number) else urlDraft
    }
    if (uri.startsWith("sms:", ignoreCase = true)) {
        val rest = uri.substring(4)
        val number = cleanPhoneNumber(rest.substringBefore('?'))
        if (!isValidPhoneNumber(number)) return urlDraft
        return draft(
            DraftKind.Sms,
            FieldKey.SMS_NUMBER to number,
            FieldKey.SMS_BODY to queryParameter(rest.substringAfter('?', ""), "body"),
        )
    }
    if (uri.startsWith("mailto:", ignoreCase = true)) {
        val rest = uri.substring(7)
        val query = rest.substringAfter('?', "")
        return draft(
            DraftKind.Email,
            FieldKey.EMAIL_ADDRESS to percentDecode(rest.substringBefore('?')),
            FieldKey.EMAIL_SUBJECT to queryParameter(query, "subject"),
            FieldKey.EMAIL_BODY to queryParameter(query, "body"),
        )
    }
    if (uri.startsWith("geo:", ignoreCase = true)) {
        val parts = uri.substring(4).substringBefore(';').substringBefore('?').split(',')
        if (parts.size >= 2 && parts[0].toBigDecimalOrNull() != null && parts[1].toBigDecimalOrNull() != null) {
            return draft(DraftKind.Geo, FieldKey.LATITUDE to parts[0].trim(), FieldKey.LONGITUDE to parts[1].trim())
        }
    }
    return urlDraft
}

private fun wifiDraft(credential: WifiCredential): RecordDraft {
    val security = when (credential.auth) {
        WifiAuth.Open -> FieldValue.SECURITY_OPEN
        WifiAuth.WpaWpa2Personal -> FieldValue.SECURITY_WPA_WPA2
        else -> FieldValue.SECURITY_WPA2
    }
    return draft(
        DraftKind.Wifi,
        FieldKey.WIFI_SSID to credential.ssid,
        FieldKey.WIFI_PASSWORD to credential.networkKey,
        FieldKey.WIFI_SECURITY to security,
    )
}

private fun mimeDrafts(record: NdefRecordData, parsed: ParsedRecord.Mime): List<RecordDraft> {
    val text = String(record.payload, Charsets.UTF_8)
    val type = parsed.mimeType
    if (type == "text/vcard" || type == "text/x-vcard") {
        val card = parseVCard(text)
        if (card != null && (card.firstName.isNotEmpty() || card.lastName.isNotEmpty())) {
            return listOf(
                draft(
                    DraftKind.Contact,
                    FieldKey.FIRST_NAME to card.firstName,
                    FieldKey.LAST_NAME to card.lastName,
                    FieldKey.CONTACT_PHONE to card.phone,
                    FieldKey.CONTACT_EMAIL to card.email,
                    FieldKey.ORGANIZATION to card.organization,
                    FieldKey.WEBSITE to card.website,
                    FieldKey.STREET to card.street,
                    FieldKey.CITY to card.city,
                    FieldKey.POSTAL_CODE to card.postalCode,
                    FieldKey.COUNTRY to card.country,
                ),
            )
        }
    }
    val isText = type.startsWith("text/") || type == "application/json" || type == "application/xml"
    if (!isText) return emptyList()
    return listOf(draft(DraftKind.Mime, FieldKey.MIME_TYPE to type, FieldKey.MIME_DATA to text))
}

fun recordsToDrafts(records: List<NdefRecordData>): List<RecordDraft> = records.flatMap { record ->
    when (val parsed = parseRecord(record)) {
        is ParsedRecord.Text -> listOf(
            draft(
                DraftKind.Text,
                FieldKey.TEXT to parsed.content.text,
                FieldKey.LANGUAGE to parsed.content.languageCode,
                FieldKey.ENCODING to if (parsed.content.utf16) FieldValue.ENCODING_UTF16 else FieldValue.ENCODING_UTF8,
            ),
        )
        is ParsedRecord.Uri -> listOf(uriDraft(parsed.uri))
        is ParsedRecord.Wifi -> listOf(wifiDraft(parsed.credential))
        is ParsedRecord.SmartPoster -> listOfNotNull(
            parsed.title?.let { draft(DraftKind.Text, FieldKey.TEXT to it, FieldKey.LANGUAGE to "en", FieldKey.ENCODING to FieldValue.ENCODING_UTF8) },
            parsed.uri?.let { uriDraft(it) },
        )
        is ParsedRecord.AndroidApp -> listOf(draft(DraftKind.App, FieldKey.APP_PACKAGE to parsed.packageName))
        is ParsedRecord.Mime -> mimeDrafts(record, parsed)
        else -> emptyList()
    }
}
