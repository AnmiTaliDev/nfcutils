// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.tools

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.anmitali.nfcutils.AppContainer
import dev.anmitali.nfcutils.R
import dev.anmitali.nfcutils.nfc.NfcAvailability
import dev.anmitali.nfcutils.nfc.TagOperation
import dev.anmitali.nfcutils.ui.common.TagOperationDialog

@Composable
fun ToolsScreen(container: AppContainer, modifier: Modifier = Modifier) {
    val viewModel: ToolsViewModel = viewModel(factory = ToolsViewModel.factory(container))
    val operationStatus by viewModel.operationStatus.collectAsStateWithLifecycle()
    val availability by viewModel.availability.collectAsStateWithLifecycle()
    var confirmLock by rememberSaveable { mutableStateOf(false) }
    val enabled = availability == NfcAvailability.Enabled

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ToolCard(R.string.tool_erase_title, R.string.tool_erase_body, R.string.tool_erase_action, enabled) {
            viewModel.start(TagOperation.Erase)
        }
        ToolCard(R.string.tool_format_title, R.string.tool_format_body, R.string.tool_format_action, enabled) {
            viewModel.start(TagOperation.Format)
        }
        ToolCard(R.string.tool_lock_title, R.string.tool_lock_body, R.string.tool_lock_action, enabled, destructive = true) {
            confirmLock = true
        }
    }

    TagOperationDialog(operationStatus, onRetry = viewModel::start, onClose = viewModel::closeOperation)

    if (confirmLock) {
        AlertDialog(
            onDismissRequest = { confirmLock = false },
            title = { Text(stringResource(R.string.lock_confirm_title)) },
            text = { Text(stringResource(R.string.lock_confirm_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmLock = false
                        viewModel.start(TagOperation.Lock)
                    },
                ) {
                    Text(stringResource(R.string.lock_confirm_action))
                }
            },
            dismissButton = { TextButton(onClick = { confirmLock = false }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
}

@Composable
private fun ToolCard(
    @StringRes title: Int,
    @StringRes body: Int,
    @StringRes action: Int,
    enabled: Boolean,
    destructive: Boolean = false,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = stringResource(title), style = MaterialTheme.typography.titleLarge)
            Text(
                text = stringResource(body),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
            Button(
                onClick = onClick,
                enabled = enabled,
                colors = if (destructive) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    )
                } else {
                    ButtonDefaults.buttonColors()
                },
            ) {
                Text(stringResource(action))
            }
        }
    }
}
