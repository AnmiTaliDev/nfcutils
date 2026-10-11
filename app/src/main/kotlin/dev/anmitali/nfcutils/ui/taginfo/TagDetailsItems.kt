// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.taginfo

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import dev.anmitali.nfcutils.R
import dev.anmitali.nfcutils.ndef.RecordInfo
import dev.anmitali.nfcutils.nfc.TagDetails
import dev.anmitali.nfcutils.nfc.TagPage
import dev.anmitali.nfcutils.ui.common.labelRes
import java.text.DateFormat
import java.util.Date

fun LazyListScope.tagDetailsItems(details: TagDetails) {
    item(key = "tag") { TagInfoCard(details) }
    if (details.unsupported) {
        item(key = "unsupported") { UnsupportedCard() }
    } else {
        item(key = "records-title") { SectionTitle(R.string.section_records) }
        if (details.records.isEmpty()) {
            item(key = "records-empty") {
                Text(
                    text = stringResource(R.string.records_none),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
        items(details.records.size, key = { "record-$it" }) { RecordCard(details.records[it]) }
    }
    if (details.pages.isNotEmpty()) {
        item(key = "pages-title") { SectionTitle(R.string.section_pages) }
        item(key = "pages") { PagesCard(details.pages) }
    }
}

@Composable
private fun SectionTitle(@StringRes title: Int) {
    Text(
        text = stringResource(title),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

@Composable
private fun InfoRow(@StringRes label: Int, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f),
        )
        SelectionContainer(modifier = Modifier.weight(0.6f)) {
            Text(text = value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun yesNo(value: Boolean?): String = when (value) {
    true -> stringResource(R.string.value_yes)
    false -> stringResource(R.string.value_no)
    null -> stringResource(R.string.value_not_available)
}

@Composable
private fun bytes(value: Int?): String =
    if (value == null) stringResource(R.string.value_not_available) else stringResource(R.string.value_bytes, value)

@Composable
private fun TagInfoCard(details: TagDetails) {
    val notAvailable = stringResource(R.string.value_not_available)
    val scanned = remember(details.scannedAt) {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(Date(details.scannedAt))
    }
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
        Column(modifier = Modifier.padding(16.dp)) {
            InfoRow(R.string.info_tag_type, stringResource(details.tagType.labelRes()))
            InfoRow(R.string.info_uid, details.uid)
            InfoRow(R.string.info_technologies, details.technologies.joinToString(", "))
            if (details.atqa != null) InfoRow(R.string.info_atqa, details.atqa)
            if (details.sak != null) {
                InfoRow(R.string.info_sak, "0x" + details.sak.toString(16).uppercase().padStart(2, '0'))
            }
            InfoRow(R.string.info_memory_size, bytes(details.maxSize))
            InfoRow(R.string.info_used_size, bytes(details.usedSize))
            InfoRow(R.string.info_writable, yesNo(details.writable))
            InfoRow(R.string.info_can_make_read_only, yesNo(details.canMakeReadOnly))
            InfoRow(R.string.info_ndef_formatable, yesNo(details.formatable))
            InfoRow(R.string.info_scanned_at, scanned.ifEmpty { notAvailable })
        }
    }
}

@Composable
private fun UnsupportedCard() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = stringResource(R.string.unsupported_title), style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(R.string.unsupported_body),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun RecordCard(record: RecordInfo) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(record.kind.labelRes()),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            if (record.summary.isNotEmpty()) {
                SelectionContainer {
                    Text(
                        text = record.summary,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            if (record.detail.isNotEmpty()) {
                Text(
                    text = record.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(R.string.record_payload_hex),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
            SelectionContainer {
                Text(
                    text = record.payloadHex.ifEmpty { stringResource(R.string.record_payload_empty) },
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}

@Composable
private fun PagesCard(pages: List<TagPage>) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        SelectionContainer {
            Column(modifier = Modifier.padding(16.dp)) {
                for (page in pages) {
                    Text(
                        text = page.index.toString().padStart(3, '0') + "  " + page.hex,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }
    }
}
