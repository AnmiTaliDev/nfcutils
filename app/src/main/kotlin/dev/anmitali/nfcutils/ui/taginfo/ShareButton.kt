// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.taginfo

import android.content.Intent
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import dev.anmitali.nfcutils.R
import dev.anmitali.nfcutils.nfc.TagDetails
import kotlinx.coroutines.launch

@Composable
fun ShareButton(details: TagDetails) {
    val context = LocalContext.current
    val host = LocalView.current.rootView as ViewGroup
    val scope = rememberCoroutineScope()
    val chooserTitle = stringResource(R.string.share_chooser_title)
    val colorScheme = MaterialTheme.colorScheme
    val shapes = MaterialTheme.shapes
    val typography = MaterialTheme.typography
    var choosing by remember { mutableStateOf(false) }

    OutlinedButton(onClick = { choosing = true }) {
        Text(stringResource(R.string.action_share))
    }

    if (choosing) {
        AlertDialog(
            onDismissRequest = { choosing = false },
            title = { Text(stringResource(R.string.share_as_title)) },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.share_as_text)) },
                        modifier = Modifier.clickable {
                            choosing = false
                            val send = Intent(Intent.ACTION_SEND)
                                .setType("text/plain")
                                .putExtra(Intent.EXTRA_TEXT, buildShareText(context, details))
                            context.startActivity(Intent.createChooser(send, chooserTitle))
                        },
                    )
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.share_as_image)) },
                        modifier = Modifier.clickable {
                            choosing = false
                            scope.launch {
                                val bitmap = renderShareImage(host, details, colorScheme, shapes, typography)
                                shareImage(context, bitmap, chooserTitle)
                            }
                        },
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { choosing = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}
