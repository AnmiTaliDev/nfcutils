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
import dev.anmitali.nfcutils.ndef.DraftKind
import dev.anmitali.nfcutils.ui.editor.DraftItem
import dev.anmitali.nfcutils.ui.editor.allValid
import dev.anmitali.nfcutils.ui.editor.buildAll
import dev.anmitali.nfcutils.ui.editor.toDraftItems
import dev.anmitali.nfcutils.ui.editor.withField
import dev.anmitali.nfcutils.ui.editor.withMovedDraft
import dev.anmitali.nfcutils.ui.editor.withNewDraft
import dev.anmitali.nfcutils.ui.editor.withoutDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SetEditorState(
    val loaded: Boolean = false,
    val name: String = "",
    val items: List<DraftItem> = emptyList(),
    val showErrors: Boolean = false,
    val saved: Boolean = false,
)

class SetEditorViewModel(private val savedSetDao: SavedSetDao, private val id: Long) : ViewModel() {
    private val mutableState = MutableStateFlow(SetEditorState(loaded = id == 0L))
    val state: StateFlow<SetEditorState> = mutableState.asStateFlow()

    init {
        if (id != 0L) {
            viewModelScope.launch {
                val set = savedSetDao.getSet(id)
                mutableState.value = SetEditorState(
                    loaded = true,
                    name = set?.set?.name.orEmpty(),
                    items = set?.toDrafts().orEmpty().toDraftItems(),
                )
            }
        }
    }

    fun changeName(name: String) = mutableState.update { it.copy(name = name) }

    fun addRecord(kind: DraftKind) = mutableState.update { it.copy(items = it.items.withNewDraft(kind)) }

    fun removeRecord(key: String) = mutableState.update { it.copy(items = it.items.withoutDraft(key)) }

    fun moveRecord(key: String, offset: Int) =
        mutableState.update { it.copy(items = it.items.withMovedDraft(key, offset)) }

    fun changeField(key: String, field: String, value: String) =
        mutableState.update { it.copy(items = it.items.withField(key, field, value)) }

    fun save() {
        val current = mutableState.value
        if (current.name.isBlank() || !current.items.buildAll().allValid()) {
            mutableState.update { it.copy(showErrors = true) }
            return
        }
        viewModelScope.launch {
            savedSetDao.save(id, current.name.trim(), current.items.map { it.draft }, System.currentTimeMillis())
            mutableState.update { it.copy(saved = true) }
        }
    }

    companion object {
        fun factory(container: AppContainer, id: Long) = viewModelFactory {
            initializer { SetEditorViewModel(container.savedSetDao, id) }
        }
    }
}
