// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.transfer

import dev.anmitali.nfcutils.ndef.DraftKind
import dev.anmitali.nfcutils.ndef.RecordDraft

private const val SET = "set"
private const val RECORD = "record"
private const val FIELD_PREFIX = "f."

class ExportedSet(val name: String, val drafts: List<RecordDraft>)

fun setsToBlocks(sets: List<ExportedSet>): List<Block> = sets.flatMap { set ->
    listOf(Block(SET, listOf("name" to set.name))) + set.drafts.map { draft ->
        Block(
            RECORD,
            listOf("kind" to draft.kind.name) +
                draft.fields.filterValues { it.isNotEmpty() }.map { (key, value) -> (FIELD_PREFIX + key) to value },
        )
    }
}

fun blocksToSets(blocks: List<Block>): List<ExportedSet> {
    val sets = mutableListOf<ExportedSet>()
    var name: String? = null
    var drafts = mutableListOf<RecordDraft>()
    fun finish() {
        name?.let { sets.add(ExportedSet(it, drafts)) }
        drafts = mutableListOf()
    }
    for (block in blocks) {
        when (block.type) {
            SET -> {
                finish()
                name = block.first("name") ?: ""
            }
            RECORD -> {
                val kind = DraftKind.entries.firstOrNull { it.name == block.first("kind") }
                if (name != null && kind != null) {
                    val fields = block.entries
                        .filter { it.first.startsWith(FIELD_PREFIX) }
                        .associate { it.first.removePrefix(FIELD_PREFIX) to it.second }
                    drafts.add(RecordDraft(kind, fields))
                }
            }
        }
    }
    finish()
    return sets.filter { it.name.isNotBlank() && it.drafts.isNotEmpty() }
}
