// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.taginfo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.anmitali.nfcutils.R
import dev.anmitali.nfcutils.nfc.ChipFeature
import dev.anmitali.nfcutils.nfc.ChipSpec

@Composable
fun ChipSpecCard(spec: ChipSpec) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.section_chip_spec),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp),
            )
            InfoRow(R.string.chip_memory_total, stringResource(R.string.chip_memory_total_value, spec.totalBytes, spec.pages))
            InfoRow(R.string.chip_memory_user, stringResource(R.string.value_bytes, spec.userBytes))
            if (spec.features.isNotEmpty()) {
                val names = spec.features.map { stringResource(it.labelRes()) }
                InfoRow(R.string.chip_features, names.joinToString("\n"))
            }
        }
    }
}

private fun ChipFeature.labelRes(): Int = when (this) {
    ChipFeature.Password -> R.string.chip_feature_password
    ChipFeature.Counter -> R.string.chip_feature_counter
    ChipFeature.OriginalitySignature -> R.string.chip_feature_signature
    ChipFeature.TripleDes -> R.string.chip_feature_triple_des
}
