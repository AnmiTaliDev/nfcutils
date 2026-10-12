// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

object Tnf {
    const val EMPTY = 0
    const val WELL_KNOWN = 1
    const val MIME = 2
    const val ABSOLUTE_URI = 3
    const val EXTERNAL = 4
    const val UNKNOWN = 5
}

class NdefRecordData(
    val tnf: Int,
    val type: ByteArray,
    val id: ByteArray,
    val payload: ByteArray,
) {
    override fun equals(other: Any?): Boolean =
        other is NdefRecordData &&
            tnf == other.tnf &&
            type.contentEquals(other.type) &&
            id.contentEquals(other.id) &&
            payload.contentEquals(other.payload)

    override fun hashCode(): Int {
        var result = tnf
        result = 31 * result + type.contentHashCode()
        result = 31 * result + id.contentHashCode()
        result = 31 * result + payload.contentHashCode()
        return result
    }
}

val EmptyRecord = NdefRecordData(Tnf.EMPTY, ByteArray(0), ByteArray(0), ByteArray(0))

fun wellKnownRecord(type: String, payload: ByteArray): NdefRecordData =
    NdefRecordData(Tnf.WELL_KNOWN, type.toByteArray(Charsets.US_ASCII), ByteArray(0), payload)

fun mimeRecord(mimeType: String, payload: ByteArray): NdefRecordData =
    NdefRecordData(Tnf.MIME, mimeType.toByteArray(Charsets.US_ASCII), ByteArray(0), payload)

fun externalRecord(type: String, payload: ByteArray): NdefRecordData =
    NdefRecordData(Tnf.EXTERNAL, type.toByteArray(Charsets.US_ASCII), ByteArray(0), payload)
