// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.nfc

import android.nfc.FormatException
import android.nfc.Tag
import android.nfc.TagLostException
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import dev.anmitali.nfcutils.ndef.NdefCodec
import dev.anmitali.nfcutils.ndef.NdefRecordData
import dev.anmitali.nfcutils.ndef.Tnf
import dev.anmitali.nfcutils.ndef.describeRecord
import dev.anmitali.nfcutils.ndef.toHex
import java.io.IOException

private const val MAX_PAGES = 256
private const val PAGES_PER_READ = 4
private const val PAGE_SIZE = 4

private class NdefSnapshot(
    val type: String,
    val maxSize: Int,
    val writable: Boolean,
    val canMakeReadOnly: Boolean,
    val records: List<NdefRecordData>,
    val readable: Boolean,
)

class TagReader {
    sealed interface Result {
        class Success(val details: TagDetails, val records: List<NdefRecordData>) : Result

        class Failed(val failure: TagFailure) : Result
    }

    fun read(tag: Tag): Result {
        val technologies = tag.techList.map { it.substringAfterLast('.') }
        val nfcA = NfcA.get(tag)
        val unsupported = "MifareClassic" in technologies && "Ndef" !in technologies
        try {
            val ndef = if (unsupported) null else Ndef.get(tag)?.let { readNdef(it) }
            val ultralight = if (unsupported) null else MifareUltralight.get(tag)
            val pages = if (ultralight == null) emptyList() else readPages(ultralight)
            val records = ndef?.records.orEmpty()
            val usedSize = when {
                ndef == null || !ndef.readable -> null
                records.all { it.tnf == Tnf.EMPTY } -> 0
                else -> NdefCodec.size(records)
            }
            val details = TagDetails(
                scannedAt = System.currentTimeMillis(),
                uid = tag.id.toHex(":"),
                tagType = classifyTag(technologies, ndef?.type, ultralight?.type, ndef?.maxSize),
                technologies = technologies,
                atqa = nfcA?.atqa?.toHex(),
                sak = nfcA?.sak?.toInt(),
                maxSize = ndef?.maxSize,
                usedSize = usedSize,
                writable = ndef?.writable,
                canMakeReadOnly = ndef?.canMakeReadOnly,
                formatable = "NdefFormatable" in technologies,
                unsupported = unsupported,
                records = records.map { describeRecord(it) },
                pages = pages,
            )
            return Result.Success(details, records)
        } catch (e: TagLostException) {
            return Result.Failed(TagFailure.TagLost)
        } catch (e: IOException) {
            return Result.Failed(TagFailure.IoError)
        }
    }

    private fun readNdef(ndef: Ndef): NdefSnapshot = ndef.use {
        it.connect()
        var readable = true
        val message = try {
            it.ndefMessage
        } catch (e: FormatException) {
            readable = false
            null
        }
        NdefSnapshot(
            type = it.type,
            maxSize = it.maxSize,
            writable = it.isWritable,
            canMakeReadOnly = it.canMakeReadOnly(),
            records = message?.records?.map { record -> record.toData() }.orEmpty(),
            readable = readable,
        )
    }

    private fun readPages(ultralight: MifareUltralight): List<TagPage> = ultralight.use {
        it.connect()
        val pages = mutableListOf<ByteArray>()
        var offset = 0
        search@ while (offset < MAX_PAGES) {
            val chunk = try {
                it.readPages(offset)
            } catch (e: TagLostException) {
                throw e
            } catch (e: IOException) {
                break
            }
            for (index in 0 until PAGES_PER_READ) {
                val page = chunk.copyOfRange(index * PAGE_SIZE, (index + 1) * PAGE_SIZE)
                if (pages.isNotEmpty() && page.contentEquals(pages[0])) break@search
                pages.add(page)
            }
            offset += PAGES_PER_READ
        }
        pages.mapIndexed { index, page -> TagPage(index, page.toHex()) }
    }
}
