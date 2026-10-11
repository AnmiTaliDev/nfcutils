// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.anmitali.nfcutils.R
import dev.anmitali.nfcutils.nfc.OperationStatus
import dev.anmitali.nfcutils.nfc.TagOperation

@StringRes
private fun TagOperation.titleRes(): Int = when (this) {
    is TagOperation.Write -> R.string.operation_write_title
    TagOperation.Erase -> R.string.operation_erase_title
    TagOperation.Format -> R.string.operation_format_title
    TagOperation.Lock -> R.string.operation_lock_title
}

@StringRes
private fun TagOperation.successRes(): Int = when (this) {
    is TagOperation.Write -> R.string.operation_write_success
    TagOperation.Erase -> R.string.operation_erase_success
    TagOperation.Format -> R.string.operation_format_success
    TagOperation.Lock -> R.string.operation_lock_success
}

@Composable
fun TagOperationDialog(
    status: OperationStatus,
    onRetry: (TagOperation) -> Unit,
    onClose: () -> Unit,
) {
    when (status) {
        OperationStatus.Idle -> Unit
        is OperationStatus.Waiting -> AlertDialog(
            onDismissRequest = onClose,
            title = { Text(stringResource(status.operation.titleRes())) },
            text = {
                Column {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(R.string.operation_hold_tag),
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.action_cancel)) } },
        )
        is OperationStatus.Succeeded -> AlertDialog(
            onDismissRequest = onClose,
            title = { Text(stringResource(status.operation.titleRes())) },
            text = { Text(stringResource(status.operation.successRes())) },
            confirmButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.action_close)) } },
        )
        is OperationStatus.Failed -> AlertDialog(
            onDismissRequest = onClose,
            title = { Text(stringResource(R.string.operation_failed_title)) },
            text = { Text(status.failure.message()) },
            confirmButton = {
                TextButton(onClick = { onRetry(status.operation) }) { Text(stringResource(R.string.action_try_again)) }
            },
            dismissButton = { TextButton(onClick = onClose) { Text(stringResource(R.string.action_close)) } },
        )
    }
}
