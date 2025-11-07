package com.ezstudio.pdftoolmodule.utils.pdftool

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.text.TextUtils
import com.ezstudio.pdftoolmodule.utils.FileUtils
import com.itextpdf.text.DocumentException
import com.itextpdf.text.pdf.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.StringBuilder

object RemoveDuplicatePage {
    suspend fun start(
        context: Context,
        path: String,
        filename: String,
        folderSave: String,
    ): String? {
        return withContext(Dispatchers.IO) {
            val lstBitmap = mutableListOf<Bitmap>()
            val sequence = StringBuilder("")
            var finPath = folderSave
            var error = false
            try {
                val fileDescriptor =
                    ParcelFileDescriptor.open(File(path), ParcelFileDescriptor.MODE_READ_ONLY)
                if (fileDescriptor != null) {
                    val renderer = PdfRenderer(fileDescriptor)
                    val pageCount = renderer.pageCount
                    for (position in 0 until pageCount) {
                        val page = renderer.openPage(position)
                        // generate bitmaps for individual pdf pages
                        val currentBitmap = Bitmap.createBitmap(
                            page.width, page.height,
                            Bitmap.Config.ARGB_8888
                        )
                        // say we render for showing on the screen
                        page.render(
                            currentBitmap,
                            null,
                            null,
                            PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                        )
                        // close the page
                        page.close()

                        //Adding bitmap to arrayList if not same

                        var add = true
                        for (b in lstBitmap) {
                            add = !b.sameAs(currentBitmap)
                        }
                        if (add) {
                            lstBitmap.add(currentBitmap)
                            sequence.append(position + 1).append(",")
                        }
                    }

                    // close the renderer
                    renderer.close()
                    if (lstBitmap.size == pageCount) {
                        //No repetition found
                        finPath = path
                    } else {
                        val mPages: String = sequence.toString()
                        finPath += "/$filename.pdf"
                        error = !createPDF(path, finPath, mPages)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                error = true
                File(finPath).delete()
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

    fun createPDF(inputPath: String, output: String, pages: String): Boolean {
        return try {
            val reader = PdfReader(inputPath)
            PdfReader.unethicalreading = true
            reader.selectPages(pages)
            val pdfStamper = PdfStamper(
                reader,
                FileOutputStream(output)
            )
            pdfStamper.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } catch (e: DocumentException) {
            e.printStackTrace()
            false
        }
    }

}