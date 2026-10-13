// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import dev.anmitali.nfcutils.ndef.ParsedKind
import dev.anmitali.nfcutils.nfc.TagType

@Entity(tableName = "history_entry")
class HistoryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val scannedAt: Long,
    val uid: String,
    val tagType: TagType,
    val atqa: String?,
    val sak: Int?,
    val maxSize: Int?,
    val usedSize: Int?,
    val writable: Boolean?,
    val canMakeReadOnly: Boolean?,
    val formatable: Boolean,
    val unsupported: Boolean,
    val summary: String,
    val nfcCounter: Int?,
    val signature: String?,
)

@Entity(
    tableName = "history_technology",
    foreignKeys = [
        ForeignKey(
            entity = HistoryEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("entryId")],
)
class HistoryTechnologyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val entryId: Long,
    val name: String,
)

@Entity(
    tableName = "history_record",
    foreignKeys = [
        ForeignKey(
            entity = HistoryEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("entryId")],
)
class HistoryRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val entryId: Long,
    val position: Int,
    val kind: ParsedKind,
    val summary: String,
    val detail: String,
    val payloadHex: String,
)

@Entity(
    tableName = "history_page",
    foreignKeys = [
        ForeignKey(
            entity = HistoryEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("entryId")],
)
class HistoryPageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val entryId: Long,
    val pageIndex: Int,
    val hex: String,
)

class HistoryDetail(
    @Embedded val entry: HistoryEntryEntity,
    @Relation(parentColumn = "id", entityColumn = "entryId") val technologies: List<HistoryTechnologyEntity>,
    @Relation(parentColumn = "id", entityColumn = "entryId") val records: List<HistoryRecordEntity>,
    @Relation(parentColumn = "id", entityColumn = "entryId") val pages: List<HistoryPageEntity>,
)
