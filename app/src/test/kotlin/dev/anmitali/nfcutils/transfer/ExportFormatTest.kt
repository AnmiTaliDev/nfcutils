// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.transfer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportFormatTest {
    @Test
    fun writesHeaderFirst() {
        assertEquals("NFC-UTILS-EXPORT 1", writeBlocks(emptyList()).lines().first())
    }

    @Test
    fun roundTripsBlocksWithRepeatedKeys() {
        val blocks = listOf(
            Block("a", listOf("k" to "1", "k" to "2", "x" to "y")),
            Block("b", listOf("z" to "")),
        )
        val read = readBlocks(writeBlocks(blocks))!!
        assertEquals(listOf("a", "b"), read.map { it.type })
        assertEquals(listOf("1", "2"), read[0].all("k"))
        assertEquals("y", read[0].first("x"))
        assertEquals("", read[1].first("z"))
    }

    @Test
    fun roundTripsSpecialCharacters() {
        val value = "line1\nline2 = 100% | é€ [x]\r\n"
        val read = readBlocks(writeBlocks(listOf(Block("a", listOf("k" to value)))))!!
        assertEquals(value, read[0].first("k"))
    }

    @Test
    fun encodedValuesNeverContainNewlines() {
        val text = writeBlocks(listOf(Block("a", listOf("k" to "a\nb\r\nc"))))
        assertEquals(4, text.trim().lines().size)
    }

    @Test
    fun rejectsMissingHeader() {
        assertNull(readBlocks("[a]\nk=v\n"))
        assertNull(readBlocks(""))
    }

    @Test
    fun rejectsUnknownVersion() {
        assertNull(readBlocks("NFC-UTILS-EXPORT 2\n\n[a]\nk=v\n"))
    }

    @Test
    fun rejectsEntryOutsideBlock() {
        assertNull(readBlocks("NFC-UTILS-EXPORT 1\nk=v\n"))
    }

    @Test
    fun rejectsLineWithoutSeparator() {
        assertNull(readBlocks("NFC-UTILS-EXPORT 1\n[a]\nnonsense\n"))
        assertNull(readBlocks("NFC-UTILS-EXPORT 1\n[a]\n=v\n"))
    }

    @Test
    fun toleratesWindowsLineEndingsAndExtraBlankLines() {
        val read = readBlocks("NFC-UTILS-EXPORT 1\r\n\r\n\r\n[a]\r\nk=v\r\n\r\n")!!
        assertEquals("v", read[0].first("k"))
    }

    @Test
    fun missingKeyIsNull() {
        val read = readBlocks(writeBlocks(listOf(Block("a", emptyList()))))!!
        assertNull(read[0].first("nope"))
        assertTrue(read[0].all("nope").isEmpty())
    }
}
