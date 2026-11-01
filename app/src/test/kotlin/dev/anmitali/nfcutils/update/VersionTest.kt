// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionTest {
    @Test
    fun newerVersionsAreDetected() {
        assertTrue(isNewerVersion("v0.3.1", "0.3.0"))
        assertTrue(isNewerVersion("v0.4.0", "0.3.9"))
        assertTrue(isNewerVersion("1.0.0", "0.9.9"))
        assertTrue(isNewerVersion("v0.10.0", "0.9.0"))
    }

    @Test
    fun sameOrOlderVersionsAreNotNewer() {
        assertFalse(isNewerVersion("v0.3.0", "0.3.0"))
        assertFalse(isNewerVersion("v0.2.9", "0.3.0"))
        assertFalse(isNewerVersion("v0.3", "0.3.0"))
    }

    @Test
    fun missingPartsCountAsZero() {
        assertTrue(isNewerVersion("v0.3.0.1", "0.3.0"))
    }

    @Test
    fun suffixIsIgnored() {
        assertFalse(isNewerVersion("v0.3.0-rc1", "0.3.0"))
    }

    @Test
    fun unparsableVersionIsNotNewer() {
        assertFalse(isNewerVersion("latest", "0.3.0"))
        assertFalse(isNewerVersion("v0.3.1", "dev"))
        assertFalse(isNewerVersion("", "0.3.0"))
    }
}
