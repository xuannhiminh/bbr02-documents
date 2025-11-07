package com.ezstudio.pdftoolmodule.utils.pdftool

import android.content.Context
import android.net.Uri
import com.ezstudio.pdftoolmodule.utils.FileUtils
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.pdf.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object AddImage {

    suspend fun start(
        context: Context,
        filePath: String,
        filename: String,
        folderSave: String,
        lstImage: MutableList<Uri>,
    ): String? {
        return withContext(Dispatchers.IO) {
            val finPath = "$folderSave/$filename.pdf"
            var error = false
            try {
                val reader = PdfReader(filePath)
                val document = Document()
                val writer = PdfWriter.getInstance(document, FileOutputStream(finPath))
                val documentRect = document.pageSize
                document.open()

                val numOfPages: Int = reader.numberOfPages
                val cb = writer.directContent
                var importedPage: PdfImportedPage?
                for (page in 1..numOfPages) {
                    importedPage = writer.getImportedPage(reader, page)
                    document.newPage()
                    cb.addTemplate(importedPage, 0f, 0f)
                }

                for (i in lstImage.indices) {
                    document.newPage()
                    val image: Image = Image.getInstance(lstImage[i].path)
                    image.border = 0
                    val pageWidth = document.pageSize.width // - (mMarginLeft + mMarginRight);
                    val pageHeight = document.pageSize.height // - (mMarginBottom + mMarginTop);
                    image.scaleToFit(pageWidth, pageHeight)
                    image.setAbsolutePosition(
                        (documentRect.width - image.scaledWidth) / 2,
                        (documentRect.height - image.scaledHeight) / 2
                    )
                    document.add(image)
                }
                document.close()
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
}