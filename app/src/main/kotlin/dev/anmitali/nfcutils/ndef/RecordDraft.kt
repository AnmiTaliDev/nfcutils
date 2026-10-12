// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

enum class DraftKind { Text, Url, Phone, Sms, Email, Geo, Wifi, Contact, App, Mime }

object FieldKey {
    const val TEXT = "text"
    const val LANGUAGE = "language"
    const val ENCODING = "encoding"
    const val URL = "url"
    const val PHONE = "phone"
    const val SMS_NUMBER = "sms_number"
    const val SMS_BODY = "sms_body"
    const val EMAIL_ADDRESS = "email_address"
    const val EMAIL_SUBJECT = "email_subject"
    const val EMAIL_BODY = "email_body"
    const val LATITUDE = "latitude"
    const val LONGITUDE = "longitude"
    const val WIFI_SSID = "wifi_ssid"
    const val WIFI_PASSWORD = "wifi_password"
    const val WIFI_SECURITY = "wifi_security"
    const val FIRST_NAME = "first_name"
    const val LAST_NAME = "last_name"
    const val CONTACT_PHONE = "contact_phone"
    const val CONTACT_EMAIL = "contact_email"
    const val ORGANIZATION = "organization"
    const val WEBSITE = "website"
    const val STREET = "street"
    const val CITY = "city"
    const val POSTAL_CODE = "postal_code"
    const val COUNTRY = "country"
    const val APP_PACKAGE = "app_package"
    const val MIME_TYPE = "mime_type"
    const val MIME_DATA = "mime_data"
}

object FieldValue {
    const val ENCODING_UTF8 = "utf8"
    const val ENCODING_UTF16 = "utf16"
    const val SECURITY_OPEN = "open"
    const val SECURITY_WPA2 = "wpa2"
    const val SECURITY_WPA_WPA2 = "wpa_wpa2"
}

class RecordDraft(val kind: DraftKind, val fields: Map<String, String>) {
    fun field(key: String): String = fields[key].orEmpty()

    fun withField(key: String, value: String): RecordDraft = RecordDraft(kind, fields + (key to value))
}

fun emptyDraft(kind: DraftKind): RecordDraft {
    val defaults = when (kind) {
        DraftKind.Text -> mapOf(FieldKey.LANGUAGE to "en", FieldKey.ENCODING to FieldValue.ENCODING_UTF8)
        DraftKind.Wifi -> mapOf(FieldKey.WIFI_SECURITY to FieldValue.SECURITY_WPA2)
        else -> emptyMap()
    }
    return RecordDraft(kind, defaults)
}
