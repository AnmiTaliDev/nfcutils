// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ndef

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

fun RecordBuild.record(): NdefRecordData {
    assertTrue("Expected Ok but was $this", this is RecordBuild.Ok)
    return (this as RecordBuild.Ok).record
}

fun assertInvalid(expectedField: String, expectedError: FieldError, build: RecordBuild) {
    assertTrue("Expected Invalid but was $build", build is RecordBuild.Invalid)
    build as RecordBuild.Invalid
    assertEquals(expectedField, build.field)
    assertEquals(expectedError, build.error)
}
