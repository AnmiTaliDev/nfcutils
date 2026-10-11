// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.anmitali.nfcutils.AppContainer
import dev.anmitali.nfcutils.data.HistoryDao
import dev.anmitali.nfcutils.data.toTagDetails
import dev.anmitali.nfcutils.nfc.TagDetails
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryDetailViewModel(private val historyDao: HistoryDao, private val id: Long) : ViewModel() {
    val details: StateFlow<TagDetails?> = historyDao.observeDetail(id)
        .map { it?.toTagDetails() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun delete() {
        viewModelScope.launch { historyDao.delete(id) }
    }

    companion object {
        fun factory(container: AppContainer, id: Long) = viewModelFactory {
            initializer { HistoryDetailViewModel(container.historyDao, id) }
        }
    }
}
