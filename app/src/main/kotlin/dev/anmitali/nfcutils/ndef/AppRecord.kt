// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

const val ANDROID_APP_TYPE = "android.com:pkg"

private val PackagePattern = Regex("^[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+$")

fun buildAppRecord(packageName: String): RecordBuild {
    val name = packageName.trim()
    if (name.isEmpty()) return RecordBuild.Invalid(FieldKey.APP_PACKAGE, FieldError.Empty)
    if (!PackagePattern.matches(name)) return RecordBuild.Invalid(FieldKey.APP_PACKAGE, FieldError.InvalidFormat)
    return RecordBuild.Ok(externalRecord(ANDROID_APP_TYPE, name.toByteArray(Charsets.UTF_8)))
}
