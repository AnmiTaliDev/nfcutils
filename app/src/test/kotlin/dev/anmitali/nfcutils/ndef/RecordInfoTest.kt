// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

import org.junit.Assert.assertEquals
import org.junit.Test

class RecordInfoTest {
    private fun info(record: NdefRecordData) = describeRecord(record)

    @Test
    fun describesTextRecord() {
        val info = info(buildTextRecord("Hello", "en", false).record())
        assertEquals(ParsedKind.Text, info.kind)
        assertEquals("Hello", info.summary)
        assertEquals("en, UTF-8", info.detail)
        assertEquals("02 65 6E 48 65 6C 6C 6F", info.payloadHex)
    }

    @Test
    fun describesUtf16Text() {
        assertEquals("en, UTF-16", info(buildTextRecord("Hi", "en", true).record()).detail)
    }

    @Test
    fun describesUriRecord() {
        val info = info(buildUriRecord("https://example.com").record())
        assertEquals(ParsedKind.Uri, info.kind)
        assertEquals("https://example.com", info.summary)
        assertEquals("", info.detail)
    }

    @Test
    fun describesMimeRecordByType() {
        assertEquals("image/png", info(mimeRecord("image/png", byteArrayOf(1))).summary)
    }

    @Test
    fun describesWifiRecordBySsid() {
        assertEquals("Net", info(buildWifiRecord("Net", "password1", WifiAuth.Wpa2Personal).record()).summary)
    }

    @Test
    fun describesExternalRecordByType() {
        val record = NdefRecordData(Tnf.EXTERNAL, "example.com:thing".toByteArray(), ByteArray(0), ByteArray(0))
        assertEquals("example.com:thing", info(record).summary)
    }

    @Test
    fun describesEmptyRecordWithoutSummary() {
        val info = info(EmptyRecord)
        assertEquals(ParsedKind.Empty, info.kind)
        assertEquals("", info.summary)
        assertEquals("", info.payloadHex)
    }

    @Test
    fun smartPosterSummaryPrefersTitle() {
        val poster = wellKnownRecord(
            "Sp",
            NdefCodec.encode(listOf(uriRecord("https://example.com"), buildTextRecord("Title", "en", false).record())),
        )
        assertEquals("Title", info(poster).summary)
    }

    @Test
    fun smartPosterSummaryFallsBackToUri() {
        val poster = wellKnownRecord("Sp", NdefCodec.encode(listOf(uriRecord("https://example.com"))))
        assertEquals("https://example.com", info(poster).summary)
    }

    @Test
    fun truncatesLongSummary() {
        val summary = info(buildTextRecord("a".repeat(200), "en", false).record()).summary
        assertEquals(80, summary.length)
        assertEquals('…', summary.last())
    }

    @Test
    fun doesNotTruncateSummaryAtLimit() {
        val text = "a".repeat(80)
        assertEquals(text, info(buildTextRecord(text, "en", false).record()).summary)
    }

    @Test
    fun collapsesWhitespaceInSummary() {
        assertEquals("a b c", info(buildTextRecord("a\n  b\tc ", "en", false).record()).summary)
    }

    @Test
    fun entrySummaryJoinsRecordSummaries() {
        val records = listOf(
            info(buildTextRecord("Hello", "en", false).record()),
            info(buildUriRecord("https://example.com").record()),
        )
        assertEquals("Hello; https://example.com", entrySummary(records))
    }

    @Test
    fun entrySummarySkipsEmptySummaries() {
        val records = listOf(info(EmptyRecord), info(buildTextRecord("Hello", "en", false).record()))
        assertEquals("Hello", entrySummary(records))
    }

    @Test
    fun entrySummaryOfNoRecordsIsEmpty() {
        assertEquals("", entrySummary(emptyList()))
    }
}
