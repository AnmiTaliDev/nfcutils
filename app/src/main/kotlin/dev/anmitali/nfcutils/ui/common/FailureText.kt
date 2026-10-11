// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.anmitali.nfcutils.R
import dev.anmitali.nfcutils.nfc.TagFailure

@Composable
fun TagFailure.message(): String = when (this) {
    TagFailure.TagLost -> stringResource(R.string.failure_tag_lost)
    TagFailure.NotWritable -> stringResource(R.string.failure_not_writable)
    is TagFailure.TooLarge -> stringResource(R.string.failure_too_large, needed, capacity)
    TagFailure.UnsupportedTag -> stringResource(R.string.failure_unsupported_tag)
    TagFailure.NotFormatted -> stringResource(R.string.failure_not_formatted)
    TagFailure.AlreadyFormatted -> stringResource(R.string.failure_already_formatted)
    TagFailure.CannotLock -> stringResource(R.string.failure_cannot_lock)
    TagFailure.InvalidMessage -> stringResource(R.string.failure_invalid_message)
    TagFailure.IoError -> stringResource(R.string.failure_io_error)
}
