// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordParserTest {
    @Test
    fun parsesTextRecord() {
        val parsed = parseRecord(buildTextRecord("Hello", "en", false).record()) as ParsedRecord.Text
        assertEquals("Hello", parsed.content.text)
        assertEquals("en", parsed.content.languageCode)
        assertEquals(ParsedKind.Text, parsed.kind)
    }

    @Test
    fun parsesUriRecord() {
        val parsed = parseRecord(buildUriRecord("https://www.example.com").record()) as ParsedRecord.Uri
        assertEquals("https://www.example.com", parsed.uri)
    }

    @Test
    fun parsesAbsoluteUriRecord() {
        val record = NdefRecordData(Tnf.ABSOLUTE_URI, "https://example.com/a".toByteArray(), ByteArray(0), ByteArray(0))
        assertEquals("https://example.com/a", (parseRecord(record) as ParsedRecord.Uri).uri)
    }

    @Test
    fun parsesMimeRecordWithTextPreview() {
        val parsed = parseRecord(buildMimeRecord("text/plain", "hello").record()) as ParsedRecord.Mime
        assertEquals("text/plain", parsed.mimeType)
        assertEquals("hello", parsed.textPreview)
    }

    @Test
    fun parsesBinaryMimeRecordWithoutPreview() {
        val parsed = parseRecord(mimeRecord("image/png", byteArrayOf(1, 2, 3))) as ParsedRecord.Mime
        assertNull(parsed.textPreview)
    }

    @Test
    fun parsesVCardAsMime() {
        val record = buildVCardRecord("Ann", "Lee", "", "", "", "").record()
        assertEquals("text/vcard", (parseRecord(record) as ParsedRecord.Mime).mimeType)
    }

    @Test
    fun parsesWifiRecord() {
        val record = buildWifiRecord("Net", "password1", WifiAuth.Wpa2Personal).record()
        val parsed = parseRecord(record) as ParsedRecord.Wifi
        assertEquals("Net", parsed.credential.ssid)
        assertEquals(ParsedKind.Wifi, parsed.kind)
    }

    @Test
    fun malformedWifiPayloadFallsBackToMime() {
        val parsed = parseRecord(mimeRecord("application/vnd.wfa.wsc", byteArrayOf(1, 2)))
        assertTrue(parsed is ParsedRecord.Mime)
    }

    @Test
    fun parsesSmartPoster() {
        val title = buildTextRecord("Example", "en", false).record()
        val uri = uriRecord("https://example.com")
        val poster = wellKnownRecord("Sp", NdefCodec.encode(listOf(uri, title)))
        val parsed = parseRecord(poster) as ParsedRecord.SmartPoster
        assertEquals("Example", parsed.title)
        assertEquals("https://example.com", parsed.uri)
    }

    @Test
    fun smartPosterWithoutTitle() {
        val poster = wellKnownRecord("Sp", NdefCodec.encode(listOf(uriRecord("https://example.com"))))
        val parsed = parseRecord(poster) as ParsedRecord.SmartPoster
        assertNull(parsed.title)
        assertEquals("https://example.com", parsed.uri)
    }

    @Test
    fun malformedSmartPosterIsUnknown() {
        val parsed = parseRecord(wellKnownRecord("Sp", byteArrayOf(1, 2, 3)))
        assertTrue(parsed is ParsedRecord.Unknown)
    }

    @Test
    fun parsesExternalRecord() {
        val record = NdefRecordData(Tnf.EXTERNAL, "example.com:thing".toByteArray(), ByteArray(0), byteArrayOf(1))
        val parsed = parseRecord(record) as ParsedRecord.External
        assertEquals("example.com:thing", parsed.type)
    }

    @Test
    fun parsesEmptyRecord() {
        assertEquals(ParsedRecord.Empty, parseRecord(EmptyRecord))
    }

    @Test
    fun unknownTnfIsUnknown() {
        val parsed = parseRecord(NdefRecordData(Tnf.UNKNOWN, ByteArray(0), ByteArray(0), byteArrayOf(1))) as ParsedRecord.Unknown
        assertEquals(Tnf.UNKNOWN, parsed.tnf)
    }

    @Test
    fun unknownWellKnownTypeIsUnknownWithTypeHex() {
        val parsed = parseRecord(wellKnownRecord("Hr", byteArrayOf(1))) as ParsedRecord.Unknown
        assertEquals("4872", parsed.type)
    }

    @Test
    fun malformedTextPayloadIsUnknown() {
        assertTrue(parseRecord(wellKnownRecord("T", ByteArray(0))) is ParsedRecord.Unknown)
    }
}
