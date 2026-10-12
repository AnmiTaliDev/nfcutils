// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.anmitali.nfcutils.AppContainer
import dev.anmitali.nfcutils.ndef.RecordDraft
import dev.anmitali.nfcutils.nfc.NfcSession
import dev.anmitali.nfcutils.nfc.ScanOutcome
import dev.anmitali.nfcutils.ui.editor.DraftHandoff
import kotlinx.coroutines.flow.StateFlow

class ScanViewModel(session: NfcSession, private val handoff: DraftHandoff) : ViewModel() {
    val lastScan: StateFlow<ScanOutcome?> = session.lastScan

    fun openInEditor(drafts: List<RecordDraft>) {
        handoff.pending.value = drafts
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { ScanViewModel(container.session, container.draftHandoff) }
        }
    }
}
