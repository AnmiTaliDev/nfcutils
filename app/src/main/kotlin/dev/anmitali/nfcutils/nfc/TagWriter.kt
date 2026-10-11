// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.nfc

import android.nfc.FormatException
import android.nfc.NdefMessage
import android.nfc.Tag
import android.nfc.TagLostException
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import dev.anmitali.nfcutils.ndef.EmptyRecord
import dev.anmitali.nfcutils.ndef.NdefCodec
import dev.anmitali.nfcutils.ndef.NdefRecordData
import java.io.IOException

class TagWriter {
    sealed interface Result {
        data object Success : Result

        class Failed(val failure: TagFailure) : Result
    }

    fun run(operation: TagOperation, tag: Tag): Result = try {
        when (operation) {
            is TagOperation.Write -> write(tag, operation.records)
            TagOperation.Erase -> erase(tag)
            TagOperation.Format -> format(tag)
            TagOperation.Lock -> lock(tag)
        }
    } catch (e: TagLostException) {
        Result.Failed(TagFailure.TagLost)
    } catch (e: FormatException) {
        Result.Failed(TagFailure.InvalidMessage)
    } catch (e: IOException) {
        Result.Failed(TagFailure.IoError)
    }

    private fun write(tag: Tag, records: List<NdefRecordData>): Result {
        val message = records.toNdefMessage()
        val ndef = Ndef.get(tag)
        if (ndef != null) {
            return ndef.use {
                it.connect()
                val needed = NdefCodec.size(records)
                when {
                    !it.isWritable -> Result.Failed(TagFailure.NotWritable)
                    needed > it.maxSize -> Result.Failed(TagFailure.TooLarge(needed, it.maxSize))
                    else -> {
                        it.writeNdefMessage(message)
                        Result.Success
                    }
                }
            }
        }
        return formatWith(tag, message)
    }

    private fun erase(tag: Tag): Result {
        val ndef = Ndef.get(tag)
        if (ndef == null) {
            return if (NdefFormatable.get(tag) != null) {
                Result.Failed(TagFailure.NotFormatted)
            } else {
                Result.Failed(TagFailure.UnsupportedTag)
            }
        }
        return ndef.use {
            it.connect()
            if (!it.isWritable) {
                Result.Failed(TagFailure.NotWritable)
            } else {
                it.writeNdefMessage(listOf(EmptyRecord).toNdefMessage())
                Result.Success
            }
        }
    }

    private fun format(tag: Tag): Result {
        if (Ndef.get(tag) != null) return Result.Failed(TagFailure.AlreadyFormatted)
        return formatWith(tag, listOf(EmptyRecord).toNdefMessage())
    }

    private fun lock(tag: Tag): Result {
        val ndef = Ndef.get(tag) ?: return Result.Failed(TagFailure.UnsupportedTag)
        return ndef.use {
            it.connect()
            when {
                !it.isWritable -> Result.Failed(TagFailure.NotWritable)
                !it.canMakeReadOnly() -> Result.Failed(TagFailure.CannotLock)
                it.makeReadOnly() -> Result.Success
                else -> Result.Failed(TagFailure.CannotLock)
            }
        }
    }

    private fun formatWith(tag: Tag, message: NdefMessage): Result {
        val formatable = NdefFormatable.get(tag) ?: return Result.Failed(TagFailure.UnsupportedTag)
        return formatable.use {
            it.connect()
            it.format(message)
            Result.Success
        }
    }
}
