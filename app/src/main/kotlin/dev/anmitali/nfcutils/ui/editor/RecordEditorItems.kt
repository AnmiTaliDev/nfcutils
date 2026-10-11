// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.anmitali.nfcutils.R
import dev.anmitali.nfcutils.ndef.DraftKind
import dev.anmitali.nfcutils.ndef.RecordBuild
import dev.anmitali.nfcutils.ui.common.labelRes
import dev.anmitali.nfcutils.ui.common.messageRes

class RecordEditorActions(
    val onAdd: (DraftKind) -> Unit,
    val onRemove: (String) -> Unit,
    val onMove: (String, Int) -> Unit,
    val onFieldChange: (String, String, String) -> Unit,
)

fun LazyListScope.recordEditorItems(
    items: List<DraftItem>,
    builds: List<RecordBuild>,
    showErrors: Boolean,
    actions: RecordEditorActions,
) {
    items(items.size, key = { items[it].key }) { index ->
        val item = items[index]
        val invalid = if (showErrors) builds[index] as? RecordBuild.Invalid else null
        RecordEditorCard(item, index, items.size, invalid, actions)
    }
    item(key = "add-record") { AddRecordRow(actions.onAdd) }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddRecordRow(onAdd: (DraftKind) -> Unit) {
    Column {
        Text(
            text = stringResource(R.string.editor_add_record),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (kind in DraftKind.entries) {
                AssistChip(onClick = { onAdd(kind) }, label = { Text(stringResource(kind.labelRes())) })
            }
        }
    }
}

@Composable
private fun RecordEditorCard(
    item: DraftItem,
    index: Int,
    count: Int,
    invalid: RecordBuild.Invalid?,
    actions: RecordEditorActions,
) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = stringResource(R.string.editor_record_title, index + 1, stringResource(item.draft.kind.labelRes())),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { actions.onMove(item.key, -1) }, enabled = index > 0) {
                    Text(stringResource(R.string.action_move_up))
                }
                TextButton(onClick = { actions.onMove(item.key, 1) }, enabled = index < count - 1) {
                    Text(stringResource(R.string.action_move_down))
                }
            }
            for (spec in fieldsFor(item.draft.kind)) {
                val error = if (invalid != null && invalid.field == spec.key) invalid.error else null
                when (spec) {
                    is FieldSpec.Input -> OutlinedTextField(
                        value = item.draft.field(spec.key),
                        onValueChange = { actions.onFieldChange(item.key, spec.key, it) },
                        label = { Text(stringResource(spec.label)) },
                        isError = error != null,
                        supportingText = error?.let { { Text(stringResource(it.messageRes())) } },
                        singleLine = !spec.multiline,
                        keyboardOptions = KeyboardOptions(keyboardType = spec.keyboardType),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    is FieldSpec.Options -> OptionsField(spec, item.draft.field(spec.key)) {
                        actions.onFieldChange(item.key, spec.key, it)
                    }
                }
            }
            TextButton(onClick = { actions.onRemove(item.key) }) {
                Text(stringResource(R.string.action_remove_record))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OptionsField(spec: FieldSpec.Options, selected: String, onSelect: (String) -> Unit) {
    Column {
        Text(text = stringResource(spec.label), style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (choice in spec.choices) {
                FilterChip(
                    selected = choice.value == selected,
                    onClick = { onSelect(choice.value) },
                    label = { Text(stringResource(choice.label)) },
                )
            }
        }
    }
}
