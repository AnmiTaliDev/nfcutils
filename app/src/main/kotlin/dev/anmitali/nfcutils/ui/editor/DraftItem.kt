// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.editor

import dev.anmitali.nfcutils.ndef.DraftKind
import dev.anmitali.nfcutils.ndef.NdefCodec
import dev.anmitali.nfcutils.ndef.RecordBuild
import dev.anmitali.nfcutils.ndef.RecordDraft
import dev.anmitali.nfcutils.ndef.buildRecord
import dev.anmitali.nfcutils.ndef.emptyDraft
import java.util.UUID

class DraftItem(val key: String, val draft: RecordDraft)

fun List<DraftItem>.withNewDraft(kind: DraftKind): List<DraftItem> =
    this + DraftItem(UUID.randomUUID().toString(), emptyDraft(kind))

fun List<RecordDraft>.toDraftItems(): List<DraftItem> =
    map { DraftItem(UUID.randomUUID().toString(), it) }

fun List<DraftItem>.withoutDraft(key: String): List<DraftItem> = filter { it.key != key }

fun List<DraftItem>.withMovedDraft(key: String, offset: Int): List<DraftItem> {
    val from = indexOfFirst { it.key == key }
    val to = from + offset
    if (from < 0 || to !in indices) return this
    val result = toMutableList()
    result.add(to, result.removeAt(from))
    return result
}

fun List<DraftItem>.withField(key: String, field: String, value: String): List<DraftItem> =
    map { if (it.key == key) DraftItem(it.key, it.draft.withField(field, value)) else it }

fun List<DraftItem>.buildAll(): List<RecordBuild> = map { buildRecord(it.draft) }

fun List<RecordBuild>.allValid(): Boolean = isNotEmpty() && all { it is RecordBuild.Ok }

fun List<RecordBuild>.messageSize(): Int? =
    if (allValid()) NdefCodec.size(filterIsInstance<RecordBuild.Ok>().map { it.record }) else null

fun List<RecordBuild>.records() = filterIsInstance<RecordBuild.Ok>().map { it.record }
