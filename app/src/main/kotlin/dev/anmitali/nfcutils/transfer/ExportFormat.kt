// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.transfer

import dev.anmitali.nfcutils.ndef.percentDecode
import dev.anmitali.nfcutils.ndef.percentEncode

const val EXPORT_HEADER = "NFC-UTILS-EXPORT 1"

class Block(val type: String, val entries: List<Pair<String, String>>) {
    fun first(key: String): String? = entries.firstOrNull { it.first == key }?.second

    fun all(key: String): List<String> = entries.filter { it.first == key }.map { it.second }
}

fun writeBlocks(blocks: List<Block>): String {
    val out = StringBuilder()
    out.append(EXPORT_HEADER).append('\n')
    for (block in blocks) {
        out.append('\n')
        out.append('[').append(block.type).append("]\n")
        for ((key, value) in block.entries) {
            out.append(key).append('=').append(percentEncode(value)).append('\n')
        }
    }
    return out.toString()
}

fun readBlocks(text: String): List<Block>? {
    val lines = text.lines()
    if (lines.firstOrNull()?.trim() != EXPORT_HEADER) return null
    val blocks = mutableListOf<Block>()
    var type: String? = null
    var entries = mutableListOf<Pair<String, String>>()
    fun finish() {
        type?.let { blocks.add(Block(it, entries)) }
        entries = mutableListOf()
    }
    for (raw in lines.drop(1)) {
        val line = raw.trim()
        if (line.isEmpty()) continue
        if (line.startsWith("[") && line.endsWith("]")) {
            finish()
            type = line.substring(1, line.length - 1)
        } else {
            val separator = line.indexOf('=')
            if (separator <= 0 || type == null) return null
            entries.add(line.substring(0, separator) to percentDecode(line.substring(separator + 1)))
        }
    }
    finish()
    return blocks
}
