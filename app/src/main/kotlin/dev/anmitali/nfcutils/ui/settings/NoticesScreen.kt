// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import dev.anmitali.nfcutils.R

@Composable
fun NoticesScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val licenseText = remember {
        context.resources.openRawResource(R.raw.license_apache_2_0).bufferedReader().use { it.readText() }
    }
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = stringResource(R.string.notices_intro), style = MaterialTheme.typography.bodyLarge)
        Text(
            text = stringResource(R.string.notices_components_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(text = stringResource(R.string.notices_component_androidx), style = MaterialTheme.typography.bodyMedium)
        Text(text = stringResource(R.string.notices_component_kotlin), style = MaterialTheme.typography.bodyMedium)
        Text(text = stringResource(R.string.notices_component_coroutines), style = MaterialTheme.typography.bodyMedium)
        Text(
            text = stringResource(R.string.notices_license_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp),
        )
        SelectionContainer {
            Text(text = licenseText, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
        }
    }
}
