// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.anmitali.nfcutils.AppContainer
import dev.anmitali.nfcutils.data.HistoryDao
import dev.anmitali.nfcutils.data.HistoryEntryEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(private val historyDao: HistoryDao) : ViewModel() {
    val entries: StateFlow<List<HistoryEntryEntity>?> = historyDao.observeEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun clearAll() {
        viewModelScope.launch { historyDao.deleteAll() }
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { HistoryViewModel(container.historyDao) }
        }
    }
}
