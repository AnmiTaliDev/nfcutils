// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.update

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import dev.anmitali.nfcutils.ndef.toHex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.security.MessageDigest

private const val RESULT_ACTION = "dev.anmitali.nfcutils.UPDATE_RESULT"
private const val APK_NAME = "update.apk"
private const val BUFFER_SIZE = 64 * 1024

sealed interface InstallEvent {
    data object Success : InstallEvent

    class Failed(val failure: UpdateFailure) : InstallEvent
}

class ApkInstaller(private val context: Context) {
    private val mutableEvents = MutableSharedFlow<InstallEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<InstallEvent> = mutableEvents.asSharedFlow()

    init {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(receiverContext: Context, intent: Intent) {
                when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)) {
                    PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                        val confirmation = IntentCompat.getParcelableExtra(intent, Intent.EXTRA_INTENT, Intent::class.java)
                        if (confirmation != null) {
                            receiverContext.startActivity(confirmation.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        }
                    }
                    PackageInstaller.STATUS_SUCCESS -> mutableEvents.tryEmit(InstallEvent.Success)
                    else -> mutableEvents.tryEmit(InstallEvent.Failed(UpdateFailure.Rejected))
                }
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(RESULT_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

    suspend fun install(release: ReleaseInfo, onProgress: (Float) -> Unit): UpdateFailure? = withContext(Dispatchers.IO) {
        val packageInstaller = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        params.setAppPackageName(context.packageName)
        params.setSize(release.apkSize)
        val sessionId = packageInstaller.createSession(params)
        val session = packageInstaller.openSession(sessionId)
        var committed = false
        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val connection = openHttps(release.apkUrl)
            try {
                connection.inputStream.use { input ->
                    session.openWrite(APK_NAME, 0, release.apkSize).use { output ->
                        val buffer = ByteArray(BUFFER_SIZE)
                        var written = 0L
                        while (true) {
                            val count = input.read(buffer)
                            if (count < 0) break
                            output.write(buffer, 0, count)
                            digest.update(buffer, 0, count)
                            written += count
                            onProgress(written.toFloat() / release.apkSize)
                        }
                        session.fsync(output)
                    }
                }
            } finally {
                connection.disconnect()
            }
            if (release.sha256 != null && !digest.digest().toHex("").equals(release.sha256, ignoreCase = true)) {
                return@withContext UpdateFailure.Corrupted
            }
            session.commit(resultSender(sessionId))
            committed = true
            null
        } catch (e: IOException) {
            UpdateFailure.Network
        } finally {
            if (!committed) session.abandon()
            session.close()
        }
    }

    private fun resultSender(sessionId: Int) = PendingIntent.getBroadcast(
        context,
        sessionId,
        Intent(RESULT_ACTION).setPackage(context.packageName),
        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    ).intentSender
}
