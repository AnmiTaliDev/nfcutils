// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val DATABASE_NAME = "migration-test"

private const val INSERT_ENTRY =
    "INSERT INTO history_entry (scannedAt, uid, tagType, formatable, unsupported, summary) " +
        "VALUES (100, '04:A1', 'Ntag215', 0, 0, 'Hello')"

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), NfcUtilsDatabase::class.java)

    @Test
    fun migrate1To2AddsCounterAndSignatureColumns() {
        helper.createDatabase(DATABASE_NAME, 1).apply {
            execSQL(INSERT_ENTRY)
            close()
        }
        val db = helper.runMigrationsAndValidate(DATABASE_NAME, 2, true, Migration1To2)
        db.query("SELECT uid, nfcCounter, signature FROM history_entry").use {
            assertEquals(1, it.count)
            it.moveToFirst()
            assertEquals("04:A1", it.getString(0))
            assertEquals(true, it.isNull(1))
            assertEquals(true, it.isNull(2))
        }
    }

    @Test
    fun migrate2To3KeepsRowsAndDefaultsToNotPinned() {
        helper.createDatabase(DATABASE_NAME, 2).apply {
            execSQL(INSERT_ENTRY)
            close()
        }
        val db = helper.runMigrationsAndValidate(DATABASE_NAME, 3, true, Migration2To3)
        db.query("SELECT uid, summary, pinned FROM history_entry").use {
            assertEquals(1, it.count)
            it.moveToFirst()
            assertEquals("04:A1", it.getString(0))
            assertEquals("Hello", it.getString(1))
            assertEquals(0, it.getInt(2))
        }
    }

    @Test
    fun migrate1To3RunsBothMigrations() {
        helper.createDatabase(DATABASE_NAME, 1).apply {
            execSQL(INSERT_ENTRY)
            close()
        }
        val db = helper.runMigrationsAndValidate(DATABASE_NAME, 3, true, Migration1To2, Migration2To3)
        db.query("SELECT COUNT(*) FROM history_entry WHERE pinned = 0").use {
            it.moveToFirst()
            assertEquals(1, it.getInt(0))
        }
    }
}
