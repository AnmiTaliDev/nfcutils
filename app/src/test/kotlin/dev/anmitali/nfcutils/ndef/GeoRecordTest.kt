// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

import org.junit.Assert.assertEquals
import org.junit.Test

class GeoRecordTest {
    private fun uriOf(build: RecordBuild) = decodeUriPayload(build.record().payload)

    @Test
    fun buildsGeoUri() {
        assertEquals("geo:52.52,13.405", uriOf(buildGeoRecord("52.5200", "13.4050")))
    }

    @Test
    fun geoUriHasNoAbbreviationPrefix() {
        assertEquals(0x00, buildGeoRecord("1", "2").record().payload[0].toInt())
    }

    @Test
    fun acceptsCommaAsDecimalSeparator() {
        assertEquals("geo:-33.8688,151.2093", uriOf(buildGeoRecord("-33,8688", "151,2093")))
    }

    @Test
    fun keepsIntegerValuesWithoutExponent() {
        assertEquals("geo:80,-100", uriOf(buildGeoRecord("80", "-100")))
    }

    @Test
    fun normalizesZero() {
        assertEquals("geo:0,0", uriOf(buildGeoRecord("0.0", "-0.00")))
    }

    @Test
    fun acceptsBoundaryValues() {
        assertEquals("geo:90,180", uriOf(buildGeoRecord("90", "180")))
        assertEquals("geo:-90,-180", uriOf(buildGeoRecord("-90", "-180")))
    }

    @Test
    fun rejectsLatitudeOutOfRange() {
        assertInvalid(FieldKey.LATITUDE, FieldError.OutOfRange, buildGeoRecord("90.1", "0"))
    }

    @Test
    fun rejectsLongitudeOutOfRange() {
        assertInvalid(FieldKey.LONGITUDE, FieldError.OutOfRange, buildGeoRecord("0", "-180.5"))
    }

    @Test
    fun rejectsNonNumbers() {
        assertInvalid(FieldKey.LATITUDE, FieldError.InvalidFormat, buildGeoRecord("abc", "0"))
        assertInvalid(FieldKey.LONGITUDE, FieldError.InvalidFormat, buildGeoRecord("0", "1.2.3"))
    }

    @Test
    fun rejectsBlankValues() {
        assertInvalid(FieldKey.LATITUDE, FieldError.Empty, buildGeoRecord(" ", "0"))
        assertInvalid(FieldKey.LONGITUDE, FieldError.Empty, buildGeoRecord("0", ""))
    }
}
