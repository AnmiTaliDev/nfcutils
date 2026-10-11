// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

private const val FLAG_MESSAGE_BEGIN = 0x80
private const val FLAG_MESSAGE_END = 0x40
private const val FLAG_CHUNKED = 0x20
private const val FLAG_SHORT_RECORD = 0x10
private const val FLAG_ID_LENGTH = 0x08
private const val MASK_TNF = 0x07
private const val SHORT_PAYLOAD_LIMIT = 255

object NdefCodec {
    fun encode(records: List<NdefRecordData>): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        for ((index, record) in records.withIndex()) {
            val short = record.payload.size <= SHORT_PAYLOAD_LIMIT
            val hasId = record.id.isNotEmpty()
            var header = record.tnf and MASK_TNF
            if (index == 0) header = header or FLAG_MESSAGE_BEGIN
            if (index == records.lastIndex) header = header or FLAG_MESSAGE_END
            if (short) header = header or FLAG_SHORT_RECORD
            if (hasId) header = header or FLAG_ID_LENGTH
            out.write(header)
            out.write(record.type.size)
            if (short) {
                out.write(record.payload.size)
            } else {
                val length = record.payload.size
                out.write(length ushr 24)
                out.write(length ushr 16)
                out.write(length ushr 8)
                out.write(length)
            }
            if (hasId) out.write(record.id.size)
            out.write(record.type)
            out.write(record.id)
            out.write(record.payload)
        }
        return out.toByteArray()
    }

    fun size(records: List<NdefRecordData>): Int {
        var total = 0
        for (record in records) {
            total += 1 + 1 + record.type.size + record.id.size + record.payload.size
            total += if (record.payload.size <= SHORT_PAYLOAD_LIMIT) 1 else 4
            if (record.id.isNotEmpty()) total += 1
        }
        return total
    }

    fun decode(bytes: ByteArray): List<NdefRecordData>? {
        val records = mutableListOf<NdefRecordData>()
        var position = 0
        var ended = false
        while (position < bytes.size && !ended) {
            val header = bytes[position++].toInt() and 0xFF
            if (records.isEmpty() != (header and FLAG_MESSAGE_BEGIN != 0)) return null
            if (header and FLAG_CHUNKED != 0) return null
            ended = header and FLAG_MESSAGE_END != 0
            if (position >= bytes.size) return null
            val typeLength = bytes[position++].toInt() and 0xFF
            val payloadLength: Int
            if (header and FLAG_SHORT_RECORD != 0) {
                if (position >= bytes.size) return null
                payloadLength = bytes[position++].toInt() and 0xFF
            } else {
                if (position + 4 > bytes.size) return null
                var length = 0L
                repeat(4) { length = (length shl 8) or (bytes[position++].toLong() and 0xFF) }
                if (length > bytes.size) return null
                payloadLength = length.toInt()
            }
            var idLength = 0
            if (header and FLAG_ID_LENGTH != 0) {
                if (position >= bytes.size) return null
                idLength = bytes[position++].toInt() and 0xFF
            }
            if (position + typeLength + idLength + payloadLength > bytes.size) return null
            val type = bytes.copyOfRange(position, position + typeLength)
            position += typeLength
            val id = bytes.copyOfRange(position, position + idLength)
            position += idLength
            val payload = bytes.copyOfRange(position, position + payloadLength)
            position += payloadLength
            records.add(NdefRecordData(header and MASK_TNF, type, id, payload))
        }
        return if (ended && position == bytes.size) records else null
    }
}
