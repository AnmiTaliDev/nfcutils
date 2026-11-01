// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

private const val LATEST_RELEASE_URL = "https://api.github.com/repos/AnmiTaliDev/nfcutils/releases/latest"

suspend fun fetchLatestRelease(): ReleaseInfo? = withContext(Dispatchers.IO) {
    try {
        val connection = openHttps(LATEST_RELEASE_URL)
        try {
            parseRelease(connection.inputStream.bufferedReader().use { it.readText() })
        } finally {
            connection.disconnect()
        }
    } catch (e: IOException) {
        null
    }
}
