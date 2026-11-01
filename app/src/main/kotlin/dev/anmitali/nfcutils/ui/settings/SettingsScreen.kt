// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.settings

import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.anmitali.nfcutils.R
import dev.anmitali.nfcutils.ui.navigation.Routes

@Composable
fun SettingsScreen(updatesEnabled: Boolean, onOpen: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        SettingsItem(R.string.screen_appearance, R.string.settings_appearance_summary) { onOpen(Routes.APPEARANCE) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            SettingsItem(R.string.settings_language, R.string.settings_language_summary) { onOpen(Routes.LANGUAGE) }
        }
        SettingsItem(R.string.settings_data_title, R.string.settings_data_summary) { onOpen(Routes.DATA) }
        if (updatesEnabled) {
            SettingsItem(R.string.screen_updates, R.string.settings_updates_summary) { onOpen(Routes.UPDATES) }
        }
        SettingsItem(R.string.settings_notices, R.string.settings_notices_summary) { onOpen(Routes.NOTICES) }
        SettingsItem(R.string.settings_about, R.string.settings_about_summary) { onOpen(Routes.ABOUT) }
    }
}

@Composable
private fun SettingsItem(@StringRes title: Int, @StringRes summary: Int, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(title)) },
        supportingContent = { Text(stringResource(summary)) },
        modifier = Modifier.clickable(onClick = onClick),
    )
}
