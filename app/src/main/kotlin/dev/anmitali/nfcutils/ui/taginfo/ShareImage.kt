// SPDX-License-Identifier: GPL-3.0-only
// SPDX-FileCopyrightText: 2026 AnmiTalidev <anmitalidev@nuros.org>

package dev.anmitali.nfcutils.ui.taginfo

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ScrollView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.view.drawToBitmap
import dev.anmitali.nfcutils.nfc.TagDetails
import java.io.File

private const val IMAGE_WIDTH_DP = 400
private const val MAX_LAYOUT_FRAMES = 10

suspend fun renderShareImage(
    host: ViewGroup,
    details: TagDetails,
    colorScheme: ColorScheme,
    shapes: Shapes,
    typography: Typography,
): Bitmap {
    val context = host.context
    val composeView = ComposeView(context)
    composeView.setContent {
        MaterialTheme(colorScheme = colorScheme, shapes = shapes, typography = typography) {
            ShareImageContent(details)
        }
    }
    val scroll = ScrollView(context)
    scroll.visibility = View.INVISIBLE
    scroll.addView(composeView)
    val width = (IMAGE_WIDTH_DP * context.resources.displayMetrics.density).toInt()
    host.addView(scroll, FrameLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT))
    try {
        var frames = 0
        while (composeView.height == 0 && frames < MAX_LAYOUT_FRAMES) {
            withFrameNanos { }
            frames++
        }
        withFrameNanos { }
        check(composeView.height > 0) { "Share image was not laid out" }
        return composeView.drawToBitmap()
    } finally {
        host.removeView(scroll)
    }
}

fun shareImage(context: Context, bitmap: Bitmap, chooserTitle: String) {
    val directory = File(context.cacheDir, "share")
    directory.mkdirs()
    val file = File(directory, "scan.png")
    file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
    val send = Intent(Intent.ACTION_SEND)
        .setType("image/png")
        .putExtra(Intent.EXTRA_STREAM, uri)
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(send, chooserTitle))
}

@Composable
private fun ShareImageContent(details: TagDetails) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TagInfoCard(details)
            for (record in details.records) RecordCard(record)
        }
    }
}
