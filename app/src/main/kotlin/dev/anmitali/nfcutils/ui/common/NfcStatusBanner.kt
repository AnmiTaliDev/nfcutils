// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.common

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.anmitali.nfcutils.R
import dev.anmitali.nfcutils.nfc.NfcAvailability

@Composable
fun NfcStatusBanner(availability: NfcAvailability, modifier: Modifier = Modifier) {
    if (availability == NfcAvailability.Enabled) return
    val context = LocalContext.current
    val title = if (availability == NfcAvailability.Unavailable) R.string.nfc_unavailable_title else R.string.nfc_disabled_title
    val body = if (availability == NfcAvailability.Unavailable) R.string.nfc_unavailable_body else R.string.nfc_disabled_body
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(title), style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(body),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
            if (availability == NfcAvailability.Disabled) {
                Button(
                    onClick = { context.startActivity(Intent(Settings.ACTION_NFC_SETTINGS)) },
                    modifier = Modifier.padding(top = 12.dp),
                ) {
                    Text(stringResource(R.string.nfc_open_settings))
                }
            }
        }
    }
}
