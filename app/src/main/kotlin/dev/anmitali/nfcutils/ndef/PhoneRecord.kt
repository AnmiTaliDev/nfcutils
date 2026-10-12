// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

private val PhonePattern = Regex("^\\+?[0-9*#]+$")

fun cleanPhoneNumber(number: String): String = number.filter { it !in " -().\t" }

fun isValidPhoneNumber(cleaned: String): Boolean = PhonePattern.matches(cleaned)

fun buildPhoneRecord(number: String): RecordBuild {
    val cleaned = cleanPhoneNumber(number)
    if (cleaned.isEmpty()) return RecordBuild.Invalid(FieldKey.PHONE, FieldError.Empty)
    if (!isValidPhoneNumber(cleaned)) return RecordBuild.Invalid(FieldKey.PHONE, FieldError.InvalidFormat)
    return RecordBuild.Ok(uriRecord("tel:$cleaned"))
}
