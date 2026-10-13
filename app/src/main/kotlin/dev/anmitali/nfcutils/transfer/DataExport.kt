// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.transfer

import dev.anmitali.nfcutils.nfc.TagDetails

class ImportedData(val sets: List<ExportedSet>, val scans: List<TagDetails>)

fun buildExport(sets: List<ExportedSet>, scans: List<TagDetails>): String =
    writeBlocks(setsToBlocks(sets) + scansToBlocks(scans))

fun parseExport(text: String): ImportedData? {
    val blocks = readBlocks(text) ?: return null
    return ImportedData(blocksToSets(blocks), blocksToScans(blocks))
}
