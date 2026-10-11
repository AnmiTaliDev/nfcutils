// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

import java.math.BigDecimal

private val MaxLatitude = BigDecimal(90)
private val MaxLongitude = BigDecimal(180)

private fun parseCoordinate(value: String): BigDecimal? =
    value.trim().replace(',', '.').toBigDecimalOrNull()

private fun formatCoordinate(value: BigDecimal): String =
    if (value.signum() == 0) "0" else value.stripTrailingZeros().toPlainString()

fun buildGeoRecord(latitude: String, longitude: String): RecordBuild {
    if (latitude.isBlank()) return RecordBuild.Invalid(FieldKey.LATITUDE, FieldError.Empty)
    if (longitude.isBlank()) return RecordBuild.Invalid(FieldKey.LONGITUDE, FieldError.Empty)
    val lat = parseCoordinate(latitude) ?: return RecordBuild.Invalid(FieldKey.LATITUDE, FieldError.InvalidFormat)
    val lon = parseCoordinate(longitude) ?: return RecordBuild.Invalid(FieldKey.LONGITUDE, FieldError.InvalidFormat)
    if (lat.abs() > MaxLatitude) return RecordBuild.Invalid(FieldKey.LATITUDE, FieldError.OutOfRange)
    if (lon.abs() > MaxLongitude) return RecordBuild.Invalid(FieldKey.LONGITUDE, FieldError.OutOfRange)
    return RecordBuild.Ok(uriRecord("geo:${formatCoordinate(lat)},${formatCoordinate(lon)}"))
}
