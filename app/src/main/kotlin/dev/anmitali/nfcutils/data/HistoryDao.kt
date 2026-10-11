// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import dev.anmitali.nfcutils.ndef.entrySummary
import dev.anmitali.nfcutils.nfc.TagDetails
import kotlinx.coroutines.flow.Flow

@Dao
abstract class HistoryDao {
    @Query("SELECT * FROM history_entry ORDER BY scannedAt DESC, id DESC")
    abstract fun observeEntries(): Flow<List<HistoryEntryEntity>>

    @Transaction
    @Query("SELECT * FROM history_entry WHERE id = :id")
    abstract fun observeDetail(id: Long): Flow<HistoryDetail?>

    @Insert
    protected abstract suspend fun insertEntry(entry: HistoryEntryEntity): Long

    @Insert
    protected abstract suspend fun insertTechnologies(technologies: List<HistoryTechnologyEntity>)

    @Insert
    protected abstract suspend fun insertRecords(records: List<HistoryRecordEntity>)

    @Insert
    protected abstract suspend fun insertPages(pages: List<HistoryPageEntity>)

    @Query("DELETE FROM history_entry WHERE id = :id")
    abstract suspend fun delete(id: Long)

    @Query("DELETE FROM history_entry")
    abstract suspend fun deleteAll()

    @Transaction
    open suspend fun insertScan(details: TagDetails): Long {
        val entryId = insertEntry(
            HistoryEntryEntity(
                id = 0,
                scannedAt = details.scannedAt,
                uid = details.uid,
                tagType = details.tagType,
                atqa = details.atqa,
                sak = details.sak,
                maxSize = details.maxSize,
                usedSize = details.usedSize,
                writable = details.writable,
                canMakeReadOnly = details.canMakeReadOnly,
                formatable = details.formatable,
                unsupported = details.unsupported,
                summary = entrySummary(details.records),
            ),
        )
        insertTechnologies(details.technologies.map { HistoryTechnologyEntity(0, entryId, it) })
        insertRecords(
            details.records.mapIndexed { index, record ->
                HistoryRecordEntity(0, entryId, index, record.kind, record.summary, record.detail, record.payloadHex)
            },
        )
        insertPages(details.pages.map { HistoryPageEntity(0, entryId, it.index, it.hex) })
        return entryId
    }
}
