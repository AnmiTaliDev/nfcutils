// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WifiRecordTest {
    @Test
    fun buildsWpa2Payload() {
        val record = buildWifiRecord("Net", "password1", WifiAuth.Wpa2Personal).record()
        assertEquals(Tnf.MIME, record.tnf)
        assertEquals("application/vnd.wfa.wsc", String(record.type))
        assertEquals(
            "10 4A 00 01 10 " +
                "10 0E 00 2F " +
                "10 26 00 01 01 " +
                "10 45 00 03 4E 65 74 " +
                "10 03 00 02 00 20 " +
                "10 0F 00 02 00 08 " +
                "10 27 00 09 70 61 73 73 77 6F 72 64 31 " +
                "10 20 00 06 FF FF FF FF FF FF",
            record.payload.toHex(),
        )
    }

    @Test
    fun buildsMixedModePayload() {
        val record = buildWifiRecord("Net", "password1", WifiAuth.WpaWpa2Personal).record()
        val hex = record.payload.toHex()
        assertEquals(true, hex.contains("10 03 00 02 00 22"))
        assertEquals(true, hex.contains("10 0F 00 02 00 0C"))
    }

    @Test
    fun openNetworkHasEmptyKeyAndIgnoresPassword() {
        val record = buildWifiRecord("Cafe", "ignored", WifiAuth.Open).record()
        val hex = record.payload.toHex()
        assertEquals(true, hex.contains("10 03 00 02 00 01"))
        assertEquals(true, hex.contains("10 0F 00 02 00 01"))
        assertEquals(true, hex.contains("10 27 00 00 10 20"))
    }

    @Test
    fun credentialLengthMatchesContent() {
        val payload = buildWifiRecord("Cafe", "", WifiAuth.Open).record().payload
        val credentialLength = ((payload[7].toInt() and 0xFF) shl 8) or (payload[8].toInt() and 0xFF)
        assertEquals(payload.size - 9, credentialLength)
    }

    @Test
    fun acceptsSixtyFourHexCharacterKey() {
        buildWifiRecord("Net", "a".repeat(64), WifiAuth.Wpa2Personal).record()
    }

    @Test
    fun rejectsEmptySsid() {
        assertInvalid(FieldKey.WIFI_SSID, FieldError.Empty, buildWifiRecord("", "password1", WifiAuth.Wpa2Personal))
    }

    @Test
    fun rejectsSsidLongerThan32Bytes() {
        assertInvalid(FieldKey.WIFI_SSID, FieldError.TooLong, buildWifiRecord("a".repeat(33), "password1", WifiAuth.Wpa2Personal))
        assertInvalid(FieldKey.WIFI_SSID, FieldError.TooLong, buildWifiRecord("é".repeat(17), "password1", WifiAuth.Wpa2Personal))
    }

    @Test
    fun rejectsMissingPasswordForSecuredNetwork() {
        assertInvalid(FieldKey.WIFI_PASSWORD, FieldError.Empty, buildWifiRecord("Net", "", WifiAuth.Wpa2Personal))
    }

    @Test
    fun rejectsShortPassword() {
        assertInvalid(FieldKey.WIFI_PASSWORD, FieldError.TooShort, buildWifiRecord("Net", "1234567", WifiAuth.Wpa2Personal))
    }

    @Test
    fun rejectsLongPassword() {
        assertInvalid(FieldKey.WIFI_PASSWORD, FieldError.TooLong, buildWifiRecord("Net", "a".repeat(65), WifiAuth.Wpa2Personal))
    }

    @Test
    fun rejectsSixtyFourCharacterKeyThatIsNotHex() {
        assertInvalid(FieldKey.WIFI_PASSWORD, FieldError.InvalidFormat, buildWifiRecord("Net", "g".repeat(64), WifiAuth.Wpa2Personal))
    }

    @Test
    fun parsesBuiltPayload() {
        val payload = buildWifiRecord("Net é", "password1", WifiAuth.Wpa2Personal).record().payload
        val credential = parseWifiPayload(payload)!!
        assertEquals("Net é", credential.ssid)
        assertEquals("password1", credential.networkKey)
        assertEquals(WifiAuth.Wpa2Personal, credential.auth)
    }

    @Test
    fun parsesOpenNetwork() {
        val credential = parseWifiPayload(buildWifiRecord("Cafe", "", WifiAuth.Open).record().payload)!!
        assertEquals("", credential.networkKey)
        assertEquals(WifiAuth.Open, credential.auth)
    }

    @Test
    fun parsesPayloadWithoutVersionAttribute() {
        val payload = buildWifiRecord("Net", "password1", WifiAuth.Wpa2Personal).record().payload
        assertEquals("Net", parseWifiPayload(payload.copyOfRange(5, payload.size))!!.ssid)
    }

    @Test
    fun unknownSecurityCombinationHasNoAuth() {
        val credential = WifiCredential("Net", 0x0008, 0x0002, "")
        assertNull(credential.auth)
    }

    @Test
    fun rejectsTruncatedPayload() {
        val payload = buildWifiRecord("Net", "password1", WifiAuth.Wpa2Personal).record().payload
        assertNull(parseWifiPayload(payload.copyOf(payload.size - 3)))
    }

    @Test
    fun rejectsPayloadWithoutCredential() {
        assertNull(parseWifiPayload(byteArrayOf(0x10, 0x4A, 0x00, 0x01, 0x10)))
    }

    @Test
    fun recognizesWifiMimeType() {
        assertEquals(true, isWifiMimeType("application/vnd.wfa.wsc"))
        assertEquals(false, isWifiMimeType("text/plain"))
    }
}
