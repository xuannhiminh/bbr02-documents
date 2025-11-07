package com.ezstudio.pdftoolmodule.utils.pdftool

import android.content.Context
import android.os.ParcelFileDescriptor
import com.ezstudio.pdftoolmodule.utils.FileUtils
import com.itextpdf.text.pdf.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

object Invert {
    suspend fun start(
        context: Context,
        path: String,
        filename: String,
        folderSave: String,
    ): String? {
        return withContext(Dispatchers.IO) {
            var finPath = folderSave
            var error = false
            try {
                val fileDescriptor =
                    ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY)
                fileDescriptor?.let {
                    finPath += "/$filename.pdf"
                    error = !createPDF(path, finPath)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                error = true
            } catch (e: SecurityException) {
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

    private fun createPDF(mPath: String, outputPath: String): Boolean {
        return try {
            val reader = PdfReader(mPath)
            val os: OutputStream = FileOutputStream(outputPath)
            val stamper = PdfStamper(reader, os)
            invert(stamper)
            stamper.close()
            os.close()
            true
        } catch (er: Exception) {
            er.printStackTrace()
            false
        }
    }

    private fun invert(stamper: PdfStamper) {
        for (i in stamper.reader.numberOfPages downTo 1) {
            invertPage(stamper, i)
        }
    }

    private fun invertPage(stamper: PdfStamper, page: Int) {
        val rect = stamper.reader.getPageSize(page)
        var cb = stamper.getOverContent(page)
        val gs = PdfGState()
        gs.setBlendMode(PdfGState.BM_DIFFERENCE)
        cb.setGState(gs)
        cb.setColorFill(GrayColor(1.0f))
        cb.rectangle(rect.left, rect.bottom, rect.width, rect.height)
        cb.fill()
        cb = stamper.getUnderContent(page)
        cb.setColorFill(GrayColor(1.0f))
        cb.rectangle(rect.left, rect.bottom, rect.width, rect.height)
        cb.fill()
    }

}