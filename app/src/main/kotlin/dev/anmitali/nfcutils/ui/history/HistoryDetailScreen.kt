// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import dev.anmitali.nfcutils.ui.taginfo.tagDetailsItems

@Composable
fun HistoryDetailScreen(container: AppContainer, id: Long, onClose: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: HistoryDetailViewModel = viewModel(
        key = "history-detail-$id",
        factory = HistoryDetailViewModel.factory(container, id),
    )
    val details by viewModel.details.collectAsStateWithLifecycle()
    var deleted by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(deleted) {
        if (deleted) onClose()
    }

    val current = details ?: return
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        tagDetailsItems(current)
        item(key = "delete") {
            OutlinedButton(
                onClick = {
                    viewModel.delete()
                    deleted = true
                },
            ) {
                Text(stringResource(R.string.history_delete_entry))
            }
        }
    }
}
