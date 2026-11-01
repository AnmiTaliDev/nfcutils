// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordActionTest {
    private fun action(record: NdefRecordData) = recordAction(describeRecord(record))

    @Test
    fun opensWebAndPhoneUris() {
        assertEquals("https://example.com/a", (action(uriRecord("https://example.com/a")) as RecordAction.OpenUri).uri)
        assertEquals("tel:+123456", (action(uriRecord("tel:+123456")) as RecordAction.OpenUri).uri)
        assertEquals("geo:1.5,2.5", (action(uriRecord("geo:1.5,2.5")) as RecordAction.OpenUri).uri)
    }

    @Test
    fun usesFullUriWhenSummaryIsTruncated() {
        val uri = "https://example.com/" + "a".repeat(200)
        assertEquals(uri, (action(uriRecord(uri)) as RecordAction.OpenUri).uri)
    }

    @Test
    fun ignoresOtherSchemes() {
        assertNull(action(uriRecord("file:///sdcard/a")))
        assertNull(action(uriRecord("intent://scan#Intent;end")))
        assertNull(action(uriRecord("btspp://001122334455")))
    }

    @Test
    fun smartPosterOpensItsUri() {
        val poster = wellKnownRecord("Sp", NdefCodec.encode(listOf(uriRecord("https://example.com"))))
        assertEquals("https://example.com", (action(poster) as RecordAction.OpenUri).uri)
    }

    @Test
    fun wifiCopiesPassword() {
        val record = buildWifiRecord("Net", "password1", WifiAuth.Wpa2Personal).record()
        assertEquals("password1", (action(record) as RecordAction.CopyWifiPassword).password)
    }

    @Test
    fun openWifiHasNoAction() {
        assertNull(action(buildWifiRecord("Cafe", "", WifiAuth.Open).record()))
    }

    @Test
    fun androidAppOpensStore() {
        val record = buildAppRecord("com.example.app").record()
        assertEquals("com.example.app", (action(record) as RecordAction.OpenStore).packageName)
    }

    @Test
    fun textHasNoAction() {
        assertNull(action(buildTextRecord("Hello", "en", false).record()))
    }

    @Test
    fun invalidHexHasNoAction() {
        assertTrue(recordAction(RecordInfo(ParsedKind.Uri, "x", "", "ZZ")) == null)
    }
}
