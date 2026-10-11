// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

private fun escapeVCard(value: String): String =
    value.replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace("\r\n", "\\n")
        .replace("\n", "\\n")

fun buildVCardRecord(
    firstName: String,
    lastName: String,
    phone: String,
    email: String,
    organization: String,
    website: String,
): RecordBuild {
    val first = firstName.trim()
    val last = lastName.trim()
    if (first.isEmpty() && last.isEmpty()) return RecordBuild.Invalid(FieldKey.FIRST_NAME, FieldError.Empty)
    val lines = buildList {
        add("BEGIN:VCARD")
        add("VERSION:3.0")
        add("N:${escapeVCard(last)};${escapeVCard(first)};;;")
        add("FN:${escapeVCard(listOf(first, last).filter { it.isNotEmpty() }.joinToString(" "))}")
        if (organization.isNotBlank()) add("ORG:${escapeVCard(organization.trim())}")
        if (phone.isNotBlank()) add("TEL:${escapeVCard(phone.trim())}")
        if (email.isNotBlank()) add("EMAIL:${escapeVCard(email.trim())}")
        if (website.isNotBlank()) add("URL:${escapeVCard(website.trim())}")
        add("END:VCARD")
    }
    val text = lines.joinToString("\r\n", postfix = "\r\n")
    return RecordBuild.Ok(mimeRecord("text/vcard", text.toByteArray(Charsets.UTF_8)))
}
