// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class HexTest {
    @Test
    fun formatsWithSpacesByDefault() {
        assertEquals("04 A1 0F 00", byteArrayOf(0x04, 0xA1.toByte(), 0x0F, 0x00).toHex())
    }

    @Test
    fun formatsWithCustomSeparator() {
        assertEquals("04:A1:FF", byteArrayOf(0x04, 0xA1.toByte(), 0xFF.toByte()).toHex(":"))
    }

    @Test
    fun formatsWithoutSeparator() {
        assertEquals("04A1", byteArrayOf(0x04, 0xA1.toByte()).toHex(""))
    }

    @Test
    fun formatsEmptyArray() {
        assertEquals("", ByteArray(0).toHex())
    }

    @Test
    fun parsesHexWithSeparators() {
        assertArrayEquals(byteArrayOf(0x04, 0xA1.toByte(), 0x0F), "04 a1:0F".hexToBytes())
    }

    @Test
    fun rejectsOddLength() {
        assertThrows(IllegalArgumentException::class.java) { "ABC".hexToBytes() }
    }

    @Test
    fun rejectsInvalidDigits() {
        assertThrows(IllegalArgumentException::class.java) { "ZZ".hexToBytes() }
    }
}
