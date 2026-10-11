// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.saved

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.anmitali.nfcutils.AppContainer
import dev.anmitali.nfcutils.R
import dev.anmitali.nfcutils.ui.editor.RecordEditorActions
import dev.anmitali.nfcutils.ui.editor.buildAll
import dev.anmitali.nfcutils.ui.editor.recordEditorItems

@Composable
fun SetEditorScreen(container: AppContainer, id: Long, onClose: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: SetEditorViewModel = viewModel(
        key = "set-editor-$id",
        factory = SetEditorViewModel.factory(container, id),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onClose()
    }

    if (!state.loaded) return
    val builds = state.items.buildAll()
    val actions = RecordEditorActions(
        onAdd = viewModel::addRecord,
        onRemove = viewModel::removeRecord,
        onMove = viewModel::moveRecord,
        onFieldChange = viewModel::changeField,
    )
    val nameError = state.showErrors && state.name.isBlank()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "name") {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::changeName,
                label = { Text(stringResource(R.string.field_set_name)) },
                isError = nameError,
                supportingText = if (nameError) {
                    { Text(stringResource(R.string.field_error_empty)) }
                } else {
                    null
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        recordEditorItems(state.items, builds, state.showErrors, actions)
        if (state.showErrors && state.items.isEmpty()) {
            item(key = "no-records") {
                Text(
                    text = stringResource(R.string.editor_no_records_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        item(key = "save") {
            Button(onClick = viewModel::save) { Text(stringResource(R.string.action_save)) }
        }
    }
}
