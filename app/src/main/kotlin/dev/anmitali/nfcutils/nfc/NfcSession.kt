// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.nfc

import android.nfc.Tag
import dev.anmitali.nfcutils.data.HistoryDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class NfcSession(
    private val reader: TagReader,
    private val writer: TagWriter,
    private val historyDao: HistoryDao,
    private val scope: CoroutineScope,
) {
    private val mutableStatus = MutableStateFlow<OperationStatus>(OperationStatus.Idle)
    val status: StateFlow<OperationStatus> = mutableStatus.asStateFlow()

    private val mutableLastScan = MutableStateFlow<ScanOutcome?>(null)
    val lastScan: StateFlow<ScanOutcome?> = mutableLastScan.asStateFlow()

    private val mutableScanEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val scanEvents: SharedFlow<Unit> = mutableScanEvents

    private val tagMutex = Mutex()

    fun start(operation: TagOperation) {
        mutableStatus.value = OperationStatus.Waiting(operation)
    }

    fun reset() {
        mutableStatus.value = OperationStatus.Idle
    }

    fun onTagDiscovered(tag: Tag) {
        scope.launch(Dispatchers.IO) {
            tagMutex.withLock {
                when (val current = mutableStatus.value) {
                    is OperationStatus.Waiting -> execute(current, tag)
                    OperationStatus.Idle -> scan(tag)
                    is OperationStatus.Succeeded, is OperationStatus.Failed -> Unit
                }
            }
        }
    }

    private fun execute(current: OperationStatus.Waiting, tag: Tag) {
        val next = when (val result = writer.run(current.operation, tag)) {
            TagWriter.Result.Success -> OperationStatus.Succeeded(current.operation)
            is TagWriter.Result.Failed -> OperationStatus.Failed(current.operation, result.failure)
        }
        mutableStatus.compareAndSet(current, next)
    }

    private suspend fun scan(tag: Tag) {
        when (val result = reader.read(tag)) {
            is TagReader.Result.Success -> {
                historyDao.insertScan(result.details)
                mutableLastScan.value = ScanOutcome.Read(result.details, result.records)
            }
            is TagReader.Result.Failed -> mutableLastScan.value = ScanOutcome.Failed(result.failure)
        }
        mutableScanEvents.tryEmit(Unit)
    }
}
