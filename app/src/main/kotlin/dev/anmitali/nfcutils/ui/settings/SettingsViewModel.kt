// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.settings

import android.content.ContentResolver
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.anmitali.nfcutils.AppContainer
import dev.anmitali.nfcutils.R
import dev.anmitali.nfcutils.data.SavedSetDao
import dev.anmitali.nfcutils.data.toDrafts
import dev.anmitali.nfcutils.data.toTagDetails
import dev.anmitali.nfcutils.data.HistoryDao
import dev.anmitali.nfcutils.settings.AppSettings
import dev.anmitali.nfcutils.settings.SettingsStore
import dev.anmitali.nfcutils.settings.ThemeMode
import dev.anmitali.nfcutils.transfer.ExportedSet
import dev.anmitali.nfcutils.transfer.buildExport
import dev.anmitali.nfcutils.transfer.parseExport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

class UiMessage(@StringRes val resource: Int, val arguments: List<Any> = emptyList())

private const val MAX_IMPORT_BYTES = 10 * 1024 * 1024

class SettingsViewModel(
    private val settingsStore: SettingsStore,
    private val historyDao: HistoryDao,
    private val savedSetDao: SavedSetDao,
) : ViewModel() {
    private val mutableMessage = MutableStateFlow<UiMessage?>(null)
    val message: StateFlow<UiMessage?> = mutableMessage.asStateFlow()

    val settings: StateFlow<AppSettings> = settingsStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsStore.setThemeMode(mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { settingsStore.setDynamicColor(enabled) }
    }

    fun messageShown() {
        mutableMessage.value = null
    }

    fun exportTo(resolver: ContentResolver, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val sets = savedSetDao.getAllSets().map { ExportedSet(it.set.name, it.toDrafts()) }
            val scans = historyDao.getAllDetails().map { it.toTagDetails() }
            val bytes = buildExport(sets, scans).toByteArray(Charsets.UTF_8)
            val written = try {
                resolver.openOutputStream(uri, "wt")?.use { it.write(bytes) } != null
            } catch (e: IOException) {
                false
            }
            mutableMessage.value = if (written) {
                UiMessage(R.string.message_export_done, listOf(sets.size, scans.size))
            } else {
                UiMessage(R.string.message_export_failed)
            }
        }
    }

    fun importFrom(resolver: ContentResolver, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = try {
                resolver.openInputStream(uri)?.use { readLimited(it) }?.let { parseExport(String(it, Charsets.UTF_8)) }
            } catch (e: IOException) {
                null
            }
            if (data == null) {
                mutableMessage.value = UiMessage(R.string.message_import_failed)
                return@launch
            }
            val now = System.currentTimeMillis()
            for (set in data.sets) savedSetDao.save(0, set.name, set.drafts, now)
            var imported = 0
            for (scan in data.scans) {
                if (historyDao.countMatching(scan.uid, scan.scannedAt) == 0) {
                    historyDao.insertScan(scan)
                    imported++
                }
            }
            mutableMessage.value = UiMessage(R.string.message_import_done, listOf(data.sets.size, imported))
        }
    }

    private fun readLimited(stream: InputStream): ByteArray? {
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (true) {
            val count = stream.read(buffer)
            if (count < 0) break
            out.write(buffer, 0, count)
            if (out.size() > MAX_IMPORT_BYTES) return null
        }
        return out.toByteArray()
    }

    fun clearHistory() {
        viewModelScope.launch { historyDao.deleteAll() }
    }

    companion object {
        fun factory(container: AppContainer) = viewModelFactory {
            initializer { SettingsViewModel(container.settingsStore, container.historyDao, container.savedSetDao) }
        }
    }
}
