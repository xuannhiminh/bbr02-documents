package com.ezstudio.pdftoolmodule.utils.pdftool

import android.content.Context
import com.ezstudio.pdftoolmodule.utils.Config
import com.ezstudio.pdftoolmodule.utils.FileUtils
import com.ezteam.baseproject.EzListener
import com.itextpdf.text.Document
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object Merge {
    suspend fun start(
        context: Context,
        filename: String,
        folderSave: String,
        lstPath: MutableList<String>,
        password: String? = null
    ): String? {
        return withContext(Dispatchers.IO) {
            var finPath = folderSave
            var error = false
            try {
                var pdfreader: PdfReader
                // Create document object
                val document = Document()
                // Create pdf copy object to copy current document to the output mergedresult file
                finPath += "/$filename.pdf"
                val copy = PdfCopy(document, FileOutputStream(finPath))
                // Open the document
                password?.let {
                    copy.setEncryption(
                        it.toByteArray(),
                        Config.MASTER_PASSWORD.toByteArray(),
                        PdfWriter.ALLOW_PRINTING or PdfWriter.ALLOW_COPY,
                        PdfWriter.ENCRYPTION_AES_128
                    )
                }
                document.open()
                var numOfPages: Int
                for (pdfPath in lstPath) {
                    // Create pdf reader object to read each input pdf file
                    pdfreader = PdfReader(pdfPath)
                    PdfReader.unethicalreading = true
                    // Get the number of pages of the pdf file
                    numOfPages = pdfreader.numberOfPages
                    for (page in 1..numOfPages) {
                        // Import all pages from the file to PdfCopy
                        copy.addPage(copy.getImportedPage(pdfreader, page))
                    }
                }
                document.close() // close the document
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