// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.nfc

private const val PAGE_SIZE_BYTES = 4

enum class ChipFeature { Password, Counter, OriginalitySignature, TripleDes }

class ChipSpec(val pages: Int, val userBytes: Int, val features: List<ChipFeature>) {
    val totalBytes: Int = pages * PAGE_SIZE_BYTES
}

fun TagType.chipSpec(): ChipSpec? = when (this) {
    TagType.MifareUltralight -> ChipSpec(16, 48, emptyList())
    TagType.MifareUltralightC -> ChipSpec(48, 144, listOf(ChipFeature.TripleDes))
    TagType.UltralightEv1Ul21 ->
        ChipSpec(41, 128, listOf(ChipFeature.Password, ChipFeature.OriginalitySignature))
    TagType.Ntag212 -> ChipSpec(41, 128, listOf(ChipFeature.Password, ChipFeature.OriginalitySignature))
    TagType.Ntag213 -> ntagSpec(45, 144)
    TagType.Ntag215 -> ntagSpec(135, 504)
    TagType.Ntag216 -> ntagSpec(231, 888)
    else -> null
}

private fun ntagSpec(pages: Int, userBytes: Int) = ChipSpec(
    pages,
    userBytes,
    listOf(ChipFeature.Password, ChipFeature.Counter, ChipFeature.OriginalitySignature),
)
