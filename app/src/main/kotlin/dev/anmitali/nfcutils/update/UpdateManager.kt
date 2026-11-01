// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.update

import android.content.Context
import dev.anmitali.nfcutils.BuildConfig
import dev.anmitali.nfcutils.settings.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val CHECK_INTERVAL_MS = 24L * 60 * 60 * 1000

class UpdateManager(
    private val context: Context,
    private val scope: CoroutineScope,
    private val settingsStore: SettingsStore,
) {
    private val installer = ApkInstaller(context)
    private val mutableState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = mutableState.asStateFlow()
    val enabled: Boolean = updatesAllowed(context)

    init {
        scope.launch {
            installer.events.collect { event ->
                val release = releaseOf(mutableState.value) ?: return@collect
                if (event is InstallEvent.Failed) mutableState.value = UpdateState.Failed(release, event.failure)
            }
        }
    }

    suspend fun checkIfDue() {
        val last = settingsStore.lastUpdateCheck.first()
        if (System.currentTimeMillis() - last >= CHECK_INTERVAL_MS) check()
    }

    fun check() {
        if (!enabled || isBusy()) return
        mutableState.value = UpdateState.Checking
        scope.launch {
            val release = fetchLatestRelease()
            settingsStore.setLastUpdateCheck(System.currentTimeMillis())
            mutableState.value = when {
                release == null -> UpdateState.CheckFailed
                isNewerVersion(release.tag, BuildConfig.VERSION_NAME) -> UpdateState.Available(release)
                else -> UpdateState.UpToDate
            }
        }
    }

    fun install() {
        if (!enabled || isBusy()) return
        val release = releaseOf(mutableState.value) ?: return
        if (!context.packageManager.canRequestPackageInstalls()) {
            mutableState.value = UpdateState.NeedsPermission(release)
            return
        }
        mutableState.value = UpdateState.Downloading(release, 0f)
        scope.launch {
            val failure = installer.install(release) { progress ->
                mutableState.value = UpdateState.Downloading(release, progress)
            }
            mutableState.value = if (failure == null) UpdateState.Installing(release) else UpdateState.Failed(release, failure)
        }
    }

    fun dismiss() {
        if (mutableState.value !is UpdateState.Downloading) mutableState.value = UpdateState.Idle
    }

    private fun isBusy() = mutableState.value.let {
        it is UpdateState.Checking || it is UpdateState.Downloading || it is UpdateState.Installing
    }

    private fun releaseOf(state: UpdateState): ReleaseInfo? = when (state) {
        is UpdateState.Available -> state.release
        is UpdateState.NeedsPermission -> state.release
        is UpdateState.Downloading -> state.release
        is UpdateState.Installing -> state.release
        is UpdateState.Failed -> state.release
        else -> null
    }
}
