// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dev.anmitali.nfcutils.R

object Routes {
    const val SCAN = "scan"
    const val WRITE = "write"
    const val TOOLS = "tools"
    const val HISTORY = "history"
    const val SAVED = "saved"
    const val SETTINGS = "settings"
    const val ABOUT = "about"
    const val NOTICES = "notices"
    const val ARG_ID = "id"
    const val HISTORY_DETAIL = "history/{$ARG_ID}"
    const val SET_EDITOR = "saved/edit/{$ARG_ID}"

    fun historyDetail(id: Long) = "history/$id"

    fun setEditor(id: Long) = "saved/edit/$id"
}

enum class Tab(val route: String, @StringRes val label: Int, @DrawableRes val icon: Int) {
    Scan(Routes.SCAN, R.string.tab_scan, R.drawable.ic_tab_scan),
    Write(Routes.WRITE, R.string.tab_write, R.drawable.ic_tab_write),
    Tools(Routes.TOOLS, R.string.tab_tools, R.drawable.ic_tab_tools),
    History(Routes.HISTORY, R.string.tab_history, R.drawable.ic_tab_history),
    Saved(Routes.SAVED, R.string.tab_saved, R.drawable.ic_tab_saved),
}

@StringRes
fun titleFor(route: String?): Int = when (route) {
    Routes.SETTINGS -> R.string.screen_settings
    Routes.ABOUT -> R.string.screen_about
    Routes.NOTICES -> R.string.screen_notices
    Routes.HISTORY_DETAIL -> R.string.screen_history_detail
    Routes.SET_EDITOR -> R.string.screen_set_editor
    else -> R.string.app_name
}
