// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InstallSourceTest {
    @Test
    fun fdroidClientsAreRecognized() {
        assertTrue(isFdroidInstaller("org.fdroid.fdroid"))
        assertTrue(isFdroidInstaller("org.fdroid.basic"))
        assertTrue(isFdroidInstaller("com.looker.droidify"))
        assertTrue(isFdroidInstaller("com.machiav3lli.fdroid"))
        assertTrue(isFdroidInstaller("org.fdroid.other"))
    }

    @Test
    fun otherInstallersAreNotFdroid() {
        assertFalse(isFdroidInstaller(null))
        assertFalse(isFdroidInstaller("com.android.vending"))
        assertFalse(isFdroidInstaller("com.google.android.packageinstaller"))
        assertFalse(isFdroidInstaller("com.example.org.fdroid.fdroid"))
    }
}
