// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.nfc

import dev.anmitali.nfcutils.ndef.NdefRecordData

sealed interface TagOperation {
    class Write(val records: List<NdefRecordData>) : TagOperation

    data object Erase : TagOperation

    data object Format : TagOperation

    data object Lock : TagOperation
}

sealed interface TagFailure {
    data object TagLost : TagFailure

    data object NotWritable : TagFailure

    class TooLarge(val needed: Int, val capacity: Int) : TagFailure

    data object UnsupportedTag : TagFailure

    data object NotFormatted : TagFailure

    data object AlreadyFormatted : TagFailure

    data object CannotLock : TagFailure

    data object InvalidMessage : TagFailure

    data object IoError : TagFailure
}

sealed interface OperationStatus {
    data object Idle : OperationStatus

    class Waiting(val operation: TagOperation) : OperationStatus

    class Succeeded(val operation: TagOperation) : OperationStatus

    class Failed(val operation: TagOperation, val failure: TagFailure) : OperationStatus
}

sealed interface ScanOutcome {
    class Read(val details: TagDetails) : ScanOutcome

    class Failed(val failure: TagFailure) : ScanOutcome
}
