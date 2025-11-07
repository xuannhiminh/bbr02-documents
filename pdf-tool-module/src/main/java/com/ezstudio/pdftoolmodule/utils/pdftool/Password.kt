package com.ezstudio.pdftoolmodule.utils.pdftool

import android.content.Context
import android.text.TextUtils
import com.ezstudio.pdftoolmodule.R
import com.ezstudio.pdftoolmodule.utils.Config
import com.ezstudio.pdftoolmodule.utils.FileUtils
import com.itextpdf.text.DocumentException
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object Password {

    fun isPDFEncrypted(path: String?): Boolean {
        var isEncrypted: Boolean
        var pdfReader: PdfReader? = null
        try {
            pdfReader = PdfReader(path)
            isEncrypted = pdfReader.isEncrypted
        } catch (e: Exception) {
            isEncrypted = true
        } finally {
            pdfReader?.close()
        }
        return isEncrypted
    }

    suspend fun encryption(
        context: Context,
        filePath: String,
        finPath: String,
        password: String
    ): String? {
        return withContext(Dispatchers.IO) {
            var error = false
            try {
                val reader = PdfReader(filePath)
                PdfReader.unethicalreading = true
                val stamper = PdfStamper(reader, FileOutputStream(finPath))
                stamper.setEncryption(
                    password.toByteArray(), Config.MASTER_PASSWORD.toByteArray(),
                    PdfWriter.ALLOW_PRINTING or PdfWriter.ALLOW_COPY, PdfWriter.ENCRYPTION_AES_128
                )
                stamper.close()
                reader.close()
            } catch (e: IOException) {
                e.printStackTrace()
                error = true
                File(finPath).delete()
            } catch (e: DocumentException) {
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

    suspend fun decryption(
        context: Context,
        filePath: String,
        finPath: String,
        inputPassword: String
    ): String? {
        return withContext(Dispatchers.IO) {
            var error = false
            val path = removePasswordMaster(filePath, finPath, inputPassword)
            if (TextUtils.isEmpty(path)) {
                try {
                    val reader = PdfReader(filePath, inputPassword.toByteArray())
                    val stamper = PdfStamper(
                        reader,
                        FileOutputStream(finPath)
                    )
                    stamper.close()
                    reader.close()
                } catch (e: DocumentException) {
                    e.printStackTrace()
                    error = true
                    File(finPath).delete()
                } catch (e: IOException) {
                    e.printStackTrace()
                    error = true
                    File(finPath).delete()
                }
            }
            FileUtils.scanFile(context, finPath, null)
            if (error) {
                null
            } else {
                finPath
            }
        }
    }

    private fun removePasswordMaster(
        filePath: String,
        finPath: String,
        inputPassword: String
    ): String {
        try {
            val reader = PdfReader(filePath, Config.MASTER_PASSWORD.toByteArray())
            val password: ByteArray = reader.computeUserPassword()
            val input = inputPassword.toByteArray()
            if (input.contentEquals(password)) {
                val stamper = PdfStamper(
                    reader,
                    FileOutputStream(finPath)
                )
                stamper.close()
                reader.close()
                return finPath
            }
        } catch (e: DocumentException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }

}