// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

enum class FieldError { Empty, TooShort, TooLong, InvalidFormat, OutOfRange }

sealed interface RecordBuild {
    class Ok(val record: NdefRecordData) : RecordBuild

    class Invalid(val field: String, val error: FieldError) : RecordBuild
}
