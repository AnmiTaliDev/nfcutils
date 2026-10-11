// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.data

import dev.anmitali.nfcutils.ndef.RecordDraft

fun SavedSetWithRecords.toDrafts(): List<RecordDraft> =
    records.sortedBy { it.record.position }.map { saved ->
        RecordDraft(saved.record.kind, saved.fields.associate { it.key to it.value })
    }
