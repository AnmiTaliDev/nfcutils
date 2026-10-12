// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

enum class ParsedKind { Empty, Text, Uri, Mime, Wifi, SmartPoster, AndroidApp, External, Unknown }

sealed interface ParsedRecord {
    val kind: ParsedKind

    data object Empty : ParsedRecord {
        override val kind = ParsedKind.Empty
    }

    class Text(val content: TextContent) : ParsedRecord {
        override val kind = ParsedKind.Text
    }

    class Uri(val uri: String) : ParsedRecord {
        override val kind = ParsedKind.Uri
    }

    class Mime(val mimeType: String, val textPreview: String?) : ParsedRecord {
        override val kind = ParsedKind.Mime
    }

    class Wifi(val credential: WifiCredential) : ParsedRecord {
        override val kind = ParsedKind.Wifi
    }

    class SmartPoster(val title: String?, val uri: String?) : ParsedRecord {
        override val kind = ParsedKind.SmartPoster
    }

    class AndroidApp(val packageName: String) : ParsedRecord {
        override val kind = ParsedKind.AndroidApp
    }

    class External(val type: String) : ParsedRecord {
        override val kind = ParsedKind.External
    }

    class Unknown(val tnf: Int, val type: String) : ParsedRecord {
        override val kind = ParsedKind.Unknown
    }
}
