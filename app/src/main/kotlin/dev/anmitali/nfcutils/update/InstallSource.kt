// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.update

import android.content.Context

private val FdroidInstallers = setOf(
    "org.fdroid.fdroid",
    "org.fdroid.basic",
    "com.looker.droidify",
    "com.machiav3lli.fdroid",
)

fun isFdroidInstaller(installer: String?): Boolean =
    installer != null && (installer in FdroidInstallers || installer.startsWith("org.fdroid."))

fun updatesAllowed(context: Context): Boolean {
    val source = context.packageManager.getInstallSourceInfo(context.packageName)
    return !isFdroidInstaller(source.installingPackageName) && !isFdroidInstaller(source.initiatingPackageName)
}
