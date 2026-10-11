// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.anmitali.nfcutils.AppContainer
import dev.anmitali.nfcutils.data.SavedSetDao
import dev.anmitali.nfcutils.data.toDrafts
import dev.anmitali.nfcutils.ndef.RecordBuild
import dev.anmitali.nfcutils.ndef.RecordDraft
import dev.anmitali.nfcutils.ndef.buildRecord
import dev.anmitali.nfcutils.nfc.NfcAvailability
import dev.anmitali.nfcutils.nfc.NfcSession
import dev.anmitali.nfcutils.nfc.NfcStatus
import dev.anmitali.nfcutils.nfc.OperationStatus
import dev.anmitali.nfcutils.nfc.TagOperation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedSetItem(val id: Long, val name: String, val drafts: List<RecordDraft>)

class SavedViewModel(
    private val session: NfcSession,
    private val savedSetDao: SavedSetDao,
    nfcStatus: NfcStatus,
) : ViewModel() {
    val sets: StateFlow<List<SavedSetItem>?> = savedSetDao.observeSets()
        .map { sets -> sets.map { SavedSetItem(it.set.id, it.set.name, it.toDrafts()) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val operationStatus: StateFlow<OperationStatus> = session.status
    val availability: StateFlow<NfcAvailability> = nfcStatus.state

    fun write(item: SavedSetItem) {
        val builds = item.drafts.map { buildRecord(it) }
        if (builds.isEmpty() || builds.any { it is RecordBuild.Invalid }) return
        session.start(TagOperation.Write(builds.filterIsInstance<RecordBuild.Ok>().map { it.record }))
    }

    fun retry(operation: TagOperation) = session.start(operation)

    fun closeOperation() = session.reset()

    fun delete(id: Long) {
        viewModelScope.launch { savedSetDao.delete(id) }
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { SavedViewModel(container.session, container.savedSetDao, container.nfcStatus) }
        }
    }
}
