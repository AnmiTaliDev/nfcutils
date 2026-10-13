// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.taginfo

import android.content.Context
import dev.anmitali.nfcutils.R
import dev.anmitali.nfcutils.nfc.TagDetails
import dev.anmitali.nfcutils.ui.common.labelRes

fun buildShareText(context: Context, details: TagDetails): String {
    val notAvailable = context.getString(R.string.value_not_available)
    fun line(label: Int, value: String) = context.getString(label) + ": " + value
    fun bytes(value: Int?) = if (value == null) notAvailable else context.getString(R.string.value_bytes, value)
    val lines = mutableListOf(
        line(R.string.info_tag_type, context.getString(details.tagType.labelRes())),
        line(R.string.info_uid, details.uid),
        line(R.string.info_technologies, details.technologies.joinToString(", ")),
    )
    if (details.atqa != null) lines.add(line(R.string.info_atqa, details.atqa))
    if (details.sak != null) lines.add(line(R.string.info_sak, "0x" + details.sak.toString(16).uppercase().padStart(2, '0')))
    lines.add(line(R.string.info_memory_size, bytes(details.maxSize)))
    lines.add(line(R.string.info_used_size, bytes(details.usedSize)))
    if (details.nfcCounter != null) lines.add(line(R.string.info_nfc_counter, details.nfcCounter.toString()))
    if (details.signature != null) lines.add(line(R.string.info_signature, details.signature))
    for (record in details.records) {
        lines.add("")
        lines.add(context.getString(record.kind.labelRes()) + if (record.summary.isEmpty()) "" else ": " + record.summary)
        if (record.payloadHex.isNotEmpty()) lines.add(line(R.string.record_payload_hex, record.payloadHex))
    }
    return lines.joinToString("\n")
}
