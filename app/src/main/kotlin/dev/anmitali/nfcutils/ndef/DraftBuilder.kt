// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

fun buildRecord(draft: RecordDraft): RecordBuild = when (draft.kind) {
    DraftKind.Text -> buildTextRecord(
        text = draft.field(FieldKey.TEXT),
        languageCode = draft.field(FieldKey.LANGUAGE),
        utf16 = draft.field(FieldKey.ENCODING) == FieldValue.ENCODING_UTF16,
    )
    DraftKind.Url -> buildUriRecord(draft.field(FieldKey.URL))
    DraftKind.Phone -> buildPhoneRecord(draft.field(FieldKey.PHONE))
    DraftKind.Email -> buildEmailRecord(
        address = draft.field(FieldKey.EMAIL_ADDRESS),
        subject = draft.field(FieldKey.EMAIL_SUBJECT),
        body = draft.field(FieldKey.EMAIL_BODY),
    )
    DraftKind.Geo -> buildGeoRecord(draft.field(FieldKey.LATITUDE), draft.field(FieldKey.LONGITUDE))
    DraftKind.Wifi -> buildWifiRecord(
        ssid = draft.field(FieldKey.WIFI_SSID),
        password = draft.field(FieldKey.WIFI_PASSWORD),
        auth = when (draft.field(FieldKey.WIFI_SECURITY)) {
            FieldValue.SECURITY_OPEN -> WifiAuth.Open
            FieldValue.SECURITY_WPA_WPA2 -> WifiAuth.WpaWpa2Personal
            else -> WifiAuth.Wpa2Personal
        },
    )
    DraftKind.Contact -> buildVCardRecord(
        firstName = draft.field(FieldKey.FIRST_NAME),
        lastName = draft.field(FieldKey.LAST_NAME),
        phone = draft.field(FieldKey.CONTACT_PHONE),
        email = draft.field(FieldKey.CONTACT_EMAIL),
        organization = draft.field(FieldKey.ORGANIZATION),
        website = draft.field(FieldKey.WEBSITE),
    )
    DraftKind.Mime -> buildMimeRecord(draft.field(FieldKey.MIME_TYPE), draft.field(FieldKey.MIME_DATA))
}
