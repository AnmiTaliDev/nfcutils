// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.write

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.anmitali.nfcutils.AppContainer
import dev.anmitali.nfcutils.R
import dev.anmitali.nfcutils.data.SavedSetDao
import dev.anmitali.nfcutils.ndef.DraftKind
import dev.anmitali.nfcutils.nfc.NfcAvailability
import dev.anmitali.nfcutils.nfc.NfcSession
import dev.anmitali.nfcutils.nfc.NfcStatus
import dev.anmitali.nfcutils.nfc.OperationStatus
import dev.anmitali.nfcutils.nfc.TagOperation
import dev.anmitali.nfcutils.ui.editor.DraftItem
import dev.anmitali.nfcutils.ui.editor.allValid
import dev.anmitali.nfcutils.ui.editor.buildAll
import dev.anmitali.nfcutils.ui.editor.records
import dev.anmitali.nfcutils.ui.editor.withField
import dev.anmitali.nfcutils.ui.editor.withMovedDraft
import dev.anmitali.nfcutils.ui.editor.withNewDraft
import dev.anmitali.nfcutils.ui.editor.withoutDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WriteUiState(
    val items: List<DraftItem> = emptyList(),
    val showErrors: Boolean = false,
    val saveDialogOpen: Boolean = false,
    val saveNameError: Boolean = false,
    @StringRes val message: Int? = null,
)

class WriteViewModel(
    private val session: NfcSession,
    private val savedSetDao: SavedSetDao,
    nfcStatus: NfcStatus,
) : ViewModel() {
    private val mutableState = MutableStateFlow(WriteUiState())
    val state: StateFlow<WriteUiState> = mutableState.asStateFlow()
    val operationStatus: StateFlow<OperationStatus> = session.status
    val availability: StateFlow<NfcAvailability> = nfcStatus.state

    fun addRecord(kind: DraftKind) = mutableState.update { it.copy(items = it.items.withNewDraft(kind)) }

    fun removeRecord(key: String) = mutableState.update { it.copy(items = it.items.withoutDraft(key)) }

    fun moveRecord(key: String, offset: Int) = mutableState.update { it.copy(items = it.items.withMovedDraft(key, offset)) }

    fun changeField(key: String, field: String, value: String) =
        mutableState.update { it.copy(items = it.items.withField(key, field, value)) }

    fun startWrite() {
        val builds = mutableState.value.items.buildAll()
        if (!builds.allValid()) {
            mutableState.update { it.copy(showErrors = true) }
            return
        }
        session.start(TagOperation.Write(builds.records()))
    }

    fun retry(operation: TagOperation) = session.start(operation)

    fun closeOperation() = session.reset()

    fun openSaveDialog() {
        val builds = mutableState.value.items.buildAll()
        if (!builds.allValid()) {
            mutableState.update { it.copy(showErrors = true) }
            return
        }
        mutableState.update { it.copy(saveDialogOpen = true, saveNameError = false) }
    }

    fun closeSaveDialog() = mutableState.update { it.copy(saveDialogOpen = false) }

    fun saveSet(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            mutableState.update { it.copy(saveNameError = true) }
            return
        }
        val drafts = mutableState.value.items.map { it.draft }
        viewModelScope.launch {
            savedSetDao.save(0, trimmed, drafts, System.currentTimeMillis())
            mutableState.update { it.copy(saveDialogOpen = false, message = R.string.message_set_saved) }
        }
    }

    fun messageShown() = mutableState.update { it.copy(message = null) }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { WriteViewModel(container.session, container.savedSetDao, container.nfcStatus) }
        }
    }
}
