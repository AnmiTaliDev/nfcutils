// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DraftImportTest {
    private fun draft(kind: DraftKind, vararg fields: Pair<String, String>): RecordDraft {
        var result = emptyDraft(kind)
        for ((key, value) in fields) result = result.withField(key, value)
        return result
    }

    private fun filled(draft: RecordDraft) = draft.kind to draft.fields.filterValues { it.isNotEmpty() }

    private fun roundTrip(original: RecordDraft) {
        val imported = recordsToDrafts(listOf(buildRecord(original).record()))
        assertEquals(1, imported.size)
        assertEquals(filled(original), filled(imported[0]))
    }

    @Test
    fun textRoundTrip() {
        roundTrip(draft(DraftKind.Text, FieldKey.TEXT to "Hello", FieldKey.LANGUAGE to "de"))
    }

    @Test
    fun utf16TextRoundTrip() {
        roundTrip(draft(DraftKind.Text, FieldKey.TEXT to "Hi €", FieldKey.ENCODING to FieldValue.ENCODING_UTF16))
    }

    @Test
    fun urlRoundTrip() {
        roundTrip(draft(DraftKind.Url, FieldKey.URL to "https://www.example.com/a?b=c"))
    }

    @Test
    fun phoneRoundTrip() {
        roundTrip(draft(DraftKind.Phone, FieldKey.PHONE to "+49123456"))
    }

    @Test
    fun smsRoundTrip() {
        roundTrip(draft(DraftKind.Sms, FieldKey.SMS_NUMBER to "+15105550101", FieldKey.SMS_BODY to "hello there & more"))
    }

    @Test
    fun emailRoundTrip() {
        roundTrip(
            draft(
                DraftKind.Email,
                FieldKey.EMAIL_ADDRESS to "a@b.co",
                FieldKey.EMAIL_SUBJECT to "Hi é",
                FieldKey.EMAIL_BODY to "Line1\nLine2",
            ),
        )
    }

    @Test
    fun geoRoundTrip() {
        roundTrip(draft(DraftKind.Geo, FieldKey.LATITUDE to "52.52", FieldKey.LONGITUDE to "-13.405"))
    }

    @Test
    fun wifiRoundTrip() {
        roundTrip(draft(DraftKind.Wifi, FieldKey.WIFI_SSID to "Net", FieldKey.WIFI_PASSWORD to "password1"))
        roundTrip(
            draft(
                DraftKind.Wifi,
                FieldKey.WIFI_SSID to "Net",
                FieldKey.WIFI_PASSWORD to "password1",
                FieldKey.WIFI_SECURITY to FieldValue.SECURITY_WPA_WPA2,
            ),
        )
        roundTrip(draft(DraftKind.Wifi, FieldKey.WIFI_SSID to "Cafe", FieldKey.WIFI_SECURITY to FieldValue.SECURITY_OPEN))
    }

    @Test
    fun contactRoundTrip() {
        roundTrip(
            draft(
                DraftKind.Contact,
                FieldKey.FIRST_NAME to "Ann",
                FieldKey.LAST_NAME to "Lee",
                FieldKey.CONTACT_PHONE to "+123",
                FieldKey.CONTACT_EMAIL to "a@b.co",
                FieldKey.ORGANIZATION to "Acme, Inc",
                FieldKey.WEBSITE to "https://example.com",
                FieldKey.STREET to "Main St 1",
                FieldKey.CITY to "Berlin",
                FieldKey.POSTAL_CODE to "10115",
                FieldKey.COUNTRY to "Germany",
            ),
        )
    }

    @Test
    fun appRoundTrip() {
        roundTrip(draft(DraftKind.App, FieldKey.APP_PACKAGE to "com.example.app"))
    }

    @Test
    fun textMimeRoundTrip() {
        roundTrip(draft(DraftKind.Mime, FieldKey.MIME_TYPE to "text/plain", FieldKey.MIME_DATA to "hello"))
    }

    @Test
    fun jsonMimeIsImportedAsMime() {
        roundTrip(draft(DraftKind.Mime, FieldKey.MIME_TYPE to "application/json", FieldKey.MIME_DATA to "{}"))
    }

    @Test
    fun smartPosterBecomesTextAndUrl() {
        val poster = wellKnownRecord(
            "Sp",
            NdefCodec.encode(listOf(uriRecord("https://example.com"), buildTextRecord("Title", "en", false).record())),
        )
        val imported = recordsToDrafts(listOf(poster))
        assertEquals(listOf(DraftKind.Text, DraftKind.Url), imported.map { it.kind })
        assertEquals("Title", imported[0].field(FieldKey.TEXT))
        assertEquals("https://example.com", imported[1].field(FieldKey.URL))
    }

    @Test
    fun keepsRecordOrder() {
        val imported = recordsToDrafts(
            listOf(uriRecord("https://example.com"), buildTextRecord("Hi", "en", false).record(), buildPhoneRecord("123").record()),
        )
        assertEquals(listOf(DraftKind.Url, DraftKind.Text, DraftKind.Phone), imported.map { it.kind })
    }

    @Test
    fun skipsRecordsThatCannotBeEdited() {
        val records = listOf(
            EmptyRecord,
            mimeRecord("image/png", byteArrayOf(1, 2, 3)),
            externalRecord("example.com:thing", byteArrayOf(1)),
            NdefRecordData(Tnf.UNKNOWN, ByteArray(0), ByteArray(0), byteArrayOf(1)),
        )
        assertTrue(recordsToDrafts(records).isEmpty())
    }

    @Test
    fun invalidPhoneUriFallsBackToUrl() {
        val imported = recordsToDrafts(listOf(uriRecord("tel:12ab")))
        assertEquals(DraftKind.Url, imported[0].kind)
        assertEquals("tel:12ab", imported[0].field(FieldKey.URL))
    }

    @Test
    fun invalidGeoUriFallsBackToUrl() {
        assertEquals(DraftKind.Url, recordsToDrafts(listOf(uriRecord("geo:abc")))[0].kind)
        assertEquals(DraftKind.Url, recordsToDrafts(listOf(uriRecord("geo:1")))[0].kind)
    }

    @Test
    fun invalidSmsNumberFallsBackToUrl() {
        assertEquals(DraftKind.Url, recordsToDrafts(listOf(uriRecord("sms:abc?body=x")))[0].kind)
    }

    @Test
    fun geoUriParametersAreIgnored() {
        val imported = recordsToDrafts(listOf(uriRecord("geo:52.5,13.4;u=35")))
        assertEquals(DraftKind.Geo, imported[0].kind)
        assertEquals("13.4", imported[0].field(FieldKey.LONGITUDE))
    }

    @Test
    fun schemeMatchingIsCaseInsensitive() {
        assertEquals(DraftKind.Phone, recordsToDrafts(listOf(uriRecord("TEL:123")))[0].kind)
    }

    @Test
    fun foreignVCardWithFormattedNameOnlyBecomesContact() {
        val record = mimeRecord("text/vcard", "BEGIN:VCARD\nVERSION:2.1\nFN:Ann Lee\nTEL;CELL:+123\nEND:VCARD".toByteArray())
        val imported = recordsToDrafts(listOf(record))
        assertEquals(DraftKind.Contact, imported[0].kind)
        assertEquals("Ann Lee", imported[0].field(FieldKey.FIRST_NAME))
        assertEquals("+123", imported[0].field(FieldKey.CONTACT_PHONE))
    }

    @Test
    fun vCardWithoutNameBecomesMime() {
        val record = mimeRecord("text/vcard", "BEGIN:VCARD\nTEL:+123\nEND:VCARD".toByteArray())
        assertEquals(DraftKind.Mime, recordsToDrafts(listOf(record))[0].kind)
    }
}
