// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.nfc

enum class TagType {
    NfcForumType1,
    NfcForumType2,
    NfcForumType3,
    NfcForumType4,
    MifareClassic,
    MifareClassicMini,
    MifareClassic1K,
    MifareClassic4K,
    MifareDesfire,
    MifareUltralight,
    MifareUltralightC,
    UltralightEv1Ul21,
    Ntag212,
    Ntag213,
    Ntag215,
    Ntag216,
    Iso15693,
    FeliCa,
    Iso14443Type4,
    Iso14443TypeB,
    Iso14443TypeA,
    Barcode,
    Unknown,
}

const val ULTRALIGHT_TYPE_C = 2

fun classifyTag(
    technologies: List<String>,
    ndefType: String?,
    ultralightType: Int?,
    identifiedUltralight: TagType?,
): TagType {
    if ("MifareUltralight" in technologies) {
        if (ultralightType == ULTRALIGHT_TYPE_C) return TagType.MifareUltralightC
        return identifiedUltralight ?: TagType.MifareUltralight
    }
    when (ndefType) {
        "org.nfcforum.ndef.type1" -> return TagType.NfcForumType1
        "org.nfcforum.ndef.type2" -> return TagType.NfcForumType2
        "org.nfcforum.ndef.type3" -> return TagType.NfcForumType3
        "org.nfcforum.ndef.type4" -> return TagType.NfcForumType4
        "com.nxp.ndef.mifareclassic" -> return TagType.MifareClassic
    }
    return when {
        "MifareClassic" in technologies -> TagType.MifareClassic
        "NfcV" in technologies -> TagType.Iso15693
        "NfcF" in technologies -> TagType.FeliCa
        "IsoDep" in technologies -> TagType.Iso14443Type4
        "NfcB" in technologies -> TagType.Iso14443TypeB
        "NfcA" in technologies -> TagType.Iso14443TypeA
        "NfcBarcode" in technologies -> TagType.Barcode
        else -> TagType.Unknown
    }
}

private val ClassicMiniSak = setOf(0x09)
private val Classic1KSak = setOf(0x08, 0x28, 0x88)
private val Classic4KSak = setOf(0x18, 0x38)
private const val DESFIRE_SAK = 0x20
private const val DESFIRE_ATQA = "44 03"

fun refineTagType(type: TagType, sak: Int?, atqa: String?): TagType = when {
    type == TagType.MifareClassic && sak in ClassicMiniSak -> TagType.MifareClassicMini
    type == TagType.MifareClassic && sak in Classic1KSak -> TagType.MifareClassic1K
    type == TagType.MifareClassic && sak in Classic4KSak -> TagType.MifareClassic4K
    sak == DESFIRE_SAK && atqa == DESFIRE_ATQA &&
        (type == TagType.Iso14443Type4 || type == TagType.NfcForumType4 || type == TagType.Iso14443TypeA) ->
        TagType.MifareDesfire
    else -> type
}
