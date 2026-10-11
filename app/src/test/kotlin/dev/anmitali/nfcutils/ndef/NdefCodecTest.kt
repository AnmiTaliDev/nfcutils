// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NdefCodecTest {
    private val textHi = buildTextRecord("Hi", "en", false).record()

    @Test
    fun encodesSingleShortRecord() {
        assertEquals("D1 01 05 54 02 65 6E 48 69", NdefCodec.encode(listOf(textHi)).toHex())
    }

    @Test
    fun encodesEmptyRecord() {
        assertEquals("D0 00 00", NdefCodec.encode(listOf(EmptyRecord)).toHex())
    }

    @Test
    fun setsMessageBeginAndEndFlagsAcrossRecords() {
        val bytes = NdefCodec.encode(listOf(textHi, textHi, textHi))
        assertEquals(0x91, bytes[0].toInt() and 0xFF)
        assertEquals(0x11, bytes[9].toInt() and 0xFF)
        assertEquals(0x51, bytes[18].toInt() and 0xFF)
    }

    @Test
    fun usesFourByteLengthForLongPayload() {
        val record = mimeRecord("a/b", ByteArray(256))
        val bytes = NdefCodec.encode(listOf(record))
        assertEquals(0xC2, bytes[0].toInt() and 0xFF)
        assertEquals("00 00 01 00", bytes.copyOfRange(2, 6).toHex())
        assertEquals(1 + 1 + 4 + 3 + 256, bytes.size)
    }

    @Test
    fun encodesIdField() {
        val record = NdefRecordData(Tnf.MIME, "a/b".toByteArray(), byteArrayOf(0x31), byteArrayOf(0x01))
        assertEquals("DA 03 01 01 61 2F 62 31 01", NdefCodec.encode(listOf(record)).toHex())
    }

    @Test
    fun sizeMatchesEncodedLength() {
        val records = listOf(
            textHi,
            mimeRecord("a/b", ByteArray(300)),
            NdefRecordData(Tnf.EXTERNAL, "x.com:t".toByteArray(), byteArrayOf(1, 2), byteArrayOf(9)),
            EmptyRecord,
        )
        assertEquals(NdefCodec.encode(records).size, NdefCodec.size(records))
    }

    @Test
    fun sizeOfEmptyList() {
        assertEquals(0, NdefCodec.size(emptyList()))
    }

    @Test
    fun decodeRoundTrip() {
        val records = listOf(
            textHi,
            mimeRecord("a/b", ByteArray(300) { it.toByte() }),
            NdefRecordData(Tnf.EXTERNAL, "x.com:t".toByteArray(), byteArrayOf(1, 2), byteArrayOf(9)),
        )
        val decoded = NdefCodec.decode(NdefCodec.encode(records))
        assertNotNull(decoded)
        assertEquals(records, decoded)
    }

    @Test
    fun decodeRejectsTruncatedInput() {
        val bytes = NdefCodec.encode(listOf(textHi))
        assertNull(NdefCodec.decode(bytes.copyOf(bytes.size - 1)))
    }

    @Test
    fun decodeRejectsMissingMessageBegin() {
        val bytes = NdefCodec.encode(listOf(textHi))
        bytes[0] = (bytes[0].toInt() and 0x7F).toByte()
        assertNull(NdefCodec.decode(bytes))
    }

    @Test
    fun decodeRejectsMissingMessageEnd() {
        val bytes = NdefCodec.encode(listOf(textHi))
        bytes[0] = (bytes[0].toInt() and 0xBF).toByte()
        assertNull(NdefCodec.decode(bytes))
    }

    @Test
    fun decodeRejectsTrailingBytes() {
        val bytes = NdefCodec.encode(listOf(textHi)) + byteArrayOf(0)
        assertNull(NdefCodec.decode(bytes))
    }

    @Test
    fun decodeRejectsChunkedRecords() {
        assertNull(NdefCodec.decode(byteArrayOf(0xB1.toByte(), 0x01, 0x00, 0x54)))
    }

    @Test
    fun decodeRejectsEmptyInput() {
        assertNull(NdefCodec.decode(ByteArray(0)))
    }

    @Test
    fun decodedPayloadIsIndependentCopy() {
        val encoded = NdefCodec.encode(listOf(textHi))
        val decoded = NdefCodec.decode(encoded)!!
        assertArrayEquals(textHi.payload, decoded[0].payload)
    }
}
