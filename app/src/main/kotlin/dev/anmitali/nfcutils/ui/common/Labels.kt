// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.common

import androidx.annotation.StringRes
import dev.anmitali.nfcutils.R
import dev.anmitali.nfcutils.ndef.DraftKind
import dev.anmitali.nfcutils.ndef.FieldError
import dev.anmitali.nfcutils.ndef.ParsedKind
import dev.anmitali.nfcutils.nfc.TagType

@StringRes
fun TagType.labelRes(): Int = when (this) {
    TagType.NfcForumType1 -> R.string.tag_type_nfc_forum_type_1
    TagType.NfcForumType2 -> R.string.tag_type_nfc_forum_type_2
    TagType.NfcForumType3 -> R.string.tag_type_nfc_forum_type_3
    TagType.NfcForumType4 -> R.string.tag_type_nfc_forum_type_4
    TagType.MifareClassic -> R.string.tag_type_mifare_classic
    TagType.MifareUltralight -> R.string.tag_type_mifare_ultralight
    TagType.MifareUltralightC -> R.string.tag_type_mifare_ultralight_c
    TagType.Ntag213 -> R.string.tag_type_ntag213
    TagType.Ntag215 -> R.string.tag_type_ntag215
    TagType.Ntag216 -> R.string.tag_type_ntag216
    TagType.Iso15693 -> R.string.tag_type_iso15693
    TagType.FeliCa -> R.string.tag_type_felica
    TagType.Iso14443Type4 -> R.string.tag_type_iso14443_4
    TagType.Iso14443TypeB -> R.string.tag_type_iso14443_b
    TagType.Iso14443TypeA -> R.string.tag_type_iso14443_a
    TagType.Barcode -> R.string.tag_type_barcode
    TagType.Unknown -> R.string.tag_type_unknown
}

@StringRes
fun ParsedKind.labelRes(): Int = when (this) {
    ParsedKind.Empty -> R.string.record_kind_empty
    ParsedKind.Text -> R.string.record_kind_text
    ParsedKind.Uri -> R.string.record_kind_uri
    ParsedKind.Mime -> R.string.record_kind_mime
    ParsedKind.Wifi -> R.string.record_kind_wifi
    ParsedKind.SmartPoster -> R.string.record_kind_smart_poster
    ParsedKind.AndroidApp -> R.string.record_kind_android_app
    ParsedKind.External -> R.string.record_kind_external
    ParsedKind.Unknown -> R.string.record_kind_unknown
}

@StringRes
fun DraftKind.labelRes(): Int = when (this) {
    DraftKind.Text -> R.string.draft_kind_text
    DraftKind.Url -> R.string.draft_kind_url
    DraftKind.Phone -> R.string.draft_kind_phone
    DraftKind.Sms -> R.string.draft_kind_sms
    DraftKind.Email -> R.string.draft_kind_email
    DraftKind.Geo -> R.string.draft_kind_geo
    DraftKind.Wifi -> R.string.draft_kind_wifi
    DraftKind.Contact -> R.string.draft_kind_contact
    DraftKind.App -> R.string.draft_kind_app
    DraftKind.Mime -> R.string.draft_kind_mime
}

@StringRes
fun FieldError.messageRes(): Int = when (this) {
    FieldError.Empty -> R.string.field_error_empty
    FieldError.TooShort -> R.string.field_error_too_short
    FieldError.TooLong -> R.string.field_error_too_long
    FieldError.InvalidFormat -> R.string.field_error_invalid_format
    FieldError.OutOfRange -> R.string.field_error_out_of_range
}
