// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import dev.anmitali.nfcutils.ndef.NdefRecordData

fun NdefRecord.toData(): NdefRecordData = NdefRecordData(tnf.toInt(), type, id, payload)

fun NdefRecordData.toNdefRecord(): NdefRecord = NdefRecord(tnf.toShort(), type, id, payload)

fun List<NdefRecordData>.toNdefMessage(): NdefMessage = NdefMessage(map { it.toNdefRecord() }.toTypedArray())
