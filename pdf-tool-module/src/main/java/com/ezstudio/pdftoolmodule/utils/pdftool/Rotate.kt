package com.ezstudio.pdftoolmodule.utils.pdftool

import android.content.Context
import android.graphics.pdf.PdfDocument
import com.ezstudio.pdftoolmodule.utils.FileUtils
import com.itextpdf.text.pdf.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object Rotate {

    suspend fun start(
        context: Context,
        angle: Int,
        filePath: String,
        filename: String,
        folderSave: String
    ): String? {
        return withContext(Dispatchers.IO) {
            var error = false
            val finPath = "$folderSave/$filename.pdf"
            try {
                val reader = PdfReader(filePath)
                val n = reader.numberOfPages
                for (p in 1..n) {
                    rotatePage(reader,p,angle)
                }
                val stamper = PdfStamper(reader, FileOutputStream(finPath))
                stamper.close()
                reader.close()
            } catch (e: Exception) {
                e.printStackTrace()
                error = true
                File(finPath).delete()
            }
            FileUtils.scanFile(context, finPath, null)
            if (error) {
                null
            } else {
                finPath
            }
        }
    }

    fun rotatePage(pdfReader: PdfReader, index: Int, angle: Int): PdfDictionary {
        val page: PdfDictionary = pdfReader.getPageN(index)
        page.getAsNumber(PdfName.ROTATE)?.let {
            page.put(PdfName.ROTATE, PdfNumber((it.intValue() + angle) % 360))
        } ?: run {
            page.put(PdfName.ROTATE, PdfNumber(angle))
        }
        return page
    }
}