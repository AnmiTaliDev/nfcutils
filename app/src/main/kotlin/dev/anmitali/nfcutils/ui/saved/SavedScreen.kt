// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import dev.anmitali.nfcutils.ui.common.TagOperationDialog
import dev.anmitali.nfcutils.ui.common.labelRes

@Composable
fun SavedScreen(container: AppContainer, onEdit: (Long) -> Unit, modifier: Modifier = Modifier) {
    val viewModel: SavedViewModel = viewModel(factory = SavedViewModel.factory(container))
    val sets by viewModel.sets.collectAsStateWithLifecycle()
    val operationStatus by viewModel.operationStatus.collectAsStateWithLifecycle()
    val availability by viewModel.availability.collectAsStateWithLifecycle()
    var deleteTarget by rememberSaveable { mutableStateOf<Long?>(null) }
    val list = sets ?: return

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "new") {
            Button(onClick = { onEdit(0L) }) { Text(stringResource(R.string.saved_new)) }
        }
        if (list.isEmpty()) {
            item(key = "empty") {
                Text(text = stringResource(R.string.saved_empty), style = MaterialTheme.typography.bodyLarge)
            }
        }
        items(list, key = { it.id }) { item ->
            SavedSetCard(
                item = item,
                writeEnabled = availability == NfcAvailability.Enabled,
                onWrite = { viewModel.write(item) },
                onEdit = { onEdit(item.id) },
                onDuplicate = { name -> viewModel.duplicate(item, name) },
                onDelete = { deleteTarget = item.id },
            )
        }
    }

    TagOperationDialog(operationStatus, onRetry = viewModel::retry, onClose = viewModel::closeOperation)

    val target = deleteTarget
    if (target != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.saved_delete_title)) },
            text = { Text(stringResource(R.string.saved_delete_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(target)
                        deleteTarget = null
                    },
                ) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SavedSetCard(
    item: SavedSetItem,
    writeEnabled: Boolean,
    onWrite: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: (String) -> Unit,
    onDelete: () -> Unit,
) {
    val copyName = stringResource(R.string.saved_copy_name, item.name)
    val kinds = item.drafts.map { stringResource(it.kind.labelRes()) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = item.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = kinds.joinToString(", "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                Button(onClick = onWrite, enabled = writeEnabled) { Text(stringResource(R.string.action_write)) }
                OutlinedButton(onClick = onEdit) { Text(stringResource(R.string.action_edit)) }
                OutlinedButton(onClick = { onDuplicate(copyName) }) { Text(stringResource(R.string.saved_duplicate)) }
                TextButton(onClick = onDelete) { Text(stringResource(R.string.action_delete)) }
            }
        }
    }
}
