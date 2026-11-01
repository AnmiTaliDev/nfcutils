// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.nfc

import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceKindTest {
    @Test
    fun ndefTagsAndUltralightFamilyAreTags() {
        assertEquals(DeviceKind.NfcTag, TagType.Ntag215.deviceKind())
        assertEquals(DeviceKind.NfcTag, TagType.MifareUltralightC.deviceKind())
        assertEquals(DeviceKind.NfcTag, TagType.NfcForumType4.deviceKind())
    }

    @Test
    fun mifareClassicIsAccessCard() {
        assertEquals(DeviceKind.AccessCard, TagType.MifareClassic.deviceKind())
    }

    @Test
    fun isoDepIsSmartCard() {
        assertEquals(DeviceKind.SmartCard, TagType.Iso14443Type4.deviceKind())
        assertEquals(DeviceKind.SmartCard, TagType.Iso14443TypeB.deviceKind())
    }

    @Test
    fun otherTechnologiesMapToTheirOwnKinds() {
        assertEquals(DeviceKind.FeliCaCard, TagType.FeliCa.deviceKind())
        assertEquals(DeviceKind.Iso15693Tag, TagType.Iso15693.deviceKind())
        assertEquals(DeviceKind.BarcodeTag, TagType.Barcode.deviceKind())
        assertEquals(DeviceKind.Unknown, TagType.Iso14443TypeA.deviceKind())
        assertEquals(DeviceKind.Unknown, TagType.Unknown.deviceKind())
    }
}
