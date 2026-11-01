// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.update

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

private fun release(assets: String) =
    """{"tag_name":"v0.4.0","html_url":"https://github.com/AnmiTaliDev/nfcutils/releases/tag/v0.4.0","assets":[$assets]}"""

private fun asset(name: String, url: String, extra: String = "") =
    """{"name":"$name","browser_download_url":"$url","size":1234$extra}"""

@RunWith(AndroidJUnit4::class)
class ReleaseParserTest {
    private val goodUrl = DOWNLOAD_PREFIX + "v0.4.0/nfc-utils-v0.4.0-debug.apk"

    @Test
    fun parsesApkAsset() {
        val parsed = parseRelease(release(asset("nfc-utils-v0.4.0-debug.apk", goodUrl)))!!
        assertEquals("v0.4.0", parsed.tag)
        assertEquals(goodUrl, parsed.apkUrl)
        assertEquals(1234L, parsed.apkSize)
        assertNull(parsed.sha256)
    }

    @Test
    fun readsSha256Digest() {
        val extra = ""","digest":"sha256:abc123""""
        val parsed = parseRelease(release(asset("a.apk", goodUrl, extra)))!!
        assertEquals("abc123", parsed.sha256)
    }

    @Test
    fun ignoresNonSha256Digest() {
        val extra = ""","digest":"md5:abc123""""
        assertNull(parseRelease(release(asset("a.apk", goodUrl, extra)))!!.sha256)
    }

    @Test
    fun skipsNonApkAssetsAndPicksTheApk() {
        val assets = asset("notes.txt", goodUrl) + "," + asset("a.apk", goodUrl)
        assertNotNull(parseRelease(release(assets)))
    }

    @Test
    fun rejectsUrlOutsideTheRepository() {
        assertNull(parseRelease(release(asset("a.apk", "https://example.com/a.apk"))))
        assertNull(parseRelease(release(asset("a.apk", "http://github.com/AnmiTaliDev/nfcutils/releases/download/v1/a.apk"))))
    }

    @Test
    fun releaseWithoutApkIsNull() {
        assertNull(parseRelease(release("")))
    }

    @Test
    fun malformedJsonIsNull() {
        assertNull(parseRelease("not json"))
        assertNull(parseRelease("{}"))
    }
}
