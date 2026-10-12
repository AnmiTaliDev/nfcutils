// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VCardParserTest {
    private val bs = "\\"

    @Test
    fun parsesBuiltVCard() {
        val text = String(
            buildVCardRecord("Ann", "Lee", "+123", "a@b.co", "Acme", "https://example.com", "Main St 1", "Berlin", "10115", "Germany")
                .record().payload,
        )
        val card = parseVCard(text)!!
        assertEquals("Ann", card.firstName)
        assertEquals("Lee", card.lastName)
        assertEquals("+123", card.phone)
        assertEquals("a@b.co", card.email)
        assertEquals("Acme", card.organization)
        assertEquals("https://example.com", card.website)
        assertEquals("Main St 1", card.street)
        assertEquals("Berlin", card.city)
        assertEquals("10115", card.postalCode)
        assertEquals("Germany", card.country)
    }

    @Test
    fun unescapesValues() {
        val text = "BEGIN:VCARD\nN:Le${bs},e;Ann${bs};;;\nORG:A${bs}${bs}B${bs}nC\nEND:VCARD"
        val card = parseVCard(text)!!
        assertEquals("Le,e", card.lastName)
        assertEquals("Ann;", card.firstName)
        assertEquals("A${bs}B\nC", card.organization)
    }

    @Test
    fun ignoresParametersAndGroups() {
        val text = "BEGIN:VCARD\r\nVERSION:3.0\r\nN:Lee;Ann;;;\r\nitem1.TEL;TYPE=CELL:+49 1\r\nEMAIL;TYPE=INTERNET:a@b.co\r\nEND:VCARD\r\n"
        val card = parseVCard(text)!!
        assertEquals("+49 1", card.phone)
        assertEquals("a@b.co", card.email)
    }

    @Test
    fun usesFirstOccurrenceOfRepeatedFields() {
        val text = "BEGIN:VCARD\nN:Lee;Ann;;;\nTEL:111\nTEL:222\nEND:VCARD"
        assertEquals("111", parseVCard(text)!!.phone)
    }

    @Test
    fun unfoldsContinuationLines() {
        val text = "BEGIN:VCARD\nN:Lee;Ann;;;\nTEL:+12\n 34\nEND:VCARD"
        assertEquals("+1234", parseVCard(text)!!.phone)
    }

    @Test
    fun fallsBackToFormattedName() {
        val card = parseVCard("BEGIN:VCARD\nFN:Ann Lee\nEND:VCARD")!!
        assertEquals("Ann Lee", card.firstName)
        assertEquals("", card.lastName)
    }

    @Test
    fun missingFieldsAreEmpty() {
        val card = parseVCard("BEGIN:VCARD\nN:Lee;Ann;;;\nEND:VCARD")!!
        assertEquals("", card.phone)
        assertEquals("", card.organization)
        assertEquals("", card.street)
    }

    @Test
    fun rejectsTextWithoutBeginMarker() {
        assertNull(parseVCard("N:Lee;Ann;;;"))
    }
}
