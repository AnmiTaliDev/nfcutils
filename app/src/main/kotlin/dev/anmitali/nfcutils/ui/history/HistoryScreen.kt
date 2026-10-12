// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.anmitali.nfcutils.AppContainer
import dev.anmitali.nfcutils.R
import dev.anmitali.nfcutils.data.HistoryEntryEntity
import dev.anmitali.nfcutils.ui.common.labelRes
import java.text.DateFormat
import java.util.Date

@Composable
fun HistoryScreen(container: AppContainer, onOpen: (Long) -> Unit, modifier: Modifier = Modifier) {
    val viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.factory(container))
    val state by viewModel.state.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    var confirmClear by rememberSaveable { mutableStateOf(false) }
    val current = state ?: return

    if (!current.hasAny) {
        Column(modifier = modifier.fillMaxSize().padding(24.dp)) {
            Text(text = stringResource(R.string.history_empty), style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "search") {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setQuery,
                label = { Text(stringResource(R.string.history_search)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item(key = "clear") {
            OutlinedButton(onClick = { confirmClear = true }) { Text(stringResource(R.string.history_clear)) }
        }
        if (current.entries.isEmpty()) {
            item(key = "no-matches") {
                Text(text = stringResource(R.string.history_no_matches), style = MaterialTheme.typography.bodyLarge)
            }
        }
        items(current.entries, key = { it.id }) { entry -> HistoryEntryCard(entry, onClick = { onOpen(entry.id) }) }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(stringResource(R.string.history_clear_title)) },
            text = { Text(stringResource(R.string.history_clear_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmClear = false
                        viewModel.clearAll()
                    },
                ) {
                    Text(stringResource(R.string.history_clear_confirm))
                }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }
}

@Composable
private fun HistoryEntryCard(entry: HistoryEntryEntity, onClick: () -> Unit) {
    val time = remember(entry.scannedAt) {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(entry.scannedAt))
    }
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(entry.tagType.labelRes()), style = MaterialTheme.typography.titleMedium)
            Text(
                text = entry.uid,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = entry.summary.ifEmpty { stringResource(R.string.history_no_records) },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
