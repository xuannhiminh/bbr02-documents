package com.ezteam.ezpdflib.util

import android.content.Context
import com.ezteam.ezpdflib.util.Config.PdfLib.DEFAULT_MASTER_PW
import com.itextpdf.text.DocumentException
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import com.itextpdf.text.pdf.PdfWriter
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object PdfUtils {

    fun isPDFEncrypted(path: String?): Boolean {
        var isEncrypted: Boolean
        var pdfReader: PdfReader? = null
        try {
            pdfReader = PdfReader(path)
            isEncrypted = pdfReader.isEncrypted
        } catch (e: IOException) {
            isEncrypted = true
        } finally {
            pdfReader?.close()
        }
        return isEncrypted
    }

    fun removePassword(
        context: Context,
        file: String,
        inputPassword: String
    ): String {
        val newPath = context.cacheDir.path + System.currentTimeMillis() + ".pdf"
        removePasswordMaster(context, file, inputPassword).let {
            if (it.isNotEmpty()) {
                return it
            }
        }
        try {
            val reader = PdfReader(file, inputPassword.toByteArray())
            val stamper = PdfStamper(
                reader,
                FileOutputStream(newPath)
            )
            stamper.close()
            reader.close()
            return newPath
        } catch (e: DocumentException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun removePasswordMaster(
        context: Context,
        file: String,
        inputPassword: String
    ): String {
        try {
            val newPath = context.cacheDir.path + System.currentTimeMillis() + ".pdf"
            val reader = PdfReader(file, DEFAULT_MASTER_PW.toByteArray())
            val password: ByteArray = reader.computeUserPassword()
            val input = inputPassword.toByteArray()
            if (input.contentEquals(password)) {
                val stamper = PdfStamper(
                    reader,
                    FileOutputStream(newPath)
                )
                stamper.close()
                reader.close()
                return newPath
            }
        } catch (e: DocumentException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }

    fun doEncryption(context: Context, path: String, password: String): String {
        return try {
            val finalOutputFile = context.cacheDir.path + System.currentTimeMillis() + ".pdf"
            val reader = PdfReader(path)
            val stamper = PdfStamper(reader, FileOutputStream(finalOutputFile))
            stamper.setEncryption(
                password.toByteArray(), DEFAULT_MASTER_PW.toByteArray(),
                PdfWriter.ALLOW_PRINTING or PdfWriter.ALLOW_COPY, PdfWriter.ENCRYPTION_AES_128
            )
            stamper.close()
            reader.close()
            finalOutputFile
        } catch (e: IOException) {
            ""
        } catch (e: DocumentException) {
            ""
        }
    }

    fun removePasswordForPrint(
        context: Context,
        filePath: String,
        inputPassword: String
    ): String {
        val newPath = "${context.cacheDir.path}/${FilenameUtils.getName(filePath)}"
        val file = File(newPath)
        if (file.exists()) {
            file.delete()
        }
        try {
            val reader = PdfReader(filePath, inputPassword.toByteArray())
            val stamper = PdfStamper(
                unlockPdf(reader),
                FileOutputStream(newPath)
            )
            stamper.close()
            reader.close()
            return newPath
        } catch (e: DocumentException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun unlockPdf(reader: PdfReader?): PdfReader? {
        if (reader == null) {
            return reader
        }
        try {
            val f = reader.javaClass.getDeclaredField("encrypted")
            f.isAccessible = true
            f[reader] = false
        } catch (e: Exception) {
        }
        return reader
    }
}