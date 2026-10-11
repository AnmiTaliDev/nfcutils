// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

private val AddressPattern = Regex("^[^@\\s,;?&]+@[^@\\s,;?&]+\\.[^@\\s,;?&]+$")

fun percentEncode(value: String): String {
    val out = StringBuilder()
    for (byte in value.toByteArray(Charsets.UTF_8)) {
        val code = byte.toInt() and 0xFF
        val char = code.toChar()
        if (char in 'A'..'Z' || char in 'a'..'z' || char in '0'..'9' || char in "-._~") {
            out.append(char)
        } else {
            out.append('%')
            out.append("0123456789ABCDEF"[code shr 4])
            out.append("0123456789ABCDEF"[code and 0x0F])
        }
    }
    return out.toString()
}

fun buildEmailRecord(address: String, subject: String, body: String): RecordBuild {
    val trimmed = address.trim()
    if (trimmed.isEmpty()) return RecordBuild.Invalid(FieldKey.EMAIL_ADDRESS, FieldError.Empty)
    if (!AddressPattern.matches(trimmed)) return RecordBuild.Invalid(FieldKey.EMAIL_ADDRESS, FieldError.InvalidFormat)
    val query = buildList {
        if (subject.isNotEmpty()) add("subject=" + percentEncode(subject))
        if (body.isNotEmpty()) add("body=" + percentEncode(body))
    }
    val suffix = if (query.isEmpty()) "" else "?" + query.joinToString("&")
    return RecordBuild.Ok(uriRecord("mailto:$trimmed$suffix"))
}
