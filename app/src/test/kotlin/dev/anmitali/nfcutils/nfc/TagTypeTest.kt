// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.nfc

import org.junit.Assert.assertEquals
import org.junit.Test

class TagTypeTest {
    @Test
    fun ultralightCIsDetectedFromTechnologyType() {
        assertEquals(
            TagType.MifareUltralightC,
            classifyTag(listOf("NfcA", "MifareUltralight", "Ndef"), "org.nfcforum.ndef.type2", 2, 144),
        )
    }

    @Test
    fun ntagVariantsAreDetectedFromMemorySize() {
        val techs = listOf("NfcA", "MifareUltralight", "Ndef")
        assertEquals(TagType.Ntag213, classifyTag(techs, "org.nfcforum.ndef.type2", 1, 144))
        assertEquals(TagType.Ntag215, classifyTag(techs, "org.nfcforum.ndef.type2", 1, 496))
        assertEquals(TagType.Ntag216, classifyTag(techs, "org.nfcforum.ndef.type2", 1, 872))
    }

    @Test
    fun otherUltralightSizesAreGenericUltralight() {
        assertEquals(
            TagType.MifareUltralight,
            classifyTag(listOf("NfcA", "MifareUltralight", "Ndef"), "org.nfcforum.ndef.type2", 1, 48),
        )
        assertEquals(TagType.MifareUltralight, classifyTag(listOf("NfcA", "MifareUltralight"), null, 1, null))
    }

    @Test
    fun nfcForumTypesAreDetectedFromNdefType() {
        assertEquals(TagType.NfcForumType1, classifyTag(listOf("NfcA", "Ndef"), "org.nfcforum.ndef.type1", null, 100))
        assertEquals(TagType.NfcForumType2, classifyTag(listOf("NfcA", "Ndef"), "org.nfcforum.ndef.type2", null, 100))
        assertEquals(TagType.NfcForumType3, classifyTag(listOf("NfcF", "Ndef"), "org.nfcforum.ndef.type3", null, 100))
        assertEquals(TagType.NfcForumType4, classifyTag(listOf("IsoDep", "NfcA", "Ndef"), "org.nfcforum.ndef.type4", null, 100))
    }

    @Test
    fun mifareClassicIsDetectedWithAndWithoutNdef() {
        assertEquals(
            TagType.MifareClassic,
            classifyTag(listOf("NfcA", "MifareClassic", "Ndef"), "com.nxp.ndef.mifareclassic", null, 716),
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
