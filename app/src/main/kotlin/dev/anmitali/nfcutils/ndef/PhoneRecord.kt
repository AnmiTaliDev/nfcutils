// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

private val PhonePattern = Regex("^\\+?[0-9*#]+$")

fun buildPhoneRecord(number: String): RecordBuild {
    val cleaned = number.filter { it !in " -().\t" }
    if (cleaned.isEmpty()) return RecordBuild.Invalid(FieldKey.PHONE, FieldError.Empty)
    if (!PhonePattern.matches(cleaned)) return RecordBuild.Invalid(FieldKey.PHONE, FieldError.InvalidFormat)
    return RecordBuild.Ok(uriRecord("tel:$cleaned"))
}
