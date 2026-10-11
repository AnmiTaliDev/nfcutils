// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

private val ThemeModeKey = stringPreferencesKey("theme_mode")
private val DynamicColorKey = booleanPreferencesKey("dynamic_color")

class SettingsStore(context: Context) {
    private val dataStore = context.applicationContext.settingsDataStore

    val settings: Flow<AppSettings> = dataStore.data.map { preferences ->
        AppSettings(
            themeMode = ThemeMode.entries.firstOrNull { it.name == preferences[ThemeModeKey] } ?: ThemeMode.System,
            dynamicColor = preferences[DynamicColorKey] ?: true,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[ThemeModeKey] = mode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { it[DynamicColorKey] = enabled }
    }
}
