// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.anmitali.nfcutils.AppContainer
import dev.anmitali.nfcutils.nfc.NfcAvailability
import dev.anmitali.nfcutils.nfc.NfcSession
import dev.anmitali.nfcutils.nfc.NfcStatus
import dev.anmitali.nfcutils.nfc.OperationStatus
import dev.anmitali.nfcutils.nfc.TagOperation
import kotlinx.coroutines.flow.StateFlow

class ToolsViewModel(private val session: NfcSession, nfcStatus: NfcStatus) : ViewModel() {
    val operationStatus: StateFlow<OperationStatus> = session.status
    val availability: StateFlow<NfcAvailability> = nfcStatus.state

    fun start(operation: TagOperation) = session.start(operation)

    fun closeOperation() = session.reset()

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { ToolsViewModel(container.session, container.nfcStatus) }
        }
    }
}
