// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import dev.anmitali.nfcutils.ndef.DraftKind

@Entity(tableName = "saved_set")
class SavedSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val name: String,
    val updatedAt: Long,
)

@Entity(
    tableName = "saved_record",
    foreignKeys = [
        ForeignKey(
            entity = SavedSetEntity::class,
            parentColumns = ["id"],
            childColumns = ["setId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("setId")],
)
class SavedRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val setId: Long,
    val position: Int,
    val kind: DraftKind,
)

@Entity(
    tableName = "saved_field",
    foreignKeys = [
        ForeignKey(
            entity = SavedRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["recordId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("recordId")],
)
class SavedFieldEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val recordId: Long,
    val key: String,
    val value: String,
)

class SavedRecordWithFields(
    @Embedded val record: SavedRecordEntity,
    @Relation(parentColumn = "id", entityColumn = "recordId") val fields: List<SavedFieldEntity>,
)

class SavedSetWithRecords(
    @Embedded val set: SavedSetEntity,
    @Relation(entity = SavedRecordEntity::class, parentColumn = "id", entityColumn = "setId")
    val records: List<SavedRecordWithFields>,
)
