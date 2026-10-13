// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        HistoryEntryEntity::class,
        HistoryTechnologyEntity::class,
        HistoryRecordEntity::class,
        HistoryPageEntity::class,
        SavedSetEntity::class,
        SavedRecordEntity::class,
        SavedFieldEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class NfcUtilsDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    abstract fun savedSetDao(): SavedSetDao

    companion object {
        fun create(context: Context): NfcUtilsDatabase =
            Room.databaseBuilder(context, NfcUtilsDatabase::class.java, "nfcutils.db")
                .addMigrations(Migration1To2)
                .build()
    }
}
