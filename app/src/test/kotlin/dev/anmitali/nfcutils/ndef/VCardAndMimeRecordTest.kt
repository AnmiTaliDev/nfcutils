// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

import org.junit.Assert.assertEquals
import org.junit.Test

class VCardAndMimeRecordTest {
    @Test
    fun vCardWithAllFields() {
        val record = buildVCardRecord("Ann", "Lee", "+123", "a@b.co", "Acme", "https://example.com").record()
        assertEquals(Tnf.MIME, record.tnf)
        assertEquals("text/vcard", String(record.type))
        assertEquals(
            "BEGIN:VCARD\r\nVERSION:3.0\r\nN:Lee;Ann;;;\r\nFN:Ann Lee\r\nORG:Acme\r\nTEL:+123\r\n" +
                "EMAIL:a@b.co\r\nURL:https://example.com\r\nEND:VCARD\r\n",
            String(record.payload),
        )
    }

    @Test
    fun vCardOmitsEmptyOptionalFields() {
        val record = buildVCardRecord("Ann", "", "", "", "", "").record()
        assertEquals("BEGIN:VCARD\r\nVERSION:3.0\r\nN:;Ann;;;\r\nFN:Ann\r\nEND:VCARD\r\n", String(record.payload))
    }

    @Test
    fun vCardAcceptsLastNameOnly() {
        val record = buildVCardRecord("", "Lee", "", "", "", "").record()
        assertEquals("BEGIN:VCARD\r\nVERSION:3.0\r\nN:Lee;;;;\r\nFN:Lee\r\nEND:VCARD\r\n", String(record.payload))
    }

    @Test
    fun vCardEscapesSpecialCharacters() {
        val record = buildVCardRecord("Ann;", "Le,e", "", "", "A\\B\nC", "").record()
        val text = String(record.payload)
        assertEquals(true, text.contains("N:Le\\,e;Ann\\;;;;\r\n"))
        assertEquals(true, text.contains("ORG:A\\\\B\\nC\r\n"))
    }

    @Test
    fun vCardEncodesUtf8() {
        val record = buildVCardRecord("Émile", "", "", "", "", "").record()
        assertEquals(true, record.payload.toHex().contains("C3 89 6D 69 6C 65"))
    }

    @Test
    fun vCardRequiresName() {
        assertInvalid(FieldKey.FIRST_NAME, FieldError.Empty, buildVCardRecord(" ", "", "+1", "", "", ""))
    }

    @Test
    fun mimeBuildsRecord() {
        val record = buildMimeRecord("text/plain", "hello").record()
        assertEquals(Tnf.MIME, record.tnf)
        assertEquals("text/plain", String(record.type))
        assertEquals("hello", String(record.payload))
    }

    @Test
    fun mimeNormalizesTypeCase() {
        assertEquals("application/json", String(buildMimeRecord(" Application/JSON ", "{}").record().type))
    }

    @Test
    fun mimeAcceptsStructuredSuffixTypes() {
        assertEquals("application/vnd.example+json", String(buildMimeRecord("application/vnd.example+json", "x").record().type))
    }

    @Test
    fun mimeEncodesDataAsUtf8() {
        assertEquals("C3 A9", buildMimeRecord("text/plain", "é").record().payload.toHex())
    }

    @Test
    fun mimeRejectsEmptyType() {
        assertInvalid(FieldKey.MIME_TYPE, FieldError.Empty, buildMimeRecord("", "x"))
    }

    @Test
    fun mimeRejectsTypeWithoutSlash() {
        assertInvalid(FieldKey.MIME_TYPE, FieldError.InvalidFormat, buildMimeRecord("plain", "x"))
    }

    @Test
    fun mimeRejectsTypeWithSpaces() {
        assertInvalid(FieldKey.MIME_TYPE, FieldError.InvalidFormat, buildMimeRecord("text/pl ain", "x"))
    }

    @Test
    fun mimeRejectsEmptyData() {
        assertInvalid(FieldKey.MIME_DATA, FieldError.Empty, buildMimeRecord("text/plain", ""))
    }
}
