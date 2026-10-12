// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

fun buildSmsRecord(number: String, body: String): RecordBuild {
    val cleaned = cleanPhoneNumber(number)
    if (cleaned.isEmpty()) return RecordBuild.Invalid(FieldKey.SMS_NUMBER, FieldError.Empty)
    if (!isValidPhoneNumber(cleaned)) return RecordBuild.Invalid(FieldKey.SMS_NUMBER, FieldError.InvalidFormat)
    val suffix = if (body.isEmpty()) "" else "?body=" + percentEncode(body)
    return RecordBuild.Ok(uriRecord("sms:$cleaned$suffix"))
}
