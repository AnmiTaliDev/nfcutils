// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.write

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import dev.anmitali.nfcutils.ui.common.TagOperationDialog
import dev.anmitali.nfcutils.ui.editor.RecordEditorActions
import dev.anmitali.nfcutils.ui.editor.buildAll
import dev.anmitali.nfcutils.ui.editor.messageSize
import dev.anmitali.nfcutils.ui.editor.recordEditorItems

@Composable
fun WriteScreen(container: AppContainer, snackbarHostState: SnackbarHostState, modifier: Modifier = Modifier) {
    val viewModel: WriteViewModel = viewModel(factory = WriteViewModel.factory(container))
    val state by viewModel.state.collectAsStateWithLifecycle()
    val operationStatus by viewModel.operationStatus.collectAsStateWithLifecycle()
    val availability by viewModel.availability.collectAsStateWithLifecycle()

    val builds = state.items.buildAll()
    val size = builds.messageSize()
    val actions = RecordEditorActions(
        onAdd = viewModel::addRecord,
        onRemove = viewModel::removeRecord,
        onMove = viewModel::moveRecord,
        onFieldChange = viewModel::changeField,
    )

    val message = state.message?.let { stringResource(it) }
    LaunchedEffect(message) {
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.messageShown()
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        recordEditorItems(state.items, builds, state.showErrors, actions)
        item(key = "summary") {
            Text(
                text = if (size != null) stringResource(R.string.editor_message_size, size) else stringResource(R.string.editor_message_size_unknown),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        item(key = "buttons") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = viewModel::startWrite,
                    enabled = state.items.isNotEmpty() && availability == NfcAvailability.Enabled,
                ) {
                    Text(stringResource(R.string.action_write_to_tag))
                }
                OutlinedButton(onClick = viewModel::openSaveDialog, enabled = state.items.isNotEmpty()) {
                    Text(stringResource(R.string.action_save_as_set))
                }
            }
        }
    }

    TagOperationDialog(operationStatus, onRetry = viewModel::retry, onClose = viewModel::closeOperation)

    if (state.saveDialogOpen) {
        SaveSetDialog(
            nameError = state.saveNameError,
            onSave = viewModel::saveSet,
            onDismiss = viewModel::closeSaveDialog,
        )
    }
}

@Composable
private fun SaveSetDialog(nameError: Boolean, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.save_set_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.field_set_name)) },
                isError = nameError,
                supportingText = if (nameError) {
                    { Text(stringResource(R.string.field_error_empty)) }
                } else {
                    null
                },
                singleLine = true,
            )
        },
        confirmButton = { TextButton(onClick = { onSave(name) }) { Text(stringResource(R.string.action_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}
