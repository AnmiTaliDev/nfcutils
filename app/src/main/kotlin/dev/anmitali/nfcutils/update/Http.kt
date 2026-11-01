// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.update

import dev.anmitali.nfcutils.BuildConfig
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

private const val CONNECT_TIMEOUT_MS = 15_000
private const val READ_TIMEOUT_MS = 30_000
private const val HTTP_OK = 200

fun openHttps(url: String): HttpURLConnection {
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.connectTimeout = CONNECT_TIMEOUT_MS
    connection.readTimeout = READ_TIMEOUT_MS
    connection.setRequestProperty("User-Agent", "NFC-Utils/" + BuildConfig.VERSION_NAME)
    if (connection.responseCode != HTTP_OK) {
        connection.disconnect()
        throw IOException("HTTP " + connection.responseCode)
    }
    return connection
}
