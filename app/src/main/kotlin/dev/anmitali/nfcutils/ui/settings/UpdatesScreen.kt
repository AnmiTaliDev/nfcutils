// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.anmitali.nfcutils.BuildConfig
import dev.anmitali.nfcutils.R
import dev.anmitali.nfcutils.update.UpdateManager
import dev.anmitali.nfcutils.update.UpdateState

@Composable
fun UpdatesScreen(manager: UpdateManager, modifier: Modifier = Modifier) {
    val state by manager.state.collectAsStateWithLifecycle()
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.updates_current_version, BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.titleMedium,
        )
        when (val current = state) {
            UpdateState.Checking -> {
                Text(stringResource(R.string.updates_checking))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            UpdateState.UpToDate -> Text(stringResource(R.string.updates_up_to_date))
            UpdateState.CheckFailed -> Text(stringResource(R.string.updates_check_failed))
            is UpdateState.Available -> Text(stringResource(R.string.updates_available, current.release.tag))
            else -> Unit
        }
        Button(onClick = manager::check, enabled = state !is UpdateState.Checking) {
            Text(stringResource(R.string.updates_check))
        }
    }
}
