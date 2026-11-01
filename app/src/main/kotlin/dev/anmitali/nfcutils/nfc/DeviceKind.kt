// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.nfc

enum class DeviceKind {
    NfcTag,
    AccessCard,
    SmartCard,
    FeliCaCard,
    Iso15693Tag,
    BarcodeTag,
    Unknown,
}

fun TagType.deviceKind(): DeviceKind = when (this) {
    TagType.NfcForumType1,
    TagType.NfcForumType2,
    TagType.NfcForumType3,
    TagType.NfcForumType4,
    TagType.MifareUltralight,
    TagType.MifareUltralightC,
    TagType.UltralightEv1Ul21,
    TagType.Ntag212,
    TagType.Ntag213,
    TagType.Ntag215,
    TagType.Ntag216,
    -> DeviceKind.NfcTag
    TagType.MifareClassic -> DeviceKind.AccessCard
    TagType.Iso14443Type4, TagType.Iso14443TypeB -> DeviceKind.SmartCard
    TagType.FeliCa -> DeviceKind.FeliCaCard
    TagType.Iso15693 -> DeviceKind.Iso15693Tag
    TagType.Barcode -> DeviceKind.BarcodeTag
    TagType.Iso14443TypeA, TagType.Unknown -> DeviceKind.Unknown
}
