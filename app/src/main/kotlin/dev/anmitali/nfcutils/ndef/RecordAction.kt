// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

private val OpenableSchemes = setOf("http", "https", "tel", "mailto", "sms", "smsto", "geo")

sealed interface RecordAction {
    class OpenUri(val uri: String) : RecordAction

    class OpenStore(val packageName: String) : RecordAction

    class CopyWifiPassword(val password: String) : RecordAction
}

private fun openUriAction(uri: String): RecordAction? {
    val scheme = uri.substringBefore(':', "").lowercase()
    return if (scheme in OpenableSchemes) RecordAction.OpenUri(uri) else null
}

fun recordAction(info: RecordInfo): RecordAction? {
    val payload = try {
        info.payloadHex.hexToBytes()
    } catch (e: IllegalArgumentException) {
        return null
    }
    return when (info.kind) {
        ParsedKind.Uri -> openUriAction(if (payload.isEmpty()) info.summary else decodeUriPayload(payload) ?: return null)
        ParsedKind.SmartPoster -> parseSmartPoster(payload)?.uri?.let { openUriAction(it) }
        ParsedKind.Wifi -> parseWifiPayload(payload)?.networkKey?.takeIf { it.isNotEmpty() }
            ?.let { RecordAction.CopyWifiPassword(it) }
        ParsedKind.AndroidApp -> RecordAction.OpenStore(String(payload, Charsets.UTF_8))
        else -> null
    }
}
