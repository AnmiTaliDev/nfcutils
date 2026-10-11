// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

private const val NAME = "[a-z0-9][a-z0-9!#\$&^_.+-]*"
private val MimeTypePattern = Regex("^$NAME/$NAME$")

fun buildMimeRecord(mimeType: String, data: String): RecordBuild {
    val type = mimeType.trim().lowercase()
    if (type.isEmpty()) return RecordBuild.Invalid(FieldKey.MIME_TYPE, FieldError.Empty)
    if (!MimeTypePattern.matches(type)) return RecordBuild.Invalid(FieldKey.MIME_TYPE, FieldError.InvalidFormat)
    if (data.isEmpty()) return RecordBuild.Invalid(FieldKey.MIME_DATA, FieldError.Empty)
    return RecordBuild.Ok(mimeRecord(type, data.toByteArray(Charsets.UTF_8)))
}
