package com.ezstudio.pdftoolmodule.utils.pdftool

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.ezteam.baseproject.extensions.invertBitmap
import com.itextpdf.text.pdf.PdfReader
import java.io.File
import java.lang.Exception

object Thumbnail {

    fun start(
        filePath: String,
        quality: Float,
        isNightMode: Boolean = false,
        result: (Bitmap, Int) -> Unit
    ) {
        try {
            val fileDescriptor =
                ParcelFileDescriptor.open(File(filePath), ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fileDescriptor)
            val pageCount = renderer.pageCount
            for (position in 0 until pageCount) {
                val page = renderer.openPage(position)
                val currentBitmap = Bitmap.createBitmap(
                    (page.width * quality).toInt(), (page.height * quality).toInt(),
                    Bitmap.Config.ARGB_8888
                )
                currentBitmap.eraseColor(Color.WHITE)
                page.render(
                    currentBitmap,
                    null,
                    null,
                    PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                )
                page.close()
//            val thumbnail = currentBitmap.bitmapToUriCache(context)
                if (isNightMode) {
                    result(currentBitmap.invertBitmap(), position)
                } else {
                    result(currentBitmap, position)
                }
            }
        } catch (ex: Exception) {

        }
    }
}