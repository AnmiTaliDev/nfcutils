// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.nfc

import org.junit.Assert.assertEquals
import org.junit.Test

class TagTypeTest {
    private val ultralightTechs = listOf("NfcA", "MifareUltralight", "Ndef")

    @Test
    fun ultralightCIsDetectedFromTechnologyType() {
        assertEquals(
            TagType.MifareUltralightC,
            classifyTag(ultralightTechs, "org.nfcforum.ndef.type2", 2, null),
        )
    }

    @Test
    fun ultralightCStaysUltralightCEvenIfIdentified() {
        assertEquals(TagType.MifareUltralightC, classifyTag(ultralightTechs, null, 2, TagType.Ntag213))
    }

    @Test
    fun identifiedFamilyWinsOverGenericUltralight() {
        assertEquals(TagType.Ntag213, classifyTag(ultralightTechs, "org.nfcforum.ndef.type2", 1, TagType.Ntag213))
        assertEquals(TagType.Ntag216, classifyTag(ultralightTechs, "org.nfcforum.ndef.type2", 1, TagType.Ntag216))
    }

    @Test
    fun unidentifiedUltralightIsGeneric() {
        assertEquals(TagType.MifareUltralight, classifyTag(ultralightTechs, "org.nfcforum.ndef.type2", 1, null))
        assertEquals(TagType.MifareUltralight, classifyTag(listOf("NfcA", "MifareUltralight"), null, 1, null))
    }

    @Test
    fun nfcForumTypesAreDetectedFromNdefType() {
        assertEquals(TagType.NfcForumType1, classifyTag(listOf("NfcA", "Ndef"), "org.nfcforum.ndef.type1", null, null))
        assertEquals(TagType.NfcForumType2, classifyTag(listOf("NfcA", "Ndef"), "org.nfcforum.ndef.type2", null, null))
        assertEquals(TagType.NfcForumType3, classifyTag(listOf("NfcF", "Ndef"), "org.nfcforum.ndef.type3", null, null))
        assertEquals(TagType.NfcForumType4, classifyTag(listOf("IsoDep", "NfcA", "Ndef"), "org.nfcforum.ndef.type4", null, null))
    }

    @Test
    fun mifareClassicIsDetectedWithAndWithoutNdef() {
        assertEquals(
            TagType.MifareClassic,
            classifyTag(listOf("NfcA", "MifareClassic", "Ndef"), "com.nxp.ndef.mifareclassic", null, null),
        )
        assertEquals(TagType.MifareClassic, classifyTag(listOf("NfcA", "MifareClassic"), null, null, null))
    }

    @Test
    fun fallsBackToTechnologyList() {
        assertEquals(TagType.Iso15693, classifyTag(listOf("NfcV"), null, null, null))
        assertEquals(TagType.FeliCa, classifyTag(listOf("NfcF"), null, null, null))
        assertEquals(TagType.Iso14443Type4, classifyTag(listOf("IsoDep", "NfcA"), null, null, null))
        assertEquals(TagType.Iso14443TypeB, classifyTag(listOf("NfcB"), null, null, null))
        assertEquals(TagType.Iso14443TypeA, classifyTag(listOf("NfcA"), null, null, null))
        assertEquals(TagType.Barcode, classifyTag(listOf("NfcBarcode"), null, null, null))
        assertEquals(TagType.Unknown, classifyTag(emptyList(), null, null, null))
    }
}
