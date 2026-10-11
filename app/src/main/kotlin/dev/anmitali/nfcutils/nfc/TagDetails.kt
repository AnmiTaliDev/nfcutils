// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.nfc

import dev.anmitali.nfcutils.ndef.RecordInfo

class TagPage(val index: Int, val hex: String)

class TagDetails(
    val scannedAt: Long,
    val uid: String,
    val tagType: TagType,
    val technologies: List<String>,
    val atqa: String?,
    val sak: Int?,
    val maxSize: Int?,
    val usedSize: Int?,
    val writable: Boolean?,
    val canMakeReadOnly: Boolean?,
    val formatable: Boolean,
    val unsupported: Boolean,
    val records: List<RecordInfo>,
    val pages: List<TagPage>,
)
