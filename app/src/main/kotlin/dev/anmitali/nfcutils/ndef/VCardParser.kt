// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

class VCardFields(
    val firstName: String,
    val lastName: String,
    val phone: String,
    val email: String,
    val organization: String,
    val website: String,
    val street: String,
    val city: String,
    val postalCode: String,
    val country: String,
)

private fun splitStructured(value: String): List<String> {
    val parts = mutableListOf<String>()
    val current = StringBuilder()
    var index = 0
    while (index < value.length) {
        val char = value[index]
        if (char == '\\' && index + 1 < value.length) {
            val next = value[index + 1]
            current.append(if (next == 'n' || next == 'N') '\n' else next)
            index += 2
        } else if (char == ';') {
            parts.add(current.toString())
            current.clear()
            index++
        } else {
            current.append(char)
            index++
        }
    }
    parts.add(current.toString())
    return parts
}

private fun unfold(text: String): List<String> {
    val lines = mutableListOf<String>()
    for (line in text.split("\r\n", "\n")) {
        if ((line.startsWith(" ") || line.startsWith("\t")) && lines.isNotEmpty()) {
            lines[lines.lastIndex] = lines.last() + line.substring(1)
        } else {
            lines.add(line)
        }
    }
    return lines
}

fun parseVCard(text: String): VCardFields? {
    val lines = unfold(text)
    if (lines.none { it.trim().equals("BEGIN:VCARD", ignoreCase = true) }) return null
    val values = HashMap<String, String>()
    for (line in lines) {
        val separator = line.indexOf(':')
        if (separator < 0) continue
        val name = line.substring(0, separator).substringBefore(';').substringAfterLast('.').uppercase()
        values.putIfAbsent(name, line.substring(separator + 1))
    }
    val name = splitStructured(values["N"].orEmpty())
    val address = splitStructured(values["ADR"].orEmpty())
    val formattedName = splitStructured(values["FN"].orEmpty()).joinToString(";")
    val last = name.getOrElse(0) { "" }.trim()
    val first = name.getOrElse(1) { "" }.trim()
    return VCardFields(
        firstName = if (first.isEmpty() && last.isEmpty()) formattedName.trim() else first,
        lastName = last,
        phone = splitStructured(values["TEL"].orEmpty()).joinToString(";").trim(),
        email = splitStructured(values["EMAIL"].orEmpty()).joinToString(";").trim(),
        organization = splitStructured(values["ORG"].orEmpty()).first().trim(),
        website = splitStructured(values["URL"].orEmpty()).joinToString(";").trim(),
        street = address.getOrElse(2) { "" }.trim(),
        city = address.getOrElse(3) { "" }.trim(),
        postalCode = address.getOrElse(5) { "" }.trim(),
        country = address.getOrElse(6) { "" }.trim(),
    )
}
