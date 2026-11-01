// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.update

fun isNewerVersion(candidate: String, current: String): Boolean {
    val candidateParts = parseVersion(candidate) ?: return false
    val currentParts = parseVersion(current) ?: return false
    for (index in 0 until maxOf(candidateParts.size, currentParts.size)) {
        val left = candidateParts.getOrElse(index) { 0 }
        val right = currentParts.getOrElse(index) { 0 }
        if (left != right) return left > right
    }
    return false
}

private fun parseVersion(value: String): List<Int>? {
    val core = value.trim().removePrefix("v").substringBefore('-')
    if (core.isEmpty()) return null
    val parts = core.split('.').map { it.toIntOrNull() ?: return null }
    return parts
}
