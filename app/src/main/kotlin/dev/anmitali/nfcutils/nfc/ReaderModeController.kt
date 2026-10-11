// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.nfc

import android.app.Activity
import android.nfc.NfcAdapter

private const val READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A or
    NfcAdapter.FLAG_READER_NFC_B or
    NfcAdapter.FLAG_READER_NFC_F or
    NfcAdapter.FLAG_READER_NFC_V or
    NfcAdapter.FLAG_READER_NFC_BARCODE

class ReaderModeController(private val status: NfcStatus, private val session: NfcSession) {
    suspend fun run(activity: Activity) {
        val adapter = status.adapter ?: return
        val callback = NfcAdapter.ReaderCallback(session::onTagDiscovered)
        try {
            status.state.collect { availability ->
                if (availability == NfcAvailability.Enabled) {
                    adapter.enableReaderMode(activity, callback, READER_FLAGS, null)
                } else {
                    adapter.disableReaderMode(activity)
                }
            }
        } finally {
            adapter.disableReaderMode(activity)
        }
    }
}
