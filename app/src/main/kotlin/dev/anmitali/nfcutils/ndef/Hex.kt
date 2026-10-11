// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

private const val HEX_DIGITS = "0123456789ABCDEF"

fun ByteArray.toHex(separator: String = " "): String {
    val builder = StringBuilder(size * (2 + separator.length))
    for ((index, byte) in withIndex()) {
        if (index > 0) builder.append(separator)
        val value = byte.toInt() and 0xFF
        builder.append(HEX_DIGITS[value shr 4])
        builder.append(HEX_DIGITS[value and 0x0F])
    }
    return builder.toString()
}

fun String.hexToBytes(): ByteArray {
    val digits = filter { it != ' ' && it != ':' }
    require(digits.length % 2 == 0) { "Odd number of hex digits" }
    return ByteArray(digits.length / 2) { index ->
        val high = Character.digit(digits[index * 2], 16)
        val low = Character.digit(digits[index * 2 + 1], 16)
        require(high >= 0 && low >= 0) { "Invalid hex digit" }
        ((high shl 4) or low).toByte()
    }
}
