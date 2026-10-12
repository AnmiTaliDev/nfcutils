// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.history

fun matchesHistoryQuery(uid: String, summary: String, query: String): Boolean {
    val needle = query.trim()
    if (needle.isEmpty()) return true
    if (summary.contains(needle, ignoreCase = true)) return true
    return uid.replace(":", "").contains(needle.replace(":", "").replace(" ", ""), ignoreCase = true)
}
