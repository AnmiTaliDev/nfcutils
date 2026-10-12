// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils

import android.content.Context
import android.nfc.NfcAdapter
import dev.anmitali.nfcutils.data.NfcUtilsDatabase
import dev.anmitali.nfcutils.nfc.NfcSession
import dev.anmitali.nfcutils.nfc.NfcStatus
import dev.anmitali.nfcutils.nfc.ReaderModeController
import dev.anmitali.nfcutils.nfc.TagReader
import dev.anmitali.nfcutils.nfc.TagWriter
import dev.anmitali.nfcutils.settings.SettingsStore
import dev.anmitali.nfcutils.ui.editor.DraftHandoff
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val database = NfcUtilsDatabase.create(context)

    val historyDao = database.historyDao()
    val savedSetDao = database.savedSetDao()
    val settingsStore = SettingsStore(context)
    val nfcStatus = NfcStatus(context, NfcAdapter.getDefaultAdapter(context))
    val session = NfcSession(TagReader(), TagWriter(), historyDao, applicationScope)
    val readerMode = ReaderModeController(nfcStatus, session)
    val draftHandoff = DraftHandoff()
}
