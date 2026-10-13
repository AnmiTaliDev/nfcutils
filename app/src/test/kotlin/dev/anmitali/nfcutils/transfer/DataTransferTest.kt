// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.transfer

import dev.anmitali.nfcutils.ndef.DraftKind
import dev.anmitali.nfcutils.ndef.FieldKey
import dev.anmitali.nfcutils.ndef.ParsedKind
import dev.anmitali.nfcutils.ndef.RecordDraft
import dev.anmitali.nfcutils.ndef.RecordInfo
import dev.anmitali.nfcutils.nfc.TagDetails
import dev.anmitali.nfcutils.nfc.TagPage
import dev.anmitali.nfcutils.nfc.TagType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DataTransferTest {
    private val fullScan = TagDetails(
        scannedAt = 1_700_000_000_123L,
        uid = "04:A1:B2:C3:D4:E5:F6",
        tagType = TagType.Ntag215,
        technologies = listOf("NfcA", "MifareUltralight", "Ndef"),
        atqa = "44 00",
        sak = 0,
        maxSize = 496,
        usedSize = 42,
        writable = true,
        canMakeReadOnly = false,
        formatable = false,
        unsupported = false,
        records = listOf(
            RecordInfo(ParsedKind.Text, "Hello | world\nline", "en, UTF-8", "02 65 6E"),
            RecordInfo(ParsedKind.Uri, "https://example.com", "", "04 65"),
        ),
        pages = listOf(TagPage(0, "04 A1 B2 C3"), TagPage(1, "D4 E5 F6 00")),
        nfcCounter = 7,
        signature = "00 01 02",
    )

    private val minimalScan = TagDetails(
        scannedAt = 5L,
        uid = "01",
        tagType = TagType.Unknown,
        technologies = emptyList(),
        atqa = null,
        sak = null,
        maxSize = null,
        usedSize = null,
        writable = null,
        canMakeReadOnly = null,
        formatable = true,
        unsupported = true,
        records = emptyList(),
        pages = emptyList(),
    )

    private fun draft(kind: DraftKind, vararg fields: Pair<String, String>) = RecordDraft(kind, mapOf(*fields))

    @Test
    fun setsRoundTrip() {
        val sets = listOf(
            ExportedSet(
                "Office Wi-Fi",
                listOf(
                    draft(DraftKind.Wifi, FieldKey.WIFI_SSID to "Net", FieldKey.WIFI_PASSWORD to "pass=word 1;2"),
                    draft(DraftKind.Text, FieldKey.TEXT to "multi\nline", FieldKey.LANGUAGE to "en"),
                ),
            ),
            ExportedSet("Second", listOf(draft(DraftKind.Url, FieldKey.URL to "https://example.com"))),
        )
        val parsed = parseExport(buildExport(sets, emptyList()))!!
        assertEquals(2, parsed.sets.size)
        assertEquals("Office Wi-Fi", parsed.sets[0].name)
        assertEquals(listOf(DraftKind.Wifi, DraftKind.Text), parsed.sets[0].drafts.map { it.kind })
        assertEquals("pass=word 1;2", parsed.sets[0].drafts[0].field(FieldKey.WIFI_PASSWORD))
        assertEquals("multi\nline", parsed.sets[0].drafts[1].field(FieldKey.TEXT))
        assertEquals("Second", parsed.sets[1].name)
        assertTrue(parsed.scans.isEmpty())
    }

    @Test
    fun emptyFieldsAreNotExported() {
        val text = buildExport(
            listOf(ExportedSet("S", listOf(draft(DraftKind.Url, FieldKey.URL to "https://x.y", FieldKey.TEXT to "")))),
            emptyList(),
        )
        assertFalse(text.contains("f.text"))
    }

    @Test
    fun unknownRecordKindsAreSkipped() {
        val text = "NFC-UTILS-EXPORT 1\n\n[set]\nname=S\n\n[record]\nkind=Hologram\nf.x=1\n\n[record]\nkind=Url\nf.url=https%3A%2F%2Fx.y\n"
        val parsed = parseExport(text)!!
        assertEquals(1, parsed.sets.single().drafts.size)
        assertEquals(DraftKind.Url, parsed.sets.single().drafts.single().kind)
    }

    @Test
    fun recordsBeforeAnySetAreIgnored() {
        val text = "NFC-UTILS-EXPORT 1\n\n[record]\nkind=Url\nf.url=a\n"
        assertTrue(parseExport(text)!!.sets.isEmpty())
    }

    @Test
    fun setsWithoutNameOrRecordsAreDropped() {
        val text = "NFC-UTILS-EXPORT 1\n\n[set]\nname=Empty\n\n[set]\nname=\n\n[record]\nkind=Url\nf.url=a\n"
        assertTrue(parseExport(text)!!.sets.isEmpty())
    }

    @Test
    fun unknownBlockTypesAreIgnored() {
        val text = "NFC-UTILS-EXPORT 1\n\n[future]\nx=1\n\n[set]\nname=S\n\n[record]\nkind=Url\nf.url=a\n"
        assertEquals(1, parseExport(text)!!.sets.size)
    }

    @Test
    fun fullScanRoundTrip() {
        val parsed = parseExport(buildExport(emptyList(), listOf(fullScan)))!!
        val scan = parsed.scans.single()
        assertEquals(fullScan.scannedAt, scan.scannedAt)
        assertEquals(fullScan.uid, scan.uid)
        assertEquals(TagType.Ntag215, scan.tagType)
        assertEquals(fullScan.technologies, scan.technologies)
        assertEquals("44 00", scan.atqa)
        assertEquals(0, scan.sak)
        assertEquals(496, scan.maxSize)
        assertEquals(42, scan.usedSize)
        assertEquals(true, scan.writable)
        assertEquals(false, scan.canMakeReadOnly)
        assertEquals(7, scan.nfcCounter)
        assertEquals("00 01 02", scan.signature)
        assertEquals(2, scan.records.size)
        assertEquals(ParsedKind.Text, scan.records[0].kind)
        assertEquals("Hello | world\nline", scan.records[0].summary)
        assertEquals("en, UTF-8", scan.records[0].detail)
        assertEquals("02 65 6E", scan.records[0].payloadHex)
        assertEquals(listOf(0, 1), scan.pages.map { it.index })
        assertEquals("D4 E5 F6 00", scan.pages[1].hex)
    }

    @Test
    fun minimalScanRoundTripKeepsNulls() {
        val scan = parseExport(buildExport(emptyList(), listOf(minimalScan)))!!.scans.single()
        assertNull(scan.atqa)
        assertNull(scan.sak)
        assertNull(scan.maxSize)
        assertNull(scan.usedSize)
        assertNull(scan.writable)
        assertNull(scan.canMakeReadOnly)
        assertNull(scan.nfcCounter)
        assertNull(scan.signature)
        assertTrue(scan.formatable)
        assertTrue(scan.unsupported)
        assertTrue(scan.records.isEmpty())
        assertTrue(scan.pages.isEmpty())
    }

    @Test
    fun unknownTagTypeAndRecordKindFallBackToUnknown() {
        val text = "NFC-UTILS-EXPORT 1\n\n[scan]\nscannedAt=1\nuid=01\ntagType=Teleporter\nrecord=Hologram%7Cs%7C%7C00\n"
        val scan = parseExport(text)!!.scans.single()
        assertEquals(TagType.Unknown, scan.tagType)
        assertEquals(ParsedKind.Unknown, scan.records.single().kind)
    }

    @Test
    fun scansWithoutRequiredFieldsAreSkipped() {
        val text = "NFC-UTILS-EXPORT 1\n\n[scan]\nuid=01\n\n[scan]\nscannedAt=abc\nuid=01\n\n[scan]\nscannedAt=1\n\n[scan]\nscannedAt=2\nuid=02\n"
        assertEquals(listOf("02"), parseExport(text)!!.scans.map { it.uid })
    }

    @Test
    fun malformedRecordsAndPagesAreSkipped() {
        val text = "NFC-UTILS-EXPORT 1\n\n[scan]\nscannedAt=1\nuid=01\nrecord=onlyone\npage=nocolon\npage=x%3Aab\npage=3%3Aab\n"
        val scan = parseExport(text)!!.scans.single()
        assertTrue(scan.records.isEmpty())
        assertEquals(listOf(3), scan.pages.map { it.index })
    }

    @Test
    fun setsAndScansCanBeExportedTogether() {
        val sets = listOf(ExportedSet("S", listOf(draft(DraftKind.Url, FieldKey.URL to "https://x.y"))))
        val parsed = parseExport(buildExport(sets, listOf(fullScan, minimalScan)))!!
        assertEquals(1, parsed.sets.size)
        assertEquals(listOf("04:A1:B2:C3:D4:E5:F6", "01"), parsed.scans.map { it.uid })
    }

    @Test
    fun invalidFileIsRejected() {
        assertNull(parseExport("hello"))
    }
}
