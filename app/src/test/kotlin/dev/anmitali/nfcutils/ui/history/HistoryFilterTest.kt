// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.history

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryFilterTest {
    private val uid = "04:A1:B2:C3:D4:E5:F6"

    @Test
    fun blankQueryMatchesEverything() {
        assertTrue(matchesHistoryQuery(uid, "Hello", ""))
        assertTrue(matchesHistoryQuery(uid, "", "   "))
    }

    @Test
    fun matchesSummaryIgnoringCase() {
        assertTrue(matchesHistoryQuery(uid, "Hello World", "world"))
        assertFalse(matchesHistoryQuery(uid, "Hello World", "mars"))
    }

    @Test
    fun matchesUidWithAndWithoutSeparators() {
        assertTrue(matchesHistoryQuery(uid, "", "a1:b2"))
        assertTrue(matchesHistoryQuery(uid, "", "A1B2"))
        assertTrue(matchesHistoryQuery(uid, "", "a1 b2"))
    }

    @Test
    fun doesNotMatchUnrelatedUid() {
        assertFalse(matchesHistoryQuery(uid, "", "ff:ee"))
    }

    @Test
    fun trimsQuery() {
        assertTrue(matchesHistoryQuery(uid, "Hello", "  hello  "))
    }
}
