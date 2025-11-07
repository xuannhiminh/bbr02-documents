package com.ezstudio.pdftoolmodule.utils.pdftool

import android.content.Context
import com.ezstudio.pdftoolmodule.R
import com.ezstudio.pdftoolmodule.utils.FileUtils
import com.itextpdf.text.Document
import com.itextpdf.text.DocumentException
import com.itextpdf.text.pdf.PdfCopy
import com.itextpdf.text.pdf.PdfReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object Split {

    private const val ERROR_PAGE_NUMBER = 1
    private const val ERROR_PAGE_RANGE = 2
    private const val ERROR_INVALID_INPUT = 3

    fun isInputValid(path: String, splitDetail: String): Int {
        val splitConfig: String = splitDetail.replace("\\s+".toRegex(), "")
        val delims = "[,]"
        val ranges: Array<String> = splitConfig.split(delims.toRegex()).toTypedArray()
        val reader = PdfReader(path)
        val numOfPages = reader.numberOfPages
        return when (checkRangeValidity(numOfPages, ranges)) {
            ERROR_PAGE_NUMBER -> R.string.error_page_number
            ERROR_PAGE_RANGE -> R.string.error_page_range
            ERROR_INVALID_INPUT -> R.string.error_invalid_input
            else -> 0
        }
    }

    private fun checkRangeValidity(numOfPages: Int, ranges: Array<String>): Int {
        var startPage: Int
        var endPage: Int
        for (range in ranges) {
            if (!range.contains("-")) {
                startPage = try {
                    range.toInt()
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    return ERROR_INVALID_INPUT
                }
                if (startPage > numOfPages || startPage == 0) {
                    return ERROR_PAGE_NUMBER
                }
            } else {
                try {
                    startPage = range.substring(0, range.indexOf("-")).toInt()
                    endPage = range.substring(range.indexOf("-") + 1).toInt()
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    return ERROR_INVALID_INPUT
                } catch (e: StringIndexOutOfBoundsException) {
                    e.printStackTrace()
                    return ERROR_INVALID_INPUT
                }
                if (startPage > numOfPages || endPage > numOfPages || startPage == 0 || endPage == 0) {
                    return ERROR_PAGE_NUMBER
                } else if (startPage >= endPage) {
                    return ERROR_PAGE_RANGE
                }
            }
        }
        return 0
    }

    suspend fun start(
        context: Context,
        path: String,
        splitDetail: String,
        folderSave: String,
    ): MutableList<String>? {
        return withContext(Dispatchers.IO) {
            val splitConfig: String = splitDetail.replace("\\s+".toRegex(), "")
            val outputPaths = mutableListOf<String>()
            val delims = "[,]"
            val ranges = splitConfig.split(delims.toRegex()).toTypedArray()
            var error = false
            try {
                val reader = PdfReader(path)
                var copy: PdfCopy
                var document: Document
                for (range in ranges) {
                    var startPage: Int
                    var endPage: Int
                    var finPath =
                        "$folderSave/${context.getString(R.string.split)}_${System.currentTimeMillis()}"
                    if (!range.contains("-")) {
                        startPage = range.toInt()
                        document = Document()
                        finPath += "_$startPage.pdf"
                        copy = PdfCopy(document, FileOutputStream(finPath))
                        document.open()
                        copy.addPage(copy.getImportedPage(reader, startPage))
                        document.close()
                    } else {
                        startPage = range.substring(0, range.indexOf("-")).toInt()
                        endPage = range.substring(range.indexOf("-") + 1).toInt()
                        document = Document()
                        finPath += "_$startPage-$endPage.pdf"
                        copy = PdfCopy(document, FileOutputStream(finPath))
                        document.open()
                        for (page in startPage..endPage) {
                            copy.addPage(copy.getImportedPage(reader, page))
                        }
                        document.close()
                    }
                    outputPaths.add(finPath)
                    FileUtils.scanFile(context, finPath, null)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                error = true
            } catch (e: DocumentException) {
                e.printStackTrace()
                error = true
            }
            if (error) {
                null
            } else {
                outputPaths
            }
        }
    }
}