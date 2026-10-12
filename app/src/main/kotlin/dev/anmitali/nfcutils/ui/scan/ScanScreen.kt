// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.scan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.anmitali.nfcutils.AppContainer
import dev.anmitali.nfcutils.R
import dev.anmitali.nfcutils.ndef.recordsToDrafts
import dev.anmitali.nfcutils.nfc.ScanOutcome
import dev.anmitali.nfcutils.ui.common.message
import dev.anmitali.nfcutils.ui.taginfo.tagDetailsItems

@Composable
fun ScanScreen(container: AppContainer, onOpenEditor: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: ScanViewModel = viewModel(factory = ScanViewModel.factory(container))
    val outcome by viewModel.lastScan.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (val current = outcome) {
            null -> item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = stringResource(R.string.scan_hint_title), style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = stringResource(R.string.scan_hint_body),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
            is ScanOutcome.Failed -> item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = stringResource(R.string.scan_failed_title), style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = current.failure.message(),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
            is ScanOutcome.Read -> {
                tagDetailsItems(current.details)
                val drafts = recordsToDrafts(current.records)
                if (drafts.isNotEmpty()) {
                    item(key = "open-in-editor") {
                        Button(
                            onClick = {
                                viewModel.openInEditor(drafts)
                                onOpenEditor()
                            },
                        ) {
                            Text(stringResource(R.string.scan_open_in_editor))
                        }
                    }
                }
            }
        }
    }
}
