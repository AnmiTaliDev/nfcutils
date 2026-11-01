// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.update

import org.json.JSONException
import org.json.JSONObject

const val DOWNLOAD_PREFIX = "https://github.com/AnmiTaliDev/nfcutils/releases/download/"

private const val SHA256_PREFIX = "sha256:"

class ReleaseInfo(
    val tag: String,
    val notes: String,
    val pageUrl: String,
    val apkUrl: String,
    val apkSize: Long,
    val sha256: String?,
)

fun parseRelease(json: String): ReleaseInfo? {
    try {
        val root = JSONObject(json)
        val assets = root.getJSONArray("assets")
        for (index in 0 until assets.length()) {
            val asset = assets.getJSONObject(index)
            val url = asset.getString("browser_download_url")
            if (!asset.getString("name").endsWith(".apk") || !url.startsWith(DOWNLOAD_PREFIX)) continue
            return ReleaseInfo(
                tag = root.getString("tag_name"),
                notes = root.optString("body"),
                pageUrl = root.getString("html_url"),
                apkUrl = url,
                apkSize = asset.getLong("size"),
                sha256 = asset.optString("digest").takeIf { it.startsWith(SHA256_PREFIX) }?.removePrefix(SHA256_PREFIX),
            )
        }
        return null
    } catch (e: JSONException) {
        return null
    }
}
