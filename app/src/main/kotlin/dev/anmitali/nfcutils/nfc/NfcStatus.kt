// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.nfc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow

enum class NfcAvailability { Unavailable, Disabled, Enabled }

class NfcStatus(private val context: Context, val adapter: NfcAdapter?) {
    private val mutableState = MutableStateFlow(read())
    val state: StateFlow<NfcAvailability> = mutableState.asStateFlow()

    private fun read(): NfcAvailability = when {
        adapter == null -> NfcAvailability.Unavailable
        adapter.isEnabled -> NfcAvailability.Enabled
        else -> NfcAvailability.Disabled
    }

    suspend fun track() {
        mutableState.value = read()
        if (adapter == null) return
        val changes = callbackFlow {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    trySend(Unit)
                }
            }
            ContextCompat.registerReceiver(
                context,
                receiver,
                IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED),
                ContextCompat.RECEIVER_NOT_EXPORTED,
            )
            awaitClose { context.unregisterReceiver(receiver) }
        }
        changes.collect { mutableState.value = read() }
    }
}
