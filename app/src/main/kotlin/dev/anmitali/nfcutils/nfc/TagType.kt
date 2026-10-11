// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.nfc

enum class TagType {
    NfcForumType1,
    NfcForumType2,
    NfcForumType3,
    NfcForumType4,
    MifareClassic,
    MifareUltralight,
    MifareUltralightC,
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

private const val NTAG213_NDEF_SIZE = 144
private const val NTAG215_NDEF_SIZE = 496
private const val NTAG216_NDEF_SIZE = 872

fun classifyTag(
    technologies: List<String>,
    ndefType: String?,
    ultralightType: Int?,
    ndefMaxSize: Int?,
): TagType {
    if ("MifareUltralight" in technologies) {
        if (ultralightType == ULTRALIGHT_TYPE_C) return TagType.MifareUltralightC
        return when (ndefMaxSize) {
            NTAG213_NDEF_SIZE -> TagType.Ntag213
            NTAG215_NDEF_SIZE -> TagType.Ntag215
            NTAG216_NDEF_SIZE -> TagType.Ntag216
            else -> TagType.MifareUltralight
        }
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
