// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

import org.junit.Assert.assertEquals
import org.junit.Test

class PhoneAndEmailRecordTest {
    private fun uriOf(build: RecordBuild) = decodeUriPayload(build.record().payload)

    @Test
    fun phoneBuildsTelUri() {
        assertEquals("tel:+491234567", uriOf(buildPhoneRecord("+49 123-4567")))
    }

    @Test
    fun phoneUsesTelPrefixCode() {
        assertEquals(0x05, buildPhoneRecord("123").record().payload[0].toInt())
    }

    @Test
    fun phoneStripsFormattingCharacters() {
        assertEquals("tel:0123456789", uriOf(buildPhoneRecord("(012) 345.6789")))
    }

    @Test
    fun phoneAllowsStarAndHash() {
        assertEquals("tel:*123#", uriOf(buildPhoneRecord("*123#")))
    }

    @Test
    fun phoneRejectsEmpty() {
        assertInvalid(FieldKey.PHONE, FieldError.Empty, buildPhoneRecord(" - "))
    }

    @Test
    fun phoneRejectsLetters() {
        assertInvalid(FieldKey.PHONE, FieldError.InvalidFormat, buildPhoneRecord("12ab"))
    }

    @Test
    fun phoneRejectsPlusInTheMiddle() {
        assertInvalid(FieldKey.PHONE, FieldError.InvalidFormat, buildPhoneRecord("12+34"))
    }

    @Test
    fun emailWithAddressOnly() {
        assertEquals("mailto:a@b.co", uriOf(buildEmailRecord("a@b.co", "", "")))
    }

    @Test
    fun emailUsesMailtoPrefixCode() {
        assertEquals(0x06, buildEmailRecord("a@b.co", "", "").record().payload[0].toInt())
    }

    @Test
    fun emailEncodesSubjectAndBody() {
        assertEquals(
            "mailto:a@b.co?subject=Hello%20World&body=Line1%0ALine2",
            uriOf(buildEmailRecord("a@b.co", "Hello World", "Line1\nLine2")),
        )
    }

    @Test
    fun emailEncodesNonAsciiAndReservedCharacters() {
        assertEquals(
            "mailto:a@b.co?subject=%C3%A9%26%3D%3F",
            uriOf(buildEmailRecord("a@b.co", "é&=?", "")),
        )
    }

    @Test
    fun emailWithBodyOnly() {
        assertEquals("mailto:a@b.co?body=x", uriOf(buildEmailRecord("a@b.co", "", "x")))
    }

    @Test
    fun emailTrimsAddress() {
        assertEquals("mailto:a@b.co", uriOf(buildEmailRecord(" a@b.co ", "", "")))
    }

    @Test
    fun emailRejectsEmpty() {
        assertInvalid(FieldKey.EMAIL_ADDRESS, FieldError.Empty, buildEmailRecord("", "", ""))
    }

    @Test
    fun emailRejectsMalformedAddress() {
        assertInvalid(FieldKey.EMAIL_ADDRESS, FieldError.InvalidFormat, buildEmailRecord("not-an-address", "", ""))
        assertInvalid(FieldKey.EMAIL_ADDRESS, FieldError.InvalidFormat, buildEmailRecord("a@b", "", ""))
        assertInvalid(FieldKey.EMAIL_ADDRESS, FieldError.InvalidFormat, buildEmailRecord("a b@c.de", "", ""))
    }

    @Test
    fun percentEncodeKeepsUnreservedCharacters() {
        assertEquals("AZaz09-._~", percentEncode("AZaz09-._~"))
    }
}
