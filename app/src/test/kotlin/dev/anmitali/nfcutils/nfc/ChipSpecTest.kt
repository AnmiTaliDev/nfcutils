// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.nfc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChipSpecTest {
    @Test
    fun totalBytesIsPagesTimesFour() {
        assertEquals(180, TagType.Ntag213.chipSpec()!!.totalBytes)
        assertEquals(540, TagType.Ntag215.chipSpec()!!.totalBytes)
        assertEquals(924, TagType.Ntag216.chipSpec()!!.totalBytes)
    }

    @Test
    fun pageCountsMatchIdentifiedFamilies() {
        val versions = mapOf(
            TagType.Ntag212 to byteArrayOf(0, 4, 4, 2, 1, 0, 0x0E, 3),
            TagType.Ntag213 to byteArrayOf(0, 4, 4, 2, 1, 0, 0x0F, 3),
            TagType.Ntag215 to byteArrayOf(0, 4, 4, 2, 1, 0, 0x11, 3),
            TagType.Ntag216 to byteArrayOf(0, 4, 4, 2, 1, 0, 0x13, 3),
            TagType.UltralightEv1Ul21 to byteArrayOf(0, 4, 3, 1, 1, 0, 0x0E, 3),
        )
        for ((type, version) in versions) {
            assertEquals(type, identifyUltralightFamily(version)!!.type)
            assertEquals(identifyUltralightFamily(version)!!.pageCount, type.chipSpec()!!.pages)
        }
    }

    @Test
    fun onlyNtag21xHaveCounter() {
        assertEquals(true, ChipFeature.Counter in TagType.Ntag215.chipSpec()!!.features)
        assertEquals(false, ChipFeature.Counter in TagType.Ntag212.chipSpec()!!.features)
        assertEquals(false, ChipFeature.Counter in TagType.UltralightEv1Ul21.chipSpec()!!.features)
    }

    @Test
    fun otherTypesHaveNoSpec() {
        assertNull(TagType.MifareClassic.chipSpec())
        assertNull(TagType.Unknown.chipSpec())
    }
}
