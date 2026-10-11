// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.data

import dev.anmitali.nfcutils.ndef.RecordInfo
import dev.anmitali.nfcutils.nfc.TagDetails
import dev.anmitali.nfcutils.nfc.TagPage

fun HistoryDetail.toTagDetails(): TagDetails = TagDetails(
    scannedAt = entry.scannedAt,
    uid = entry.uid,
    tagType = entry.tagType,
    technologies = technologies.sortedBy { it.id }.map { it.name },
    atqa = entry.atqa,
    sak = entry.sak,
    maxSize = entry.maxSize,
    usedSize = entry.usedSize,
    writable = entry.writable,
    canMakeReadOnly = entry.canMakeReadOnly,
    formatable = entry.formatable,
    unsupported = entry.unsupported,
    records = records.sortedBy { it.position }.map { RecordInfo(it.kind, it.summary, it.detail, it.payloadHex) },
    pages = pages.sortedBy { it.pageIndex }.map { TagPage(it.pageIndex, it.hex) },
)
