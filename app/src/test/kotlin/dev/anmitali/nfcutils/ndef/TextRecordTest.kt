// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TextRecordTest {
    @Test
    fun buildsUtf8Record() {
        val record = buildTextRecord("Hi", "en", false).record()
        assertEquals(Tnf.WELL_KNOWN, record.tnf)
        assertEquals("T", String(record.type))
        assertEquals("02 65 6E 48 69", record.payload.toHex())
    }

    @Test
    fun buildsUtf16RecordWithByteOrderMark() {
        val record = buildTextRecord("Hi", "en", true).record()
        assertEquals("82 65 6E FE FF 00 48 00 69", record.payload.toHex())
    }

    @Test
    fun encodesNonAsciiTextAsUtf8() {
        val record = buildTextRecord("é", "fr", false).record()
        assertEquals("02 66 72 C3 A9", record.payload.toHex())
    }

    @Test
    fun trimsLanguageCode() {
        val record = buildTextRecord("Hi", " en-US ", false).record()
        assertEquals("05 65 6E 2D 55 53 48 69", record.payload.toHex())
    }

    @Test
    fun rejectsEmptyText() {
        assertInvalid(FieldKey.TEXT, FieldError.Empty, buildTextRecord("", "en", false))
    }

    @Test
    fun rejectsEmptyLanguage() {
        assertInvalid(FieldKey.LANGUAGE, FieldError.Empty, buildTextRecord("Hi", "  ", false))
    }

    @Test
    fun rejectsLongLanguage() {
        assertInvalid(FieldKey.LANGUAGE, FieldError.TooLong, buildTextRecord("Hi", "a".repeat(64), false))
    }

    @Test
    fun rejectsLanguageWithInvalidCharacters() {
        assertInvalid(FieldKey.LANGUAGE, FieldError.InvalidFormat, buildTextRecord("Hi", "e n", false))
    }

    @Test
    fun parsesUtf8Payload() {
        val content = parseTextPayload(buildTextRecord("Hello", "de", false).record().payload)!!
        assertEquals("de", content.languageCode)
        assertEquals("Hello", content.text)
        assertFalse(content.utf16)
    }

    @Test
    fun parsesUtf16Payload() {
        val content = parseTextPayload(buildTextRecord("Hello €", "en", true).record().payload)!!
        assertEquals("Hello €", content.text)
        assertTrue(content.utf16)
    }

    @Test
    fun parsesUtf16WithoutByteOrderMark() {
        val payload = byteArrayOf(0x82.toByte(), 0x65, 0x6E, 0x00, 0x48, 0x00, 0x69)
        assertEquals("Hi", parseTextPayload(payload)!!.text)
    }

    @Test
    fun rejectsEmptyPayload() {
        assertNull(parseTextPayload(ByteArray(0)))
    }

    @Test
    fun rejectsPayloadShorterThanLanguage() {
        assertNull(parseTextPayload(byteArrayOf(0x05, 0x65)))
    }
}
