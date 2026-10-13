// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.nfc

import dev.anmitali.nfcutils.ndef.toHex

val GetVersionCommand = byteArrayOf(0x60)
val ReadSignatureCommand = byteArrayOf(0x3C, 0x00)
val ReadCounterCommand = byteArrayOf(0x39, 0x02)

private const val VERSION_LENGTH = 8
private const val SIGNATURE_LENGTH = 32
private const val COUNTER_LENGTH = 3
private const val VENDOR_NXP = 0x04
private const val PRODUCT_NTAG = 0x04
private const val PRODUCT_ULTRALIGHT = 0x03
private const val STORAGE_128_BYTES = 0x0E
private const val STORAGE_NTAG213 = 0x0F
private const val STORAGE_NTAG215 = 0x11
private const val STORAGE_NTAG216 = 0x13

val VersionedNdefSizes = setOf(128, 144, 496, 872)

class UltralightFamily(val type: TagType, val pageCount: Int, val hasCounter: Boolean)

fun identifyUltralightFamily(version: ByteArray): UltralightFamily? {
    if (version.size != VERSION_LENGTH) return null
    if ((version[1].toInt() and 0xFF) != VENDOR_NXP) return null
    val product = version[2].toInt() and 0xFF
    val storage = version[6].toInt() and 0xFF
    return when {
        product == PRODUCT_NTAG && storage == STORAGE_128_BYTES -> UltralightFamily(TagType.Ntag212, 41, false)
        product == PRODUCT_NTAG && storage == STORAGE_NTAG213 -> UltralightFamily(TagType.Ntag213, 45, true)
        product == PRODUCT_NTAG && storage == STORAGE_NTAG215 -> UltralightFamily(TagType.Ntag215, 135, true)
        product == PRODUCT_NTAG && storage == STORAGE_NTAG216 -> UltralightFamily(TagType.Ntag216, 231, true)
        product == PRODUCT_ULTRALIGHT && storage == STORAGE_128_BYTES ->
            UltralightFamily(TagType.UltralightEv1Ul21, 41, false)
        else -> null
    }
}

fun parseCounter(response: ByteArray): Int? {
    if (response.size != COUNTER_LENGTH) return null
    return (response[0].toInt() and 0xFF) or
        ((response[1].toInt() and 0xFF) shl 8) or
        ((response[2].toInt() and 0xFF) shl 16)
}

fun parseSignature(response: ByteArray): String? =
    if (response.size == SIGNATURE_LENGTH) response.toHex() else null
