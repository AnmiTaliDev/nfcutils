// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.transfer

import dev.anmitali.nfcutils.ndef.ParsedKind
import dev.anmitali.nfcutils.ndef.RecordInfo
import dev.anmitali.nfcutils.ndef.percentDecode
import dev.anmitali.nfcutils.ndef.percentEncode
import dev.anmitali.nfcutils.nfc.TagDetails
import dev.anmitali.nfcutils.nfc.TagPage
import dev.anmitali.nfcutils.nfc.TagType

private const val SCAN = "scan"
private const val SEPARATOR = "|"

fun scansToBlocks(scans: List<TagDetails>): List<Block> = scans.map { scan ->
    val entries = mutableListOf<Pair<String, String>>()
    entries.add("scannedAt" to scan.scannedAt.toString())
    entries.add("uid" to scan.uid)
    entries.add("tagType" to scan.tagType.name)
    scan.atqa?.let { entries.add("atqa" to it) }
    scan.sak?.let { entries.add("sak" to it.toString()) }
    scan.maxSize?.let { entries.add("maxSize" to it.toString()) }
    scan.usedSize?.let { entries.add("usedSize" to it.toString()) }
    scan.writable?.let { entries.add("writable" to it.toString()) }
    scan.canMakeReadOnly?.let { entries.add("canMakeReadOnly" to it.toString()) }
    entries.add("formatable" to scan.formatable.toString())
    entries.add("unsupported" to scan.unsupported.toString())
    scan.nfcCounter?.let { entries.add("nfcCounter" to it.toString()) }
    scan.signature?.let { entries.add("signature" to it) }
    scan.technologies.forEach { entries.add("tech" to it) }
    scan.records.forEach { record ->
        val parts = listOf(record.kind.name, record.summary, record.detail, record.payloadHex)
        entries.add("record" to parts.joinToString(SEPARATOR) { percentEncode(it) })
    }
    scan.pages.forEach { entries.add("page" to "${it.index}:${it.hex}") }
    Block(SCAN, entries)
}

private fun parseRecord(value: String): RecordInfo? {
    val parts = value.split(SEPARATOR)
    if (parts.size != 4) return null
    val kind = ParsedKind.entries.firstOrNull { it.name == percentDecode(parts[0]) } ?: ParsedKind.Unknown
    return RecordInfo(kind, percentDecode(parts[1]), percentDecode(parts[2]), percentDecode(parts[3]))
}

private fun parsePage(value: String): TagPage? {
    val separator = value.indexOf(':')
    val index = value.substring(0, maxOf(separator, 0)).toIntOrNull() ?: return null
    return TagPage(index, value.substring(separator + 1))
}

fun blocksToScans(blocks: List<Block>): List<TagDetails> = blocks.filter { it.type == SCAN }.mapNotNull { block ->
    val scannedAt = block.first("scannedAt")?.toLongOrNull() ?: return@mapNotNull null
    val uid = block.first("uid")?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
    TagDetails(
        scannedAt = scannedAt,
        uid = uid,
        tagType = TagType.entries.firstOrNull { it.name == block.first("tagType") } ?: TagType.Unknown,
        technologies = block.all("tech"),
        atqa = block.first("atqa"),
        sak = block.first("sak")?.toIntOrNull(),
        maxSize = block.first("maxSize")?.toIntOrNull(),
        usedSize = block.first("usedSize")?.toIntOrNull(),
        writable = block.first("writable")?.toBooleanStrictOrNull(),
        canMakeReadOnly = block.first("canMakeReadOnly")?.toBooleanStrictOrNull(),
        formatable = block.first("formatable")?.toBooleanStrictOrNull() ?: false,
        unsupported = block.first("unsupported")?.toBooleanStrictOrNull() ?: false,
        records = block.all("record").mapNotNull { parseRecord(it) },
        pages = block.all("page").mapNotNull { parsePage(it) },
        nfcCounter = block.first("nfcCounter")?.toIntOrNull(),
        signature = block.first("signature"),
    )
}
