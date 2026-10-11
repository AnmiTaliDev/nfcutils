// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import dev.anmitali.nfcutils.ndef.RecordDraft
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SavedSetDao {
    @Transaction
    @Query("SELECT * FROM saved_set ORDER BY updatedAt DESC, id DESC")
    abstract fun observeSets(): Flow<List<SavedSetWithRecords>>

    @Transaction
    @Query("SELECT * FROM saved_set WHERE id = :id")
    abstract suspend fun getSet(id: Long): SavedSetWithRecords?

    @Insert
    protected abstract suspend fun insertSet(set: SavedSetEntity): Long

    @Insert
    protected abstract suspend fun insertRecord(record: SavedRecordEntity): Long

    @Insert
    protected abstract suspend fun insertFields(fields: List<SavedFieldEntity>)

    @Query("UPDATE saved_set SET name = :name, updatedAt = :updatedAt WHERE id = :id")
    protected abstract suspend fun updateSet(id: Long, name: String, updatedAt: Long)

    @Query("DELETE FROM saved_record WHERE setId = :setId")
    protected abstract suspend fun deleteRecords(setId: Long)

    @Query("DELETE FROM saved_set WHERE id = :id")
    abstract suspend fun delete(id: Long)

    @Transaction
    open suspend fun save(id: Long, name: String, drafts: List<RecordDraft>, now: Long): Long {
        val setId = if (id == 0L) {
            insertSet(SavedSetEntity(0, name, now))
        } else {
            updateSet(id, name, now)
            deleteRecords(id)
            id
        }
        for ((position, draft) in drafts.withIndex()) {
            val recordId = insertRecord(SavedRecordEntity(0, setId, position, draft.kind))
            insertFields(draft.fields.map { (key, value) -> SavedFieldEntity(0, recordId, key, value) })
        }
        return setId
    }
}
