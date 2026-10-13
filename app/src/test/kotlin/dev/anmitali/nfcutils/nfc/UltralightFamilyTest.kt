// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.nfc

import dev.anmitali.nfcutils.ndef.hexToBytes
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UltralightFamilyTest {
    private fun family(hex: String) = identifyUltralightFamily(hex.hexToBytes())

    @Test
    fun commandsMatchTheDatasheets() {
        assertArrayEquals(byteArrayOf(0x60), GetVersionCommand)
        assertArrayEquals(byteArrayOf(0x3C, 0x00), ReadSignatureCommand)
        assertArrayEquals(byteArrayOf(0x39, 0x02), ReadCounterCommand)
    }

    @Test
    fun identifiesNtag213() {
        val result = family("00 04 04 02 01 00 0F 03")!!
        assertEquals(TagType.Ntag213, result.type)
        assertEquals(45, result.pageCount)
        assertTrue(result.hasCounter)
    }

    @Test
    fun identifiesNtag215() {
        val result = family("00 04 04 02 01 00 11 03")!!
        assertEquals(TagType.Ntag215, result.type)
        assertEquals(135, result.pageCount)
        assertTrue(result.hasCounter)
    }

    @Test
    fun identifiesNtag216() {
        val result = family("00 04 04 02 01 00 13 03")!!
        assertEquals(TagType.Ntag216, result.type)
        assertEquals(231, result.pageCount)
        assertTrue(result.hasCounter)
    }

    @Test
    fun identifiesNtag212WithoutCounter() {
        val result = family("00 04 04 01 01 00 0E 03")!!
        assertEquals(TagType.Ntag212, result.type)
        assertEquals(41, result.pageCount)
        assertFalse(result.hasCounter)
    }

    @Test
    fun identifiesUltralightEv1Mf0ul21() {
        val result = family("00 04 03 02 01 00 0E 03")!!
        assertEquals(TagType.UltralightEv1Ul21, result.type)
        assertEquals(41, result.pageCount)
        assertFalse(result.hasCounter)
    }

    @Test
    fun ignoresSubtypeByte() {
        assertEquals(TagType.UltralightEv1Ul21, family("00 04 03 01 01 00 0E 03")!!.type)
    }

    @Test
    fun doesNotIdentifyOtherStorageSizes() {
        assertNull(family("00 04 04 01 01 00 0B 03"))
        assertNull(family("00 04 03 01 01 00 0B 03"))
    }

    @Test
    fun doesNotIdentifyOtherVendorsOrProducts() {
        assertNull(family("00 05 04 02 01 00 0F 03"))
        assertNull(family("00 04 05 02 01 00 0F 03"))
    }

    @Test
    fun rejectsWrongLength() {
        assertNull(family("00 04 04 02 01 00 0F"))
        assertNull(identifyUltralightFamily(ByteArray(0)))
    }

    @Test
    fun parsesCounterWithLeastSignificantByteFirst() {
        assertEquals(1, parseCounter("01 00 00".hexToBytes()))
        assertEquals(0x030201, parseCounter("01 02 03".hexToBytes()))
        assertEquals(0xFFFFFF, parseCounter("FF FF FF".hexToBytes()))
    }

    @Test
    fun rejectsCounterOfWrongLength() {
        assertNull(parseCounter(ByteArray(2)))
        assertNull(parseCounter(ByteArray(4)))
    }

    @Test
    fun parsesSignatureAsHex() {
        val signature = ByteArray(32) { it.toByte() }
        assertEquals(32 * 3 - 1, parseSignature(signature)!!.length)
        assertEquals("00 01 02", parseSignature(signature)!!.substring(0, 8))
    }

    @Test
    fun rejectsSignatureOfWrongLength() {
        assertNull(parseSignature(ByteArray(31)))
        assertNull(parseSignature(ByteArray(33)))
    }
}
