// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.editor

import dev.anmitali.nfcutils.ndef.RecordDraft
import kotlinx.coroutines.flow.MutableStateFlow

class DraftHandoff {
    val pending = MutableStateFlow<List<RecordDraft>?>(null)
}
