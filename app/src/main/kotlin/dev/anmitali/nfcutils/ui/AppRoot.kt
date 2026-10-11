// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.anmitali.nfcutils.AppContainer
import dev.anmitali.nfcutils.settings.AppSettings
import dev.anmitali.nfcutils.theme.NfcUtilsTheme
import dev.anmitali.nfcutils.ui.navigation.AppScaffold

@Composable
fun AppRoot(container: AppContainer) {
    val settings by container.settingsStore.settings.collectAsStateWithLifecycle(initialValue = AppSettings())
    NfcUtilsTheme(settings) {
        AppScaffold(container)
    }
}
