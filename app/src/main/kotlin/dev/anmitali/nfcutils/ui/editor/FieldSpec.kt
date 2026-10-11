// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.editor

import androidx.annotation.StringRes
import androidx.compose.ui.text.input.KeyboardType
import dev.anmitali.nfcutils.R
import dev.anmitali.nfcutils.ndef.DraftKind
import dev.anmitali.nfcutils.ndef.FieldKey
import dev.anmitali.nfcutils.ndef.FieldValue

class Choice(val value: String, @StringRes val label: Int)

sealed interface FieldSpec {
    val key: String
    val label: Int

    class Input(
        override val key: String,
        @StringRes override val label: Int,
        val keyboardType: KeyboardType = KeyboardType.Text,
        val multiline: Boolean = false,
    ) : FieldSpec

    class Options(
        override val key: String,
        @StringRes override val label: Int,
        val choices: List<Choice>,
    ) : FieldSpec
}

fun fieldsFor(kind: DraftKind): List<FieldSpec> = when (kind) {
    DraftKind.Text -> listOf(
        FieldSpec.Input(FieldKey.TEXT, R.string.field_text, multiline = true),
        FieldSpec.Input(FieldKey.LANGUAGE, R.string.field_language),
        FieldSpec.Options(
            FieldKey.ENCODING,
            R.string.field_encoding,
            listOf(
                Choice(FieldValue.ENCODING_UTF8, R.string.choice_utf8),
                Choice(FieldValue.ENCODING_UTF16, R.string.choice_utf16),
            ),
        ),
    )
    DraftKind.Url -> listOf(FieldSpec.Input(FieldKey.URL, R.string.field_url, KeyboardType.Uri))
    DraftKind.Phone -> listOf(FieldSpec.Input(FieldKey.PHONE, R.string.field_phone, KeyboardType.Phone))
    DraftKind.Email -> listOf(
        FieldSpec.Input(FieldKey.EMAIL_ADDRESS, R.string.field_email_address, KeyboardType.Email),
        FieldSpec.Input(FieldKey.EMAIL_SUBJECT, R.string.field_email_subject),
        FieldSpec.Input(FieldKey.EMAIL_BODY, R.string.field_email_body, multiline = true),
    )
    DraftKind.Geo -> listOf(
        FieldSpec.Input(FieldKey.LATITUDE, R.string.field_latitude, KeyboardType.Decimal),
        FieldSpec.Input(FieldKey.LONGITUDE, R.string.field_longitude, KeyboardType.Decimal),
    )
    DraftKind.Wifi -> listOf(
        FieldSpec.Input(FieldKey.WIFI_SSID, R.string.field_wifi_ssid),
        FieldSpec.Input(FieldKey.WIFI_PASSWORD, R.string.field_wifi_password),
        FieldSpec.Options(
            FieldKey.WIFI_SECURITY,
            R.string.field_wifi_security,
            listOf(
                Choice(FieldValue.SECURITY_OPEN, R.string.choice_wifi_open),
                Choice(FieldValue.SECURITY_WPA2, R.string.choice_wifi_wpa2),
                Choice(FieldValue.SECURITY_WPA_WPA2, R.string.choice_wifi_wpa_wpa2),
            ),
        ),
    )
    DraftKind.Contact -> listOf(
        FieldSpec.Input(FieldKey.FIRST_NAME, R.string.field_first_name),
        FieldSpec.Input(FieldKey.LAST_NAME, R.string.field_last_name),
        FieldSpec.Input(FieldKey.CONTACT_PHONE, R.string.field_phone, KeyboardType.Phone),
        FieldSpec.Input(FieldKey.CONTACT_EMAIL, R.string.field_email_address, KeyboardType.Email),
        FieldSpec.Input(FieldKey.ORGANIZATION, R.string.field_organization),
        FieldSpec.Input(FieldKey.WEBSITE, R.string.field_website, KeyboardType.Uri),
    )
    DraftKind.Mime -> listOf(
        FieldSpec.Input(FieldKey.MIME_TYPE, R.string.field_mime_type),
        FieldSpec.Input(FieldKey.MIME_DATA, R.string.field_mime_data, multiline = true),
    )
}
