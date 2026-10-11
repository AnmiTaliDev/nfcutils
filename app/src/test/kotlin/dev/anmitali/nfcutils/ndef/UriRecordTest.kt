// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UriRecordTest {
    private fun payloadOf(uri: String) = buildUriRecord(uri).record().payload

    @Test
    fun usesLongestMatchingPrefix() {
        assertEquals("02 65 78 61 6D 70 6C 65", payloadOf("https://www.example").toHex())
        assertEquals("01", payloadOf("http://www.").toHex())
    }

    @Test
    fun usesShorterPrefixWhenLongerDoesNotMatch() {
        assertEquals(0x04, payloadOf("https://example.com")[0].toInt())
        assertEquals(0x03, payloadOf("http://example.com")[0].toInt())
    }

    @Test
    fun prefersSpecificEpcPrefixOverUrn() {
        assertEquals(0x1E, payloadOf("urn:epc:id:sgtin:1")[0].toInt())
        assertEquals(0x13, payloadOf("urn:isbn:123")[0].toInt())
    }

    @Test
    fun usesNoPrefixForUnknownScheme() {
        assertEquals(0x00, payloadOf("custom:thing")[0].toInt())
        assertEquals("custom:thing", String(payloadOf("custom:thing"), 1, 12))
    }

    @Test
    fun buildsWellKnownUriRecord() {
        val record = buildUriRecord("tel:+123").record()
        assertEquals(Tnf.WELL_KNOWN, record.tnf)
        assertEquals("U", String(record.type))
        assertEquals(0x05, record.payload[0].toInt())
    }

    @Test
    fun trimsInput() {
        assertEquals(0x04, payloadOf("  https://example.com  ")[0].toInt())
    }

    @Test
    fun encodesNonAsciiAsUtf8() {
        assertEquals("04 C3 A9", payloadOf("https://é").toHex().substring(0, 8))
    }

    @Test
    fun rejectsEmptyInput() {
        assertInvalid(FieldKey.URL, FieldError.Empty, buildUriRecord("  "))
    }

    @Test
    fun rejectsInputWithoutScheme() {
        assertInvalid(FieldKey.URL, FieldError.InvalidFormat, buildUriRecord("example.com"))
    }

    @Test
    fun rejectsInputWithWhitespace() {
        assertInvalid(FieldKey.URL, FieldError.InvalidFormat, buildUriRecord("https://exa mple.com"))
    }

    @Test
    fun decodesPrefixedPayload() {
        assertEquals("https://www.example.com", decodeUriPayload(payloadOf("https://www.example.com")))
    }

    @Test
    fun decodesPayloadWithoutPrefix() {
        assertEquals("custom:thing", decodeUriPayload(payloadOf("custom:thing")))
    }

    @Test
    fun decodeRejectsUnknownPrefixIndex() {
        assertNull(decodeUriPayload(byteArrayOf(0x24, 0x61)))
    }

    @Test
    fun decodeRejectsEmptyPayload() {
        assertNull(decodeUriPayload(ByteArray(0)))
    }

    @Test
    fun decodesEveryPrefixIndex() {
        for (index in 0..0x23) {
            val decoded = decodeUriPayload(byteArrayOf(index.toByte(), 0x78))
            assertEquals(true, decoded!!.endsWith("x"))
            assertEquals(decoded, decodeUriPayload(encodeUriPayload(decoded)))
        }
    }
}
