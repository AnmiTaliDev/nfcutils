// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

import org.junit.Assert.assertEquals
import org.junit.Test

class SmsAndAppRecordTest {
    private fun uriOf(build: RecordBuild) = decodeUriPayload(build.record().payload)

    @Test
    fun smsWithNumberOnly() {
        assertEquals("sms:+15105550101", uriOf(buildSmsRecord("+1 (510) 555-0101", "")))
    }

    @Test
    fun smsWithBody() {
        assertEquals("sms:+15105550101?body=hello%20there", uriOf(buildSmsRecord("+15105550101", "hello there")))
    }

    @Test
    fun smsEncodesReservedCharactersInBody() {
        assertEquals("sms:123?body=a%26b%3Dc%0A%C3%A9", uriOf(buildSmsRecord("123", "a&b=c\né")))
    }

    @Test
    fun smsUriHasNoAbbreviationPrefix() {
        assertEquals(0x00, buildSmsRecord("123", "").record().payload[0].toInt())
    }

    @Test
    fun smsRejectsEmptyNumber() {
        assertInvalid(FieldKey.SMS_NUMBER, FieldError.Empty, buildSmsRecord(" ", "hi"))
    }

    @Test
    fun smsRejectsInvalidNumber() {
        assertInvalid(FieldKey.SMS_NUMBER, FieldError.InvalidFormat, buildSmsRecord("12ab", "hi"))
    }

    @Test
    fun appRecordIsExternalTypeWithPackagePayload() {
        val record = buildAppRecord("com.example.app").record()
        assertEquals(Tnf.EXTERNAL, record.tnf)
        assertEquals("android.com:pkg", String(record.type))
        assertEquals("com.example.app", String(record.payload))
    }

    @Test
    fun appRecordTrimsPackageName() {
        assertEquals("com.example", String(buildAppRecord("  com.example ").record().payload))
    }

    @Test
    fun appRecordAcceptsDigitsAndUnderscores() {
        buildAppRecord("org.my_app2.sub1").record()
    }

    @Test
    fun appRecordRejectsEmptyName() {
        assertInvalid(FieldKey.APP_PACKAGE, FieldError.Empty, buildAppRecord(""))
    }

    @Test
    fun appRecordRejectsInvalidNames() {
        assertInvalid(FieldKey.APP_PACKAGE, FieldError.InvalidFormat, buildAppRecord("example"))
        assertInvalid(FieldKey.APP_PACKAGE, FieldError.InvalidFormat, buildAppRecord("com..example"))
        assertInvalid(FieldKey.APP_PACKAGE, FieldError.InvalidFormat, buildAppRecord("1com.example"))
        assertInvalid(FieldKey.APP_PACKAGE, FieldError.InvalidFormat, buildAppRecord("com.exa mple"))
    }

    @Test
    fun appRecordIsParsedAsAndroidApp() {
        val parsed = parseRecord(buildAppRecord("com.example.app").record()) as ParsedRecord.AndroidApp
        assertEquals("com.example.app", parsed.packageName)
        assertEquals(ParsedKind.AndroidApp, parsed.kind)
    }

    @Test
    fun appRecordSummaryIsPackageName() {
        assertEquals("com.example.app", describeRecord(buildAppRecord("com.example.app").record()).summary)
    }

    @Test
    fun otherExternalRecordsStayExternal() {
        val record = externalRecord("example.com:thing", byteArrayOf(1))
        assertEquals(ParsedKind.External, parseRecord(record).kind)
    }

    @Test
    fun emptyAppPayloadStaysExternal() {
        assertEquals(ParsedKind.External, parseRecord(externalRecord("android.com:pkg", ByteArray(0))).kind)
    }
}
