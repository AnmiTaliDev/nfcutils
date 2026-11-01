// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.update

import android.content.Intent
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.anmitali.nfcutils.BuildConfig
import dev.anmitali.nfcutils.R
import dev.anmitali.nfcutils.ui.common.openUri
import dev.anmitali.nfcutils.update.UpdateFailure
import dev.anmitali.nfcutils.update.UpdateManager
import dev.anmitali.nfcutils.update.UpdateState

@Composable
fun UpdateDialog(manager: UpdateManager) {
    val state by manager.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (manager.state.value is UpdateState.NeedsPermission) manager.install()
    }

    when (val current = state) {
        is UpdateState.Available -> AlertDialog(
            onDismissRequest = manager::dismiss,
            title = { Text(stringResource(R.string.update_dialog_title)) },
            text = { Text(stringResource(R.string.update_dialog_body, current.release.tag, BuildConfig.VERSION_NAME)) },
            confirmButton = { TextButton(onClick = manager::install) { Text(stringResource(R.string.update_action)) } },
            dismissButton = { TextButton(onClick = manager::dismiss) { Text(stringResource(R.string.update_later)) } },
        )
        is UpdateState.NeedsPermission -> AlertDialog(
            onDismissRequest = manager::dismiss,
            title = { Text(stringResource(R.string.update_permission_title)) },
            text = { Text(stringResource(R.string.update_permission_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val settings = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, "package:${context.packageName}".toUri())
                        context.startActivity(settings)
                    },
                ) {
                    Text(stringResource(R.string.update_open_settings))
                }
            },
            dismissButton = { TextButton(onClick = manager::dismiss) { Text(stringResource(R.string.action_cancel)) } },
        )
        is UpdateState.Downloading -> AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.update_downloading_title)) },
            text = { LinearProgressIndicator(progress = { current.progress }) },
            confirmButton = {},
        )
        is UpdateState.Installing -> AlertDialog(
            onDismissRequest = manager::dismiss,
            title = { Text(stringResource(R.string.update_installing_title)) },
            text = { Text(stringResource(R.string.update_installing_body)) },
            confirmButton = {},
            dismissButton = { TextButton(onClick = manager::dismiss) { Text(stringResource(R.string.action_close)) } },
        )
        is UpdateState.Failed -> {
            val rejected = current.failure == UpdateFailure.Rejected
            AlertDialog(
                onDismissRequest = manager::dismiss,
                title = { Text(stringResource(R.string.update_failed_title)) },
                text = {
                    Text(
                        stringResource(
                            when (current.failure) {
                                UpdateFailure.Network -> R.string.update_failed_network
                                UpdateFailure.Corrupted -> R.string.update_failed_corrupted
                                UpdateFailure.Rejected -> R.string.update_failed_rejected
                            },
                        ),
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (rejected) {
                                openUri(context, current.release.pageUrl)
                                manager.dismiss()
                            } else {
                                manager.install()
                            }
                        },
                    ) {
                        Text(stringResource(if (rejected) R.string.update_open_release else R.string.action_try_again))
                    }
                },
                dismissButton = { TextButton(onClick = manager::dismiss) { Text(stringResource(R.string.action_close)) } },
            )
        }
        else -> Unit
    }
}
